from fastapi import APIRouter

from app.core.config import settings
from app.core.database import get_conn
from app.core.errors import AppError

router = APIRouter(tags=["Salud"])


@router.get("/health")
def health():
    """Comprueba que la API está viva (no toca la base de datos)."""
    return {"estado": "ok", "servicio": settings.app_name, "version": settings.app_version}


@router.get("/health/db")
def health_db():
    """Comprueba la conexión con PostgreSQL."""
    try:
        with get_conn() as conn:
            fila = conn.execute("SELECT version() AS version").fetchone()
    except Exception as exc:  # noqa: BLE001
        raise AppError(503, "BD_NO_DISPONIBLE", f"No se pudo conectar a la base de datos: {type(exc).__name__}")
    return {"estado": "ok", "postgres": fila["version"]}
