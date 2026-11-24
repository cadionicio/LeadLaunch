"""Capa 3: acceso a productos."""
from uuid import UUID

from psycopg import Connection

_COLUMNAS = """producto_id, sku, nombre, descripcion, precio_base::float AS precio_base, tiempo_prep_horas,
               stock_disponible, permite_express, activo, creado_en"""


def sku_existe(conn: Connection, negocio_id: UUID, sku: str) -> bool:
    fila = conn.execute("SELECT 1 FROM productos WHERE negocio_id = %s AND sku = %s", (negocio_id, sku)).fetchone()
    return fila is not None


def crear(conn: Connection, negocio_id: UUID, datos: dict) -> dict:
    return conn.execute(
        f"""INSERT INTO productos (negocio_id, sku, nombre, descripcion, precio_base, tiempo_prep_horas,
                                   stock_disponible, permite_express)
            VALUES (%(negocio_id)s, %(sku)s, %(nombre)s, %(descripcion)s, round(%(precio_base)s::numeric, 2),
                    %(tiempo_prep_horas)s, %(stock_disponible)s, %(permite_express)s)
            RETURNING {_COLUMNAS}""",
        {**datos, "negocio_id": negocio_id},
    ).fetchone()


def listar_activos(conn: Connection, negocio_id: UUID, limite: int, desplazamiento: int) -> tuple[list[dict], int]:
    total = conn.execute(
        "SELECT count(*) AS n FROM productos WHERE negocio_id = %s AND activo = TRUE", (negocio_id,)
    ).fetchone()["n"]
    filas = conn.execute(
        f"""SELECT {_COLUMNAS} FROM productos
             WHERE negocio_id = %s AND activo = TRUE
             ORDER BY nombre, sku
             LIMIT %s OFFSET %s""",
        (negocio_id, limite, desplazamiento),
    ).fetchall()
    return filas, total


def listar_para_ia(conn: Connection, negocio_id: UUID) -> list[dict]:
    return conn.execute(
        f"SELECT {_COLUMNAS} FROM productos WHERE negocio_id = %s AND activo = TRUE ORDER BY nombre",
        (negocio_id,),
    ).fetchall()
