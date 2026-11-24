"""Rutas de catálogo, reglas y dashboard, todas bajo /businesses/{negocio_id}."""
from uuid import UUID

from fastapi import APIRouter, Depends, Query, status

from app.core.security import get_current_user_id
from app.schemas.dashboard import DashboardOut
from app.schemas.product import ProductIn, ProductOut, ProductPage
from app.schemas.rules import RulesIn, RulesOut
from app.services import catalog_service, dashboard_service, policy_service

router = APIRouter(prefix="/businesses/{negocio_id}")


@router.get("/dashboard", response_model=DashboardOut, tags=["Dashboard"], summary="Resumen comercial (S05)")
def dashboard(negocio_id: UUID, usuario_id: UUID = Depends(get_current_user_id)):
    return dashboard_service.obtener_dashboard(usuario_id, negocio_id)


@router.post("/products", response_model=ProductOut, status_code=status.HTTP_201_CREATED,
             tags=["Catálogo"], summary="Agregar producto (S07)")
def crear_producto(negocio_id: UUID, datos: ProductIn, usuario_id: UUID = Depends(get_current_user_id)):
    """SKU único por negocio (se guarda en mayúsculas). Duplicado → 422 `SKU_DUPLICADO`."""
    return catalog_service.crear_producto(usuario_id, negocio_id, datos)


@router.get("/products", response_model=ProductPage, tags=["Catálogo"], summary="Listar catálogo activo (S06)")
def listar_productos(
    negocio_id: UUID,
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    usuario_id: UUID = Depends(get_current_user_id),
):
    return catalog_service.listar_productos(usuario_id, negocio_id, page, size)


@router.get("/rules", response_model=RulesOut, tags=["Reglas"], summary="Ver políticas y reglas (S08)")
def obtener_reglas(negocio_id: UUID, usuario_id: UUID = Depends(get_current_user_id)):
    return policy_service.obtener_reglas(usuario_id, negocio_id)


@router.put("/rules", response_model=RulesOut, tags=["Reglas"], summary="Actualizar políticas y reglas (S08)")
def actualizar_reglas(negocio_id: UUID, datos: RulesIn, usuario_id: UUID = Depends(get_current_user_id)):
    """Aplica solo a cotizaciones nuevas. Los tipos de regla que no envíes quedan como estaban."""
    return policy_service.actualizar_reglas(usuario_id, negocio_id, datos)
