# ESPECIFICACIÓN INTEGRAL DE PROYECTO, ARQUITECTURA, BASE DE DATOS Y BACKLOG SCRUM
## PROYECTO: LEADLAUNCH — RADAR COMERCIAL Y COPILOTO ADAPTATIVO LOCAL
### VERTICAL MVP: EMPRENDIMIENTOS GASTRONÓMICOS Y DE REPOSTERÍA

---

## 1. DEFINICIÓN DEL PROBLEMA, SOLUCIÓN PROPUESTA Y ALCANCE

### 1.1 Definición del Problema
Los micro y pequeños emprendimientos gastronómicos en fase de lanzamiento (repostería artesanal, tortas personalizadas, comida rápida local y cafeterías/postres) enfrentan una barrera crítica de supervivencia durante sus primeros meses: **la dificultad para captar y cerrar sus primeros clientes de forma predecible en zonas altamente competidas**.

Esta problemática se origina en cuatro cuellos de botella concretos dentro de su proceso comercial:
1. **Carencia de presupuesto publicitario:** No disponen de capital para pauta pagada (Meta Ads o Google Ads), dependiendo únicamente del alcance orgánico limitado de sus contactos personales.
2. **Ceguera frente a las fallas de la competencia local:** Lanzan su menú sin visibilidad de las quejas recurrentes que los clientes dejan sobre negocios vecinos (demoras en delivery, pedidos que llegan dañados, falta de atención para pedidos urgentes o costos de envío elevados).
3. **Bloqueo creativo y publicidad desconectada:** Publican contenido genérico que no resuelve las fricciones reales del consumidor de su distrito.
4. **Fricción y errores en el cierre por WhatsApp:** Cuando reciben consultas, demoran en cotizar o calculan manualmente recargos de entrega rápida y promociones, perdiendo la venta antes de concretar el pago.

### 1.2 Visión del Producto y Arquetipo de Cliente
* **Visión del Producto:** Proveer a microemprendedores gastronómicos una plataforma móvil adaptativa e integrada que transforme las debilidades públicas de sus competidores locales en oportunidades comerciales accionables: ajustes tácticos de menú, publicidad reactiva de alta conversión y cotizaciones rápidas listas para WhatsApp, operando con costo de infraestructura cero.
* **Arquetipo de Cliente (Target User):**
  * **Perfil:** Emprendedor independiente (22 a 35 años) que inicia un negocio de repostería personalizada, bocaditos para eventos o comida rápida en un distrito urbano de Lima.
  * **Canal principal de cierre:** WhatsApp y pagos digitales inmediatos (Yape / Plin).
  * **Objetivo inmediato:** Diferenciarse de las tiendas ya establecidas en un radio de 3 km y captar sus primeros clientes recurrentes sin gastar en publicidad pagada.

### 1.3 Solución Propuesta (El Ciclo Comercial Integrado)
**LeadLaunch** es un sistema distribuido Cliente-Servidor compuesto por una **aplicación móvil Android nativa** y una **API REST propia desplegada en la nube** con base de datos relacional PostgreSQL. Optimiza el ciclo completo de adquisición y cierre en tres pilares acoplados:

```text
           [ ENTORNO LOCAL: MAPAS Y RESEÑAS PÚBLICAS ]
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ 1. RADAR LOCAL DE FRICCIONES (Ingesta y Filtrado en Backend)         │
│    • Mapea negocios gastronómicos en el radio del emprendedor.       │
│    • Filtra algorítmicamente reseñas críticas (<= 3 estrellas).      │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ 2. ADAPTACIÓN DE OFERTA Y PUBLICIDAD REACTIVA (Motor IA en Backend)  │
│    • Cruza las quejas del vecindario contra el catálogo en BD.       │
│    • Genera alerta de ajuste (ej. combo express) + post + guion 15s. │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ 3. CIERRE RÁPIDO Y TRAZABILIDAD (Liquidación Paramétrica + WhatsApp) │
│    • Aplica reglas de negocio (recargo express / desc. bienvenida).  │
│    • Dispara cotización formateada a WhatsApp y mide tasa de cierre. │
└──────────────────────────────────────────────────────────────────────┘
```

### 1.4 Alcance del Proyecto

