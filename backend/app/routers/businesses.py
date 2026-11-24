from uuid import UUID

from fastapi import APIRouter, Depends, status

from app.core.security import get_current_user_id
from app.schemas.business import BusinessIn, BusinessOut
from app.services import business_service

router = APIRouter(prefix="/businesses", tags=["Negocio"])


@router.post("", response_model=BusinessOut, status_code=status.HTTP_201_CREATED,
             summary="Registrar negocio (S04)")
def crear_negocio(datos: BusinessIn, usuario_id: UUID = Depends(get_current_user_id)):
    """Crea el negocio con su política por defecto y reglas base (recargo express 15 %, bienvenida 10 %)."""
    return business_service.crear_negocio(usuario_id, datos)


@router.get("/{negocio_id}", response_model=BusinessOut, summary="Ver mi negocio (S17 / S04 edición)")
def obtener_negocio(negocio_id: UUID, usuario_id: UUID = Depends(get_current_user_id)):
    return business_service.obtener_negocio(usuario_id, negocio_id)
