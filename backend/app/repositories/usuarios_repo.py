"""Capa 3: acceso a la tabla usuarios."""
from uuid import UUID

from psycopg import Connection


def obtener_por_correo(conn: Connection, correo: str) -> dict | None:
    return conn.execute(
        """SELECT usuario_id, correo, password_hash, nombre_completo, activo
             FROM usuarios WHERE correo = %s""",
        (correo,),
    ).fetchone()


def crear(conn: Connection, correo: str, password_hash: str, nombre_completo: str) -> dict:
    return conn.execute(
        """INSERT INTO usuarios (correo, password_hash, nombre_completo)
           VALUES (%s, %s, %s)
           RETURNING usuario_id, correo, nombre_completo""",
        (correo, password_hash, nombre_completo),
    ).fetchone()


def obtener_por_id(conn: Connection, usuario_id: UUID) -> dict | None:
    return conn.execute(
        "SELECT usuario_id, correo, nombre_completo, activo FROM usuarios WHERE usuario_id = %s",
        (usuario_id,),
    ).fetchone()