#### Dentro del Alcance (In-Scope — MVP Acotado y Paramétrico)
* **Vertical de Negocio del MVP:** Orientado exclusivamente al sector **Gastronómico y de Repostería**, estructurado en 3 subcategorías afines (*Repostería y Pastelería Artesanal*, *Comida Rápida y Bocaditos*, *Cafeterías y Postres*).
* **Extensibilidad Paramétrica:** Aunque el MVP se valida y demuestra en gastronomía, la tabla `cat_rubros` y las reglas de liquidación están completamente parametrizadas en base de datos, permitiendo escalar a otros sectores en el futuro sin modificar el código fuente.
* **Backend API REST en Capas (Cloud):** Controladores, Servicios de Negocio y Acceso a Datos desplegados en la nube con autenticación JWT, validaciones estrictas, manejo estandarizado de códigos HTTP y documentación OpenAPI/Swagger.
* **Base de Datos Relacional en la Nube (PostgreSQL):** Esquema normalizado con separación Catálogo/Parque, triggers de auditoría y comportamiento, y vista materializada para la consulta crítica del dashboard.
* **Cliente Móvil Android (Kotlin):** Consumo íntegro de todos los endpoints propios vía Retrofit, manejo explícito de errores de red/HTTP y redirección nativa a WhatsApp mediante `Intent`.

#### Fuera del Alcance (Out-of-Scope)
* Chatbots conversacionales automáticos dentro de WhatsApp Business API.
* Publicación automática en redes sociales (Instagram/Facebook/TikTok); el sistema genera el contenido listo para copiar y compartir.
* Pasarelas de cobro con tarjeta integradas dentro de la aplicación (se incluyen referencias de pago Yape/Plin en la cotización).
* Versión iOS o panel web administrativo.

---

## 2. ARQUITECTURA DEL SISTEMA, PERFIL DE DISEÑO Y STACK GRATUITO

### 2.1 Perfil Técnico del Sistema

| Parámetro | Definición para LeadLaunch | Implicación Arquitectónica |
| :--- | :--- | :--- |
| **Modalidad** | **Híbrida (On-Line + Batch diferido)** | Operaciones transaccionales unitarias en tiempo real (catálogo, cotizaciones, registro de cierre) y procesos diferidos (barrido del radar local, agregación de fricciones y refresco de resúmenes). |
| **Magnitud** | **Multi-negocio (Cloud / Banda Ancha)** | Arquitectura Cliente-Servidor sobre HTTPS con aislamiento lógico por `negocio_id` (*multi-tenant*). |
| **Tiempo de Respuesta** | **Crítico en atención ($< 2\text{ s}$) / Tolerante en IA ($< 10\text{ s}$)** | Consultas de catálogo, cotizaciones y dashboard optimizadas con índices y vista materializada; llamadas al LLM ejecutadas de forma asíncrona con indicadores de carga. |
| **Seguridad** | **Autenticación JWT + Control por Perfil** | Contraseñas cifradas (bcrypt/Argon2), tokens JWT firmados, validación estricta de propiedad del recurso y consultas parametrizadas (prevención de SQL Injection). |
| **Auditoría** | **Transparente vía Bitácora Inmutable** | Todo evento crítico (cotización, cambio de estado, modificación de catálogo) se registra automáticamente en `bitacora_auditoria` mediante triggers de base de datos. |
| **Reversa de Operaciones** | **Transacciones ACID + Snapshot de Estado** | Operaciones compuestas en bloques `BEGIN ... COMMIT / ROLLBACK`; cada cotización guarda un *snapshot* JSON inmutable de los precios y reglas aplicadas. |

### 2.2 Estilo Arquitectónico: SOA RESTful en 4 Capas Desacopladas
Se selecciona una **Arquitectura Orientada a Servicios (SOA) implementada como API RESTful en 4 capas**:
* **Por qué no almacenamiento local aislado (SQLite):** Procesar APIs externas e invocar LLMs desde el dispositivo móvil expone las credenciales (*API Keys*), impide el control centralizado de reglas de negocio, degrada el rendimiento del teléfono y limita la escalabilidad.
* **Por qué no Microservicios ni Bus de Eventos distribuido (Kafka/RabbitMQ):** Dividir un MVP académico en múltiples contenedores con bases de datos independientes introduce complejidad de red, consistencia eventual innecesaria y costos de despliegue que exceden los planes gratuitos. Un backend REST modular en capas ofrece bajo acoplamiento, alta cohesión funcional y despliegue gratuito inmediato.

