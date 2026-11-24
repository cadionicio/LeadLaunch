"""Capa 3: acceso a negocios, políticas y reglas base."""
from uuid import UUID

from psycopg import Connection

_COLUMNAS = """n.negocio_id, n.usuario_id, n.rubro_id, r.nombre AS rubro_nombre, n.razon_comercial,
               n.descripcion, n.whatsapp_numero, n.distrito, n.latitud::float AS latitud,
               n.longitud::float AS longitud, n.radio_cobertura_km::float AS radio_cobertura_km,
               n.activo, n.creado_en"""

# Reglas que se crean junto con el negocio (decisión del plan: recargo 15 %, bienvenida 10 %).
REGLAS_BASE = [
    ("RECARGO_EXPRESS", "Recargo por preparación prioritaria", 15.00),
    ("DESC_BIENVENIDA", "Descuento para clientes nuevos", 10.00),
]


def rubro_activo(conn: Connection, rubro_id: int) -> bool:
    fila = conn.execute("SELECT 1 FROM cat_rubros WHERE rubro_id = %s AND activo = TRUE", (rubro_id,)).fetchone()
    return fila is not None


def obtener_id_activo_por_usuario(conn: Connection, usuario_id: UUID) -> UUID | None:
    fila = conn.execute(
        """SELECT negocio_id FROM negocios
            WHERE usuario_id = %s AND activo = TRUE
            ORDER BY creado_en LIMIT 1""",
        (usuario_id,),
    ).fetchone()
    return fila["negocio_id"] if fila else None


def obtener_de_usuario(conn: Connection, negocio_id: UUID, usuario_id: UUID) -> dict | None:
    """Devuelve el negocio solo si pertenece al usuario (aislamiento multi-tenant)."""
    return conn.execute(
        f"""SELECT {_COLUMNAS}
              FROM negocios n JOIN cat_rubros r ON r.rubro_id = n.rubro_id
             WHERE n.negocio_id = %s AND n.usuario_id = %s AND n.activo = TRUE""",
        (negocio_id, usuario_id),
    ).fetchone()


def crear(conn: Connection, usuario_id: UUID, datos: dict) -> UUID:
    fila = conn.execute(
        """INSERT INTO negocios (usuario_id, rubro_id, razon_comercial, descripcion, whatsapp_numero,
                                 distrito, latitud, longitud, radio_cobertura_km)
           VALUES (%(usuario_id)s, %(rubro_id)s, %(razon_comercial)s, %(descripcion)s, %(whatsapp_numero)s,
                   %(distrito)s, round(%(latitud)s::numeric, 6), round(%(longitud)s::numeric, 6),
                   round(%(radio_cobertura_km)s::numeric, 2))
           RETURNING negocio_id""",
        {**datos, "usuario_id": usuario_id},
    ).fetchone()
    return fila["negocio_id"]


def crear_politica_base(conn: Connection, negocio_id: UUID) -> None:
    conn.execute("INSERT INTO cat_politicas_negocio (negocio_id) VALUES (%s)", (negocio_id,))


def crear_reglas_base(conn: Connection, negocio_id: UUID) -> None:
    with conn.cursor() as cur:
        cur.executemany(
            """INSERT INTO cat_reglas_negocio (negocio_id, tipo_regla, descripcion, valor_porcentaje)
               VALUES (%s, %s, %s, %s)""",
            [(negocio_id, tipo, desc, valor) for tipo, desc, valor in REGLAS_BASE],
        )


def refrescar_resumen(conn: Connection) -> None:
    """Refresca la vista materializada del dashboard sin bloquear lecturas."""
    conn.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_resumen_comercial_negocio")
