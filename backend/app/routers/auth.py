from fastapi import APIRouter, status

from app.schemas.auth import AuthOut, LoginIn, RegisterIn
from app.services import auth_service

router = APIRouter(prefix="/auth", tags=["Autenticación"])


@router.post("/register", response_model=AuthOut, status_code=status.HTTP_201_CREATED,
             summary="Crear cuenta (S03)")
def register(datos: RegisterIn):
    """Registra al emprendedor y devuelve su JWT. `negocio_id` siempre es null (siguiente pantalla: S04)."""
    return auth_service.registrar(datos)


@router.post("/login", response_model=AuthOut, summary="Iniciar sesión (S02)")
def login(datos: LoginIn):
    """Devuelve el JWT y `negocio_id` (null si aún no registró negocio → S04; si no → S05)."""
    return auth_service.iniciar_sesion(datos)