```text
┌────────────────────────────────────────────────────────────────────────────┐
│ CAPA 0: PRESENTACIÓN (Cliente Móvil - Android Nativo en Kotlin)            │
│ • Pantallas: Login/Perfil, Catálogo/Reglas, Radar Local, Campañas/Cierre   │
│ • Cliente HTTP: Retrofit 2 + OkHttp (JWT Interceptor + Corrutinas)         │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │ HTTPS / REST (JSON + JWT Bearer)
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ CAPA 1: CONTROLADORES / EVENTO DE NEGOCIO (API Routers)                    │
│ • FastAPI (Python) o Express (Node.js) desplegado en Render / Koyeb        │
│ • Validación de DTOs (Pydantic/Zod), Auth JWT, manejo global de errores    │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │ Invocación de Servicios
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ CAPA 2: SERVICIOS DE NEGOCIO (Lógica e Integración Externa)                │
│ • AuthService & BusinessService      • QuoteService (Motor Paramétrico)    │
│ • RadarService (Mapas + Filtro <=3*) • AdaptiveAIService (Gemini Flash)    │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │ SQL Parametrizado (ORM / Driver)
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ CAPA 3: ACCESO A DATOS Y PERSISTENCIA (PostgreSQL Cloud)                   │
│ • Base de Datos Relacional en Neon / Supabase (Free Tier)                  │
│ • Tablas Catálogo + Tablas Parque + Triggers + Vista Materializada         │
└────────────────────────────────────────────────────────────────────────────┘
```

### 2.3 Matriz de Responsabilidades Técnicas (Quién Hace Qué) y Herramientas Gratuitas ($0.00)

| Funcionalidad del Sistema | ¿Quién lo ejecuta? | Herramienta Gratuita | Cómo opera exactamente |
| :--- | :--- | :--- | :--- |
| **Autenticación y Perfil del Negocio** | Backend (Capa 1 y 2) + BD | **FastAPI / Express** (Render/Koyeb) + **PostgreSQL** (Neon/Supabase) | Valida credenciales, cifra contraseñas, emite JWT y persiste el negocio, su política y reglas base en una transacción ACID. |
| **Búsqueda de Competidores Cercanos** | Backend (`RadarService`) | **Overpass API (OpenStreetMap)** / **Google Places API (Free Tier)** + **Dataset JSON Fallback** | El servidor envía coordenadas `(lat, lon)` y `radio_km` para ubicar pastelerías/restaurantes cercanos. Si la API externa falla o agota cuota, activa automáticamente el dataset estructurado de respaldo en el servidor. |
| **Lectura y Filtrado de Reseñas** | Backend (`RadarService` — Código puro) | Código Python/Node en servidor | **No gasta IA en ruido:** El código filtra algorítmicamente el arreglo de reseñas quedándose solo con puntuaciones $\le 3$ estrellas y descartando textos vacíos. |
| **Detección de Brechas y Campañas (Post + Guion 15s)** | Motor LLM invocado desde Backend (`AdaptiveAIService`) | **Google AI Studio (Gemini 1.5 Flash — Free Tier)** | El servidor agrupa las últimas fricciones filtradas + el catálogo del emprendedor en **1 sola llamada HTTPS** a Gemini (protegiendo la `API_KEY` en variables de entorno). Devuelve alerta adaptativa, copy publicitario y guion de video corto. |
| **Cálculo de Cotización y Reglas Comerciales** | Backend (`QuoteService` — Motor Paramétrico) | Código Backend + PostgreSQL | **No usa LLM (evita alucinaciones en precios):** Consulta `cat_politicas_negocio` y `cat_reglas_negocio`, aplica recargo express o descuento de bienvenida, persiste el *snapshot* JSON y ensambla el texto con plantilla parametrizada. |
| **Envío de Cotización por WhatsApp** | Cliente Móvil Android (Capa 0) | **Android Native `Intent.ACTION_VIEW`** | La app recibe la URL `https://wa.me/{numero}?text={mensaje}` desde el servidor y abre WhatsApp con el mensaje precargado listo para enviar en un toque. |

---

## 3. MODELADO CONCEPTUAL DE DATOS (ENFOQUE SEMÁNTICO-ONTOLÓGICO)

