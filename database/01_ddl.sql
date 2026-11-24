-- Fuente: docs/especificaciones/Documento_Maestro_LeadLaunch.md §4.3 (copia fiel).
-- Ya aplicado en Neon. Para una base nueva: ejecutar 01_ddl.sql y luego 02_seed.sql
-- (o desde backend: python -m scripts.aplicar_ddl). Verificar con: python -m scripts.verificar_bd


-- ============================================================================
-- LEADLAUNCH: SCRIPT DE DEFINICIÓN DE DATOS (DDL) - POSTGRESQL 15+
-- VERTICAL MVP: GASTRONOMÍA Y REPOSTERÍA (MODELO PARAMÉTRICO EXTENSIBLE)
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ----------------------------------------------------------------------------
-- 1. ENTIDADES DE CATÁLOGO (ESTÁTICAS Y NORMATIVAS)
-- ----------------------------------------------------------------------------

CREATE TABLE cat_rubros (
    rubro_id        SMALLSERIAL PRIMARY KEY,
    codigo          VARCHAR(30)  NOT NULL UNIQUE,
    nombre          VARCHAR(80)  NOT NULL,
    sector          VARCHAR(40)  NOT NULL DEFAULT 'GASTRONOMIA',
    activo          BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE cat_tipos_friccion (
    tipo_friccion_id SMALLSERIAL PRIMARY KEY,
    codigo           VARCHAR(30)  NOT NULL UNIQUE,
    nombre           VARCHAR(80)  NOT NULL,
    descripcion      VARCHAR(200) NOT NULL
);

-- ----------------------------------------------------------------------------
-- 2. SEGURIDAD Y ENTIDADES PASIVAS / CUENTAS DE COMPORTAMIENTO (PARQUE)
-- ----------------------------------------------------------------------------

CREATE TABLE usuarios (
    usuario_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    correo          VARCHAR(120) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    rol             VARCHAR(20)  NOT NULL DEFAULT 'EMPRENDEDOR'
                    CHECK (rol IN ('EMPRENDEDOR', 'ADMIN')),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE negocios (
    negocio_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id           UUID         NOT NULL REFERENCES usuarios(usuario_id) ON DELETE RESTRICT,
    rubro_id             SMALLINT     NOT NULL REFERENCES cat_rubros(rubro_id) ON DELETE RESTRICT,
    razon_comercial      VARCHAR(120) NOT NULL,
    descripcion          VARCHAR(400) NOT NULL,
    whatsapp_numero      VARCHAR(15)  NOT NULL CHECK (whatsapp_numero ~ '^[0-9]{10,15}$'),
    distrito             VARCHAR(80)  NOT NULL,
    latitud              NUMERIC(9,6) NOT NULL CHECK (latitud BETWEEN -90 AND 90),
    longitud             NUMERIC(9,6) NOT NULL CHECK (longitud BETWEEN -180 AND 180),
    radio_cobertura_km   NUMERIC(4,2) NOT NULL DEFAULT 3.00 CHECK (radio_cobertura_km BETWEEN 0.50 AND 25.00),
    -- Acumuladores de Comportamiento (Entidad Cuenta)
    total_escaneos       INTEGER      NOT NULL DEFAULT 0 CHECK (total_escaneos >= 0),
    total_cotizaciones   INTEGER      NOT NULL DEFAULT 0 CHECK (total_cotizaciones >= 0),
    total_ventas_ganadas INTEGER      NOT NULL DEFAULT 0 CHECK (total_ventas_ganadas >= 0),
    monto_ventas_ganadas NUMERIC(12,2) NOT NULL DEFAULT 0.00 CHECK (monto_ventas_ganadas >= 0),
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    actualizado_en       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
) WITH (fillfactor = 80);

CREATE TABLE cat_politicas_negocio (
    politica_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    negocio_id            UUID         NOT NULL UNIQUE REFERENCES negocios(negocio_id) ON DELETE RESTRICT,
    permite_express       BOOLEAN      NOT NULL DEFAULT TRUE,
    permite_acumular_desc BOOLEAN      NOT NULL DEFAULT FALSE,
    acepta_yape_plin      BOOLEAN      NOT NULL DEFAULT TRUE,
    horario_atencion      VARCHAR(100) NOT NULL DEFAULT '09:00 - 20:00',
    actualizado_en        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE cat_reglas_negocio (
    regla_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    negocio_id           UUID         NOT NULL REFERENCES negocios(negocio_id) ON DELETE RESTRICT,
    tipo_regla           VARCHAR(30)  NOT NULL
                         CHECK (tipo_regla IN ('RECARGO_EXPRESS', 'DESC_BIENVENIDA', 'DESC_VOLUMEN')),
    descripcion          VARCHAR(150) NOT NULL,
    monto_minimo         NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (monto_minimo >= 0),
    cantidad_minima      INTEGER      NOT NULL DEFAULT 1 CHECK (cantidad_minima >= 1),
    valor_porcentaje     NUMERIC(5,2) NOT NULL CHECK (valor_porcentaje BETWEEN 0.00 AND 100.00),
    vigente_desde        DATE         NOT NULL DEFAULT CURRENT_DATE,
    vigente_hasta        DATE,
    activa               BOOLEAN      NOT NULL DEFAULT TRUE,
    CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde)
);

CREATE TABLE productos (
    producto_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    negocio_id           UUID         NOT NULL REFERENCES negocios(negocio_id) ON DELETE RESTRICT,
    sku                  VARCHAR(30)  NOT NULL,
    nombre               VARCHAR(120) NOT NULL,
    descripcion          VARCHAR(300),
    precio_base          NUMERIC(10,2) NOT NULL CHECK (precio_base > 0),
    tiempo_prep_horas    SMALLINT     NOT NULL DEFAULT 24 CHECK (tiempo_prep_horas >= 1),
    stock_disponible     INTEGER      NOT NULL DEFAULT 0 CHECK (stock_disponible >= 0),
    permite_express      BOOLEAN      NOT NULL DEFAULT TRUE,
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (negocio_id, sku)
) WITH (fillfactor = 80);

CREATE TABLE competidores_locales (
    competidor_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fuente_externa_id    VARCHAR(100) NOT NULL UNIQUE,
    rubro_id             SMALLINT     NOT NULL REFERENCES cat_rubros(rubro_id) ON DELETE RESTRICT,
    nombre               VARCHAR(150) NOT NULL,
    distrito             VARCHAR(80)  NOT NULL,
    latitud              NUMERIC(9,6) NOT NULL,
    longitud             NUMERIC(9,6) NOT NULL,
    calificacion_prom    NUMERIC(2,1) CHECK (calificacion_prom BETWEEN 1.0 AND 5.0),
    actualizado_en       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- 3. ENTIDADES EJECUTIVAS (EVENTOS Y TRANSACCIONES)
-- ----------------------------------------------------------------------------

CREATE TABLE eventos_escaneo_radar (
    escaneo_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    negocio_id           UUID         NOT NULL REFERENCES negocios(negocio_id) ON DELETE RESTRICT,
    fuente_datos         VARCHAR(30)  NOT NULL CHECK (fuente_datos IN ('OVERPASS_OSM', 'GOOGLE_PLACES', 'DATASET_FALLBACK')),
    radio_busqueda_km    NUMERIC(4,2) NOT NULL,
    total_analizados     INTEGER      NOT NULL DEFAULT 0 CHECK (total_analizados >= 0),
    total_fricciones     INTEGER      NOT NULL DEFAULT 0 CHECK (total_fricciones >= 0),
    estado               VARCHAR(20)  NOT NULL DEFAULT 'COMPLETADO'
                         CHECK (estado IN ('EN_PROCESO', 'COMPLETADO', 'FALLIDO')),
    ejecutado_en         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE hallazgos_friccion (
    hallazgo_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escaneo_id           UUID         NOT NULL REFERENCES eventos_escaneo_radar(escaneo_id) ON DELETE RESTRICT,
    negocio_id           UUID         NOT NULL REFERENCES negocios(negocio_id) ON DELETE RESTRICT,
    competidor_id        UUID         NOT NULL REFERENCES competidores_locales(competidor_id) ON DELETE RESTRICT,
    tipo_friccion_id     SMALLINT     NOT NULL REFERENCES cat_tipos_friccion(tipo_friccion_id) ON DELETE RESTRICT,
    calificacion         SMALLINT     NOT NULL CHECK (calificacion BETWEEN 1 AND 3),
    texto_resena         TEXT         NOT NULL,
    resumen_dolor        VARCHAR(250) NOT NULL,
    creado_en            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
) WITH (fillfactor = 100);

CREATE TABLE campanas_adaptativas (
    campana_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    negocio_id           UUID         NOT NULL REFERENCES negocios(negocio_id) ON DELETE RESTRICT,
    escaneo_id           UUID         REFERENCES eventos_escaneo_radar(escaneo_id) ON DELETE RESTRICT,
    alerta_adaptativa    TEXT         NOT NULL,
    post_redes           TEXT         NOT NULL,
    guion_reel_15s       TEXT         NOT NULL,
    estado               VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR'
                         CHECK (estado IN ('BORRADOR', 'PUBLICADA', 'ARCHIVADA')),
    creado_en            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE cotizaciones_cierre (
    cotizacion_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    negocio_id           UUID         NOT NULL REFERENCES negocios(negocio_id) ON DELETE RESTRICT,
    nombre_prospecto     VARCHAR(100) NOT NULL,
    telefono_prospecto   VARCHAR(15)  CHECK (telefono_prospecto IS NULL OR telefono_prospecto ~ '^[0-9]{9,15}$'),
    es_entrega_express   BOOLEAN      NOT NULL DEFAULT FALSE,
    es_cliente_nuevo     BOOLEAN      NOT NULL DEFAULT TRUE,
    subtotal             NUMERIC(10,2) NOT NULL CHECK (subtotal > 0),
    monto_recargo        NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (monto_recargo >= 0),
    monto_descuento      NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (monto_descuento >= 0),
    total_neto           NUMERIC(10,2) NOT NULL CHECK (total_neto > 0),
    mensaje_whatsapp     TEXT         NOT NULL,
    snapshot_reglas_json JSONB        NOT NULL,
    estado               VARCHAR(20)  NOT NULL DEFAULT 'EMITIDA'
                         CHECK (estado IN ('EMITIDA', 'ENVIADA_WA', 'GANADA', 'PERDIDA')),
    creado_en            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    actualizado_en       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CHECK (total_neto = subtotal + monto_recargo - monto_descuento)
) WITH (fillfactor = 80);

CREATE TABLE cotizacion_detalles (
    detalle_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cotizacion_id        UUID         NOT NULL REFERENCES cotizaciones_cierre(cotizacion_id) ON DELETE RESTRICT,
    producto_id          UUID         NOT NULL REFERENCES productos(producto_id) ON DELETE RESTRICT,
    cantidad             INTEGER      NOT NULL CHECK (cantidad > 0),
    precio_unitario_snap NUMERIC(10,2) NOT NULL CHECK (precio_unitario_snap > 0),
    importe_linea        NUMERIC(10,2) NOT NULL CHECK (importe_linea = cantidad * precio_unitario_snap)
);

CREATE TABLE bitacora_auditoria (
    auditoria_id         BIGSERIAL PRIMARY KEY,
    entidad_afectada     VARCHAR(50)  NOT NULL,
    registro_id          UUID         NOT NULL,
    accion               VARCHAR(20)  NOT NULL CHECK (accion IN ('INSERT', 'UPDATE', 'DELETE')),
    usuario_actor        UUID,
    detalle_json         JSONB        NOT NULL,
    registrado_en        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
) WITH (fillfactor = 100);

-- ----------------------------------------------------------------------------
-- 4. ÍNDICES SECUNDARIOS Y DE RENDIMIENTO (ÁRBOL-B)
-- ----------------------------------------------------------------------------

CREATE INDEX idx_negocios_usuario          ON negocios(usuario_id);
CREATE INDEX idx_productos_negocio_activo  ON productos(negocio_id, activo);
CREATE INDEX idx_reglas_negocio_activa     ON cat_reglas_negocio(negocio_id, tipo_regla, activa);
CREATE INDEX idx_escaneos_negocio_fecha    ON eventos_escaneo_radar(negocio_id, ejecutado_en DESC);
CREATE INDEX idx_hallazgos_negocio_fecha   ON hallazgos_friccion(negocio_id, creado_en DESC);
CREATE INDEX idx_campanas_negocio_fecha    ON campanas_adaptativas(negocio_id, creado_en DESC);
CREATE INDEX idx_cotizaciones_negocio_est  ON cotizaciones_cierre(negocio_id, estado, creado_en DESC);
CREATE INDEX idx_auditoria_entidad_fecha   ON bitacora_auditoria(entidad_afectada, registrado_en DESC);

-- ----------------------------------------------------------------------------
-- 5. REGLAS ACTIVAS (TRIGGERS DE COMPORTAMIENTO Y AUDITORÍA)
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_actualizar_cuenta_cotizacion()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE negocios
           SET total_cotizaciones = total_cotizaciones + 1,
               actualizado_en = NOW()
         WHERE negocio_id = NEW.negocio_id;

        INSERT INTO bitacora_auditoria(entidad_afectada, registro_id, accion, detalle_json)
        VALUES ('cotizaciones_cierre', NEW.cotizacion_id, 'INSERT',
                jsonb_build_object('total_neto', NEW.total_neto, 'estado', NEW.estado));

    ELSIF TG_OP = 'UPDATE' AND OLD.estado <> NEW.estado THEN
        IF NEW.estado = 'GANADA' AND OLD.estado <> 'GANADA' THEN
            UPDATE negocios
               SET total_ventas_ganadas = total_ventas_ganadas + 1,
                   monto_ventas_ganadas = monto_ventas_ganadas + NEW.total_neto,
                   actualizado_en = NOW()
             WHERE negocio_id = NEW.negocio_id;
        ELSIF OLD.estado = 'GANADA' AND NEW.estado <> 'GANADA' THEN
            UPDATE negocios
               SET total_ventas_ganadas = GREATEST(0, total_ventas_ganadas - 1),
                   monto_ventas_ganadas = GREATEST(0, monto_ventas_ganadas - OLD.total_neto),
                   actualizado_en = NOW()
             WHERE negocio_id = NEW.negocio_id;
        END IF;

        INSERT INTO bitacora_auditoria(entidad_afectada, registro_id, accion, detalle_json)
        VALUES ('cotizaciones_cierre', NEW.cotizacion_id, 'UPDATE',
                jsonb_build_object('estado_anterior', OLD.estado, 'estado_nuevo', NEW.estado));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cotizacion_comportamiento
AFTER INSERT OR UPDATE ON cotizaciones_cierre
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_cuenta_cotizacion();

CREATE OR REPLACE FUNCTION fn_acumular_escaneo_radar()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.estado = 'COMPLETADO' THEN
        UPDATE negocios
           SET total_escaneos = total_escaneos + 1,
               actualizado_en = NOW()
         WHERE negocio_id = NEW.negocio_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_escaneo_comportamiento
AFTER INSERT ON eventos_escaneo_radar
FOR EACH ROW EXECUTE FUNCTION fn_acumular_escaneo_radar();

-- ----------------------------------------------------------------------------
-- 6. VISTA MATERIALIZADA PARA CONSULTA CRÍTICA (DASHBOARD)
-- ----------------------------------------------------------------------------

CREATE MATERIALIZED VIEW mv_resumen_comercial_negocio AS
SELECT
    n.negocio_id,
    n.razon_comercial,
    r.nombre AS rubro_nombre,
    n.distrito,
    n.total_escaneos,
    n.total_cotizaciones,
    n.total_ventas_ganadas,
    n.monto_ventas_ganadas,
    CASE WHEN n.total_cotizaciones > 0
         THEN ROUND((n.total_ventas_ganadas::NUMERIC / n.total_cotizaciones) * 100, 2)
         ELSE 0.00 END AS tasa_conversion_pct,
    (SELECT COUNT(*) FROM productos p WHERE p.negocio_id = n.negocio_id AND p.activo = TRUE) AS productos_activos,
    (SELECT COUNT(*) FROM hallazgos_friccion h WHERE h.negocio_id = n.negocio_id) AS fricciones_detectadas,
    (SELECT c.alerta_adaptativa FROM campanas_adaptativas c
      WHERE c.negocio_id = n.negocio_id ORDER BY c.creado_en DESC LIMIT 1) AS ultima_alerta_adaptativa
FROM negocios n
JOIN cat_rubros r ON r.rubro_id = n.rubro_id
WHERE n.activo = TRUE;

CREATE UNIQUE INDEX idx_mv_resumen_negocio ON mv_resumen_comercial_negocio(negocio_id);
