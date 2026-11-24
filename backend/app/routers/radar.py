from uuid import UUID

from fastapi import APIRouter, Body, Depends, status

from app.core.security import get_current_user_id
from app.schemas.radar import RadarScanIn, RadarScanOut
from app.services import radar_service

router = APIRouter(prefix="/businesses/{negocio_id}/radar", tags=["Radar"])


@router.post("/scan", response_model=RadarScanOut, status_code=status.HTTP_201_CREATED,
             summary="Escanear competencia (S09 → S10)")
def escanear(
    negocio_id: UUID,
    datos: RadarScanIn = Body(default_factory=RadarScanIn),
    usuario_id: UUID = Depends(get_current_user_id),
):
    """Busca competidores del mismo rubro en el radio, filtra reseñas de 1 a 3 estrellas y las clasifica
    en tipos de fricción. Si `es_demo` es true, la app debe mostrar `aviso` (datos ficticios)."""
    return radar_service.escanear(usuario_id, negocio_id, datos.radio_km)