El núcleo de datos se construye desde la **Misión del Negocio** para garantizar **robustez semántica** (cobertura sin ambigüedad) y **estabilidad paramétrica** (adaptabilidad mediante tablas de configuración).

### 3.1 Matriz del Parque de Datos (Eventos $\rightarrow$ Operaciones $\rightarrow$ Métodos)

| Macro-Función | Macro-Evento | Operación de Negocio | Método (Acceso a Datos) | Atributos Clave Impactados |
| :--- | :--- | :--- | :--- | :--- |
| **Definición** | `REGISTRO_NEGOCIO` | Alta de Emprendimiento y Parámetros | `crear_negocio()`, `crear_politica_base()`, `asignar_reglas_base()` | `negocio_id`, `razon_comercial`, `rubro_id`, `latitud`, `longitud`, `radio_cobertura_km`, `whatsapp_numero` |
| **Definición** | `GESTION_CATALOGO` | Alta/Cambio de Producto Gastronómico | `upsert_producto()`, `registrar_auditoria()` | `producto_id`, `sku`, `precio_base`, `tiempo_prep_horas`, `stock_disponible`, `permite_express` |
| **Ingesta (Radar)** | `ESCANEO_RADAR` | Extracción y Filtrado de Fricciones | `crear_escaneo()`, `upsert_competidor()`, `insertar_hallazgo()` | `escaneo_id`, `fuente_datos`, `calificacion`, `texto_resena`, `tipo_friccion_id`, `resumen_dolor` |
| **Atracción** | `GENERACION_CAMPANA` | Síntesis Adaptativa de Oferta y Copies | `consultar_hallazgos()`, `consultar_catalogo()`, `guardar_campana()` | `campana_id`, `alerta_adaptativa`, `post_redes`, `guion_reel_15s`, `estado` |
| **Venta / Cierre** | `EMISION_COTIZACION` | Liquidación Paramétrica de Pedido | `evaluar_politicas_reglas()`, `crear_cotizacion()`, `acumular_cotizaciones()` | `cotizacion_id`, `subtotal`, `monto_recargo`, `monto_descuento`, `total_neto`, `snapshot_reglas_json` |
| **Entrega / Cierre** | `ACTUALIZACION_CIERRE` | Cambio de Estado del Prospecto | `actualizar_estado()`, `acumular_ventas_ganadas()`, `registrar_auditoria()` | `estado`, `total_ventas_ganadas`, `monto_ventas_ganadas` |

### 3.2 Diccionario Semántico-Ontológico (Atributos Nucleares)

| Atributo | Naturaleza | Formato | Dominio / Valores | Semántica (Qué es) | Ontología de Negocio (Para qué sirve) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `negocio_id` | Código (IU) | `UUID` | Único, `NOT NULL` | Identificador universal del emprendimiento. | Garantiza aislamiento *multi-tenant* y actúa como raíz de la Entidad Cuenta. |
| `rubro_id` | Parámetro | `SMALLINT` | FK `cat_rubros` | Subcategoría gastronómica del emprendimiento. | Especializa el contexto del radar y permite escalar a otros rubros sin cambiar código. |
| `radio_cobertura_km` | Parámetro | `NUMERIC(4,2)` | `0.50` a `25.00` | Distancia radial en kilómetros alrededor del negocio. | Acota dinámicamente la búsqueda competitiva local. |
| `tiempo_prep_horas` | Numérico | `SMALLINT` | `>= 1` | Horas requeridas para hornear o preparar el producto. | Permite contrastar la capacidad operativa contra quejas de demora de la competencia. |
| `tipo_regla` | Parámetro | `VARCHAR(30)` | `RECARGO_EXPRESS`, `DESC_BIENVENIDA`, `DESC_VOLUMEN` | Categoría normativa de liquidación. | Externaliza las reglas comerciales a la base de datos evitando valores fijos en código. |
| `calificacion` | Numérico | `SMALLINT` | `1` a `3` | Puntuación crítica de una reseña de competidor. | Criterio de filtrado objetivo para capturar únicamente fricciones explotables. |
| `monto_ventas_ganadas` | Acumulador | `NUMERIC(12,2)` | `>= 0.00` | Suma neta de cotizaciones cerradas en estado `GANADA`. | Mide cuantitativamente el incremento de valor económico generado por la aplicación. |
| `snapshot_reglas_json` | Evidencia | `JSONB` | JSON válido | Fotografía inmutable de precios y reglas al cotizar. | Garantiza auditoría y consistencia histórica aunque el catálogo cambie posteriormente. |

