"""Capa 3: escaneos, competidores y hallazgos del radar."""
from uuid import UUID

from psycopg import Connection


def rubro_codigo(conn: Connection, rubro_id: int) -> str:
    return conn.execute("SELECT codigo FROM cat_rubros WHERE rubro_id = %s", (rubro_id,)).fetchone()["codigo"]


def tipos_friccion(conn: Connection) -> dict[str, dict]:
    filas = conn.execute("SELECT tipo_friccion_id, codigo, nombre FROM cat_tipos_friccion").fetchall()
    return {f["codigo"]: f for f in filas}


def crear_escaneo(conn: Connection, negocio_id: UUID, fuente: str, radio_km: float,
                  total_analizados: int, total_fricciones: int) -> dict:
    # Se inserta ya COMPLETADO: el trigger trg_escaneo_comportamiento suma total_escaneos en negocios.
    return conn.execute(
        """INSERT INTO eventos_escaneo_radar (negocio_id, fuente_datos, radio_busqueda_km,
                                              total_analizados, total_fricciones, estado)
           VALUES (%s, %s, round(%s::numeric, 2), %s, %s, 'COMPLETADO')
           RETURNING escaneo_id, ejecutado_en""",
        (negocio_id, fuente, radio_km, total_analizados, total_fricciones),
    ).fetchone()


def upsert_competidor(conn: Connection, fuente_externa_id: str, rubro_id: int, nombre: str, distrito: str,
                      latitud: float, longitud: float, calificacion_prom: float | None) -> UUID:
    return conn.execute(
        """INSERT INTO competidores_locales (fuente_externa_id, rubro_id, nombre, distrito, latitud, longitud,
                                             calificacion_prom)
           VALUES (%s, %s, %s, %s, %s, %s, %s)
           ON CONFLICT (fuente_externa_id) DO UPDATE
              SET nombre = EXCLUDED.nombre, distrito = EXCLUDED.distrito, latitud = EXCLUDED.latitud,
                  longitud = EXCLUDED.longitud, calificacion_prom = EXCLUDED.calificacion_prom,
                  actualizado_en = NOW()
           RETURNING competidor_id""",
        (fuente_externa_id, rubro_id, nombre, distrito, latitud, longitud, calificacion_prom),
    ).fetchone()["competidor_id"]


def hallazgo_existe(conn: Connection, negocio_id: UUID, competidor_id: UUID, texto: str) -> bool:
    return conn.execute(
        """SELECT 1 FROM hallazgos_friccion
            WHERE negocio_id = %s AND competidor_id = %s AND texto_resena = %s LIMIT 1""",
        (negocio_id, competidor_id, texto),
    ).fetchone() is not None


def insertar_hallazgo(conn: Connection, escaneo_id: UUID, negocio_id: UUID, competidor_id: UUID,
                      tipo_friccion_id: int, calificacion: int, texto: str, resumen: str) -> None:
    conn.execute(
        """INSERT INTO hallazgos_friccion (escaneo_id, negocio_id, competidor_id, tipo_friccion_id,
                                           calificacion, texto_resena, resumen_dolor)
           VALUES (%s, %s, %s, %s, %s, %s, %s)""",
        (escaneo_id, negocio_id, competidor_id, tipo_friccion_id, calificacion, texto, resumen),
    )
