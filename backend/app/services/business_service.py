"""Capa 2: BusinessService — alta de negocio y verificación de propiedad."""
from uuid import UUID

from psycopg import Connection

from app.core.database import get_conn
from app.core.errors import AppError
from app.repositories import negocios_repo, usuarios_repo
from app.schemas.business import BusinessIn, BusinessOut


def _no_encontrado() -> AppError:
    return AppError(404, "NEGOCIO_NO_ENCONTRADO", "No encontramos este negocio.")


def crear_negocio(usuario_id: UUID, datos: BusinessIn) -> BusinessOut:
    """Crea negocio + política por defecto + reglas base en UNA transacción (todo o nada)."""
    with get_conn() as conn:
        if usuarios_repo.obtener_por_id(conn, usuario_id) is None:
            raise AppError(401, "NO_AUTENTICADO", "Tu sesión no es válida. Inicia sesión nuevamente.")
        if not negocios_repo.rubro_activo(conn, datos.rubro_id):
            raise AppError(422, "RUBRO_INVALIDO", "La categoría seleccionada no existe.", ["rubro_id"])
        # MVP: un negocio por cuenta (multi-negocio queda pendiente, P13).
        if negocios_repo.obtener_id_activo_por_usuario(conn, usuario_id):
            raise AppError(400, "NEGOCIO_EXISTENTE", "Tu cuenta ya tiene un negocio registrado.")

        negocio_id = negocios_repo.crear(conn, usuario_id, datos.model_dump())
        negocios_repo.crear_politica_base(conn, negocio_id)
        negocios_repo.crear_reglas_base(conn, negocio_id)
        negocio = negocios_repo.obtener_de_usuario(conn, negocio_id, usuario_id)

    # Fuera de la transacción: la vista se refresca con el negocio ya confirmado.
    with get_conn() as conn:
        negocios_repo.refrescar_resumen(conn)

    return BusinessOut(**negocio)


def obtener_negocio(usuario_id: UUID, negocio_id: UUID) -> BusinessOut:
    with get_conn() as conn:
        negocio = verificar_propiedad(conn, negocio_id, usuario_id)
    return BusinessOut(**negocio)


def verificar_propiedad(conn: Connection, negocio_id: UUID, usuario_id: UUID) -> dict:
    """Úsalo en todos los endpoints /businesses/{id}/...: si el negocio no es del usuario → 404 (CP-10)."""
    negocio = negocios_repo.obtener_de_usuario(conn, negocio_id, usuario_id)
    if negocio is None:
        raise _no_encontrado()
    return negocio