### 3.3 Separación Estructural: Catálogo vs. Parque (4NF)
1. **Entidades de Catálogo (Estáticas y Normativas):** `cat_rubros`, `cat_tipos_friccion`, `cat_politicas_negocio`, `cat_reglas_negocio`.
2. **Entidades de Parque — Pasivas y Cuentas de Comportamiento:** `usuarios`, `negocios` (Entidad Cuenta con acumuladores de comportamiento), `productos`, `competidores_locales`.
3. **Entidades de Parque — Ejecutivas (Eventos y Transacciones):** `eventos_escaneo_radar`, `hallazgos_friccion`, `campanas_adaptativas`, `cotizaciones_cierre`, `cotizacion_detalles`, `bitacora_auditoria`.

---

## 4. DISEÑO LÓGICO, FÍSICO Y OPTIMIZACIÓN (POSTGRESQL)

### 4.1 Optimización de la Consulta Crítica (Desnormalización Controlada)
* **Consulta Crítica:** La pantalla principal (*Dashboard*) consolida datos del negocio, productos activos, fricciones detectadas, última alerta adaptativa y tasa de conversión (cruzando 6 tablas).
* **Solución:** Se implementa la **Vista Materializada `mv_resumen_comercial_negocio`** con índice único sobre `negocio_id`. Reduce el costo de la vista a **1 solo acceso indexado** ($< 500\text{ ms}$) y se actualiza de forma concurrente sin bloquear lecturas.

### 4.2 Decisiones de Almacenamiento Físico e Integridad
* **Control de Espacio Libre (`FILLFACTOR`):** `FILLFACTOR = 80` en tablas con actualizaciones frecuentes (`negocios`, `productos`, `cotizaciones_cierre`) para prevenir fragmentación de páginas (*page splits*); `FILLFACTOR = 100` en tablas de solo inserción (`hallazgos_friccion`, `bitacora_auditoria`).
* **Índices Compuestos (Árbol-B):** Definidos en orden `(negocio_id, creado_en DESC)` para acelerar consultas filtradas por negocio y ordenadas cronológicamente.
* **Integridad Referencial Segura:** `ON DELETE RESTRICT` en llaves foráneas para impedir borrados en cascada accidentales; se aplica borrado lógico (`activo = FALSE`).
* **Reglas Activas (Triggers):** Mantenimiento automático y atómico de los acumuladores de comportamiento en `negocios` (incluyendo reversa si una venta `GANADA` cambia de estado) y registro automático en `bitacora_auditoria`.

### 4.3 Script DDL Completo (PostgreSQL 15+)

```sql
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

-- ----------------------------------------------------------------------------
-- 7. CARGA SEMILLA PARAMÉTRICA (VERTICAL GASTRONOMÍA Y REPOSTERÍA)
-- ----------------------------------------------------------------------------

INSERT INTO cat_rubros (codigo, nombre, sector) VALUES
('REPOSTERIA',    'Repostería y Pastelería Artesanal', 'GASTRONOMIA'),
('COMIDA_RAPIDA', 'Comida Rápida y Bocaditos',         'GASTRONOMIA'),
('CAFETERIA',     'Cafeterías y Postres',              'GASTRONOMIA');

INSERT INTO cat_tipos_friccion (codigo, nombre, descripcion) VALUES
('DEMORA_ENTREGA',        'Demora en delivery/entrega',   'Retrasos o incumplimiento en la hora pactada de entrega'),
('CALIDAD_PRESENTACION',  'Presentación o frescura',      'Pedidos mal empacados, dañados o con baja frescura'),
('PRECIO_DELIVERY',       'Precio o envío elevado',       'Percepción de costo alto o tarifa de reparto excesiva'),
('MALA_ATENCION',         'Mala atención al cliente',     'Demoras en responder mensajes o trato deficiente'),
('SIN_OPCIONES_URGENTES', 'Sin atención de último minuto','Falta de combos express o pedidos para el mismo día');
```

---

## 5. ESPECIFICACIÓN DE REQUERIMIENTOS (ESTÁNDAR IEEE 830)

