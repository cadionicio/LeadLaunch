"""Capa 2: CatalogService — productos del menú."""
from uuid import UUID

from psycopg import errors as pg_errors

from app.core.database import get_conn
from app.core.errors import AppError
from app.repositories import negocios_repo, productos_repo
from app.schemas.product import ProductIn, ProductOut, ProductPage
from app.services.business_service import verificar_propiedad


def _sku_duplicado() -> AppError:
    return AppError(422, "SKU_DUPLICADO", "Ya existe un producto con ese SKU.", ["sku"])


def crear_producto(usuario_id: UUID, negocio_id: UUID, datos: ProductIn) -> ProductOut:
    try:
        with get_conn() as conn:
            verificar_propiedad(conn, negocio_id, usuario_id)
            if productos_repo.sku_existe(conn, negocio_id, datos.sku):
                raise _sku_duplicado()
            producto = productos_repo.crear(conn, negocio_id, datos.model_dump())
    except pg_errors.UniqueViolation:
        raise _sku_duplicado()

    with get_conn() as conn:
        negocios_repo.refrescar_resumen(conn)
    return ProductOut(**producto)


def listar_productos(usuario_id: UUID, negocio_id: UUID, page: int, size: int) -> ProductPage:
    with get_conn() as conn:
        verificar_propiedad(conn, negocio_id, usuario_id)
        filas, total = productos_repo.listar_activos(conn, negocio_id, size, (page - 1) * size)
    return ProductPage(items=[ProductOut(**f) for f in filas], page=page, size=size, total=total)
