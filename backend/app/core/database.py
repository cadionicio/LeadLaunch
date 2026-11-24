"""Capa 3: pool de conexiones a PostgreSQL (Neon).

Uso en un repositorio:

    with get_conn() as conn:
        conn.execute("SELECT * FROM productos WHERE negocio_id = %s", (negocio_id,))

Al salir del bloque `with` se hace COMMIT; si ocurre una excepción, ROLLBACK.
Siempre usar parámetros (%s), nunca f-strings con datos del usuario.
"""
from contextlib import contextmanager
from typing import Iterator

from psycopg import Connection
from psycopg.rows import dict_row
from psycopg_pool import ConnectionPool

from app.core.config import settings

_pool: ConnectionPool | None = None


def get_pool() -> ConnectionPool:
    global _pool
    if _pool is None:
        if not settings.database_url:
            raise RuntimeError("DATABASE_URL no está configurada")
        _pool = ConnectionPool(
            conninfo=settings.database_url,
            min_size=1,
            max_size=5,
            kwargs={"row_factory": dict_row},
            open=True,
        )
    return _pool


@contextmanager
def get_conn() -> Iterator[Connection]:
    with get_pool().connection() as conn:
        yield conn


def close_pool() -> None:
    global _pool
    if _pool is not None:
        _pool.close()
        _pool = None