### 5.1 Requerimientos Funcionales (RF)
* **RF-01 Autenticación y Gestión de Negocio Gastronómico:** El sistema debe permitir registrar usuarios con JWT y configurar el perfil del emprendimiento (subcategoría gastronómica, distrito, coordenadas, radio de cobertura y WhatsApp) en PostgreSQL.
* **RF-02 Administración del Catálogo de Productos:** El emprendedor debe poder crear, listar y desactivar productos de su menú especificando `sku`, `precio_base`, `tiempo_prep_horas`, `stock_disponible` y disponibilidad express.
* **RF-03 Configuración Paramétrica de Políticas y Reglas:** El emprendedor debe poder habilitar o ajustar sus reglas comerciales (`RECARGO_EXPRESS`, `DESC_BIENVENIDA`, `DESC_VOLUMEN`) desde la app sin requerir cambios de código.
* **RF-04 Radar Local de Competidores y Fricciones:** El backend debe consultar negocios gastronómicos en el radio geográfico (o activar el respaldo controlado del servidor), filtrar algorítmicamente reseñas $\le 3$ estrellas, clasificar cada queja según `cat_tipos_friccion` y persistir los hallazgos.
* **RF-05 Generación Reactiva de Campañas y Alertas Adaptativas:** El backend debe consolidar las fricciones detectadas y el catálogo activo para invocar Gemini 1.5 Flash en una única petición, persistiendo y devolviendo: (a) alerta de ajuste de oferta, (b) copy para redes sociales y (c) guion de video corto de 15 segundos.
* **RF-06 Liquidación Paramétrica de Cotizaciones:** El backend debe validar stock/políticas, calcular `subtotal`, `monto_recargo`, `monto_descuento` y `total_neto` según reglas vigentes, guardar el *snapshot* JSON y generar el mensaje y URL prellenada de WhatsApp.
* **RF-07 Disparo a WhatsApp y Seguimiento de Conversión:** La app Android debe abrir WhatsApp vía `Intent` con la cotización generada y permitir actualizar el estado del prospecto (`ENVIADA_WA`, `GANADA`, `PERDIDA`).
* **RF-08 Dashboard de Rendimiento Comercial:** La app debe consultar la vista materializada del servidor para mostrar métricas en tiempo real (productos activos, fricciones detectadas, cotizaciones, ventas ganadas, tasa de conversión e ingresos acumulados).

### 5.2 Requerimientos No Funcionales (RNF)
* **RNF-01 Arquitectura en Capas e Integridad:** Separación estricta en 4 capas (Presentación, Controladores, Servicios, Acceso a Datos) e integridad referencial con `ON DELETE RESTRICT` y restricciones `CHECK`.
* **RNF-02 Seguridad y Validación de Entradas:** Contraseñas con hash seguro, endpoints protegidos con JWT, aislamiento *multi-tenant* por `usuario_id`/`negocio_id`, consultas parametrizadas contra SQL Injection y claves externas (Gemini/Mapas) resguardadas exclusivamente en variables de entorno del servidor.
* **RNF-03 Rendimiento y Asincronía:** Consultas del dashboard $< 500\text{ ms}$ mediante vista materializada; llamadas HTTP en Android ejecutadas en `Dispatchers.IO` con manejo explícito de códigos HTTP (`400`, `401`, `404`, `422`, `500`) y *timeouts*.
* **RNF-04 Resiliencia en Demostración:** Mecanismo de respaldo controlado en el servidor ante fallos o límites de cuota del proveedor de mapas externos.
* **RNF-05 Documentación y Pruebas Verificables:** Especificación OpenAPI/Swagger publicada y ejecución documentada de al menos 5 casos de prueba de éxito y 5 casos de falla/seguridad.
* **RNF-06 Costo Operativo Cero:** Toda la infraestructura (Render/Koyeb, Neon/Supabase, Gemini Free Tier, OpenStreetMap) debe operar en modalidad gratuita ($0.00 USD).

---

## 6. CONTRATOS DE LA API REST (11 ENDPOINTS PROPIOS)

| # | Método | Ruta (Endpoint) | Servicio | Descripción y Validaciones Clave |
| :--- | :--- | :--- | :--- | :--- |
| 1 | `POST` | `/api/v1/auth/register` | `AuthService` | Valida formato de correo único, cifra contraseña y retorna JWT (`201`). |
| 2 | `POST` | `/api/v1/auth/login` | `AuthService` | Valida credenciales y retorna JWT Bearer (`200` / `401`). |
| 3 | `POST` | `/api/v1/businesses` | `BusinessService` | Crea negocio gastronómico + política por defecto + reglas base en transacción ACID (`201`). |
| 4 | `GET` | `/api/v1/businesses/{id}/dashboard` | `BusinessService` | Consulta crítica desde `mv_resumen_comercial_negocio` (`200`). |
| 5 | `POST` | `/api/v1/businesses/{id}/products` | `CatalogService` | Registra producto validando `precio_base > 0`, `tiempo_prep_horas >= 1` y `sku` único (`201` / `422`). |
| 6 | `GET` | `/api/v1/businesses/{id}/products` | `CatalogService` | Lista menú activo del emprendimiento (`200`). |
| 7 | `PUT` | `/api/v1/businesses/{id}/rules` | `PolicyService` | Actualiza parámetros de políticas y reglas comerciales vigentes (`200`). |
| 8 | `POST` | `/api/v1/businesses/{id}/radar/scan` | `RadarService` | **Lógica Compleja 1:** Consulta competidores gastronómicos (o fallback), filtra reseñas $\le 3$ estrellas, clasifica fricciones y persiste escaneo (`201`). |
| 9 | `POST` | `/api/v1/businesses/{id}/campaigns/generate` | `AdaptiveAIService` | **Lógica Compleja 2:** Cruza fricciones + catálogo vía Gemini 1.5 Flash y persiste alerta, post y guion de 15s (`201`). |
| 10 | `POST` | `/api/v1/businesses/{id}/quotes` | `QuoteService` | **Lógica Compleja 3:** Evalúa políticas/reglas vigentes, liquida totales, guarda *snapshot* JSON y retorna URL `wa.me` (`201` / `400`). |
| 11 | `PATCH` | `/api/v1/quotes/{quote_id}/status` | `QuoteService` | Actualiza estado (`ENVIADA_WA`, `GANADA`, `PERDIDA`), dispara triggers de cuenta/auditoría y refresca vista (`200`). |

---

## 7. GESTIÓN ÁGIL (SCRUM), PRUEBAS Y REPARTO DEL EQUIPO

### 7.1 Product Backlog Priorizado (3 Sprints de 1 Semana)
Estimación consensuada mediante escala Fibonacci (ninguna tarea técnica individual excede 16 horas).

| ID | Sprint | Historia de Usuario | Puntos | Criterios de Aceptación (Gherkin) |
| :--- | :--- | :--- | :---: | :--- |
| **HU-01** | Sprint 1 | **Como** emprendedor gastronómico, **quiero** registrar mi cuenta, negocio y catálogo de productos en la nube **para** gestionar mi menú de forma centralizada. | 8 | **Dado** un usuario autenticado con JWT, **cuando** registra un producto con precio $> 0$ y SKU único, **entonces** la API responde `201 Created` y persiste en PostgreSQL; si el precio es $\le 0$, responde `422`. |
| **HU-02** | Sprint 1 | **Como** emprendedor, **quiero** configurar mis políticas y reglas de recargo express y descuentos **para** adaptar mis precios sin modificar código. | 5 | **Dado** un negocio activo, **cuando** actualiza `RECARGO_EXPRESS` al $15\%$, **entonces** la regla se persiste en `cat_reglas_negocio` y aplica solo a nuevas cotizaciones. |
| **HU-03** | Sprint 2 | **Como** emprendedor, **quiero** escanear competidores gastronómicos en mi radio de cobertura **para** identificar las quejas reales ($\le 3$ estrellas) de mi distrito. | 13 | **Dado** un negocio con coordenadas válidas, **cuando** ejecuta `/radar/scan`, **entonces** el backend filtra reseñas $\le 3$ estrellas (usando API de mapas o fallback controlado), las clasifica en `hallazgos_friccion` y responde `201`. |
| **HU-04** | Sprint 2 | **Como** emprendedor, **quiero** generar una campaña adaptativa basada en los hallazgos del radar **para** obtener una alerta de ajuste de oferta, un post y un guion de video corto. | 8 | **Dado** al menos 1 fricción registrada, **cuando** invoca `/campaigns/generate`, **entonces** el servidor consulta Gemini 1.5 Flash en 1 llamada y persiste las 3 secciones en `campanas_adaptativas`. |
| **HU-05** | Sprint 3 | **Como** emprendedor, **quiero** emitir una cotización parametrizada y enviarla por WhatsApp en un toque **para** cerrar ventas rápido sin errores de cálculo. | 8 | **Dado** un pedido con entrega express habilitada, **cuando** se llama a `/quotes`, **entonces** el servidor liquida el total con las reglas vigentes, guarda `snapshot_reglas_json` y Android abre WhatsApp con el texto precargado. |
| **HU-06** | Sprint 3 | **Como** emprendedor, **quiero** actualizar el estado de mis cotizaciones y visualizar mi dashboard **para** medir mi tasa de conversión e ingresos reales. | 5 | **Dado** una cotización en estado `ENVIADA_WA`, **cuando** pasa a `GANADA`, **entonces** el trigger actualiza `total_ventas_ganadas` y `monto_ventas_ganadas` en `negocios` y el dashboard responde en $< 500\text{ ms}$. |

### 7.2 Matriz de Pruebas Obligatorias (5 Casos de Éxito y 5 de Falla)

| ID | Tipo | Caso de Prueba | Entrada / Escenario | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CP-01** | Éxito | Registro y Login JWT | Credenciales válidas en `/auth/register` y `/auth/login` | HTTP `201` / `200` + JWT firmado válido. |
| **CP-02** | Éxito | Alta de Producto Gastronómico | `nombre = 'Torta Temática 15 porciones'`, `precio_base = 95.00` | HTTP `201` + fila insertada en `productos`. |
| **CP-03** | Éxito | Escaneo de Radar Local | `POST /radar/scan` con radio `3.0 km` en rubro `REPOSTERIA` | HTTP `201` + hallazgos $\le 3$ estrellas persistidos y `total_escaneos + 1`. |
| **CP-04** | Éxito | Campaña Adaptativa IA | `POST /campaigns/generate` tras escaneo completado | HTTP `201` + JSON con `alerta_adaptativa`, `post_redes` y `guion_reel_15s`. |
| **CP-05** | Éxito | Cotización + Cierre Ganado | Cotización express + `PATCH /status` a `GANADA` | Cálculo exacto de `total_neto` + actualización automática de saldos y `bitacora_auditoria`. |
| **CP-06** | Falla | Acceso sin Token JWT | `GET /businesses/{id}/products` sin header `Authorization` | HTTP `401 Unauthorized`. |
| **CP-07** | Falla | Integridad de Dominio | Producto con `precio_base = -20.00` o `whatsapp_numero = 'abc'` | HTTP `422 Unprocessable Entity` rechazado por validación de esquema. |
| **CP-08** | Falla | Inyección SQL | `nombre_prospecto = "'; DROP TABLE negocios; --"` | Neutralizado por consultas parametrizadas; integridad intacta. |
| **CP-09** | Falla | Violación de Política | Solicitar `es_entrega_express = true` con `permite_express = false` | HTTP `400 Bad Request` indicando restricción de política comercial. |
| **CP-10** | Falla | Aislamiento Multi-Tenant | `PATCH /quotes/{uuid_ajeno}/status` con token de otro emprendedor | HTTP `404 Not Found` (bloqueo de acceso cruzado). |

### 7.3 Distribución de Roles en el Equipo (Trabajo en Paralelo + CI)
Flujo de control de versiones en GitHub con rama principal `main`, ramas por historia `feature/HU-XX` y revisión mediante Pull Requests:
1. **Integrante 1 (Base de Datos, Despliegue Cloud y QA):** Ejecuta el DDL en Neon/Supabase, configura el despliegue continuo en Render/Koyeb, mantiene la documentación Swagger/OpenAPI y ejecuta la matriz de 10 casos de prueba.
2. **Integrante 2 (Backend API REST — Capas 1 y 2):** Implementa los controladores, autenticación JWT, CRUD de negocio/menú/reglas, el servicio de Radar con respaldo y la integración segura con Gemini 1.5 Flash.
3. **Integrante 3 (Cliente Móvil Android — Capa 0 y Product Owner):** Implementa las pantallas en Kotlin, configura Retrofit con interceptor JWT, consume todos los endpoints propios, maneja los estados de error/carga y programa el disparo a WhatsApp vía `Intent`.