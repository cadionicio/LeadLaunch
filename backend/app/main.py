"""LeadLaunch API — punto de entrada (Capa 1: controladores)."""
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.core.config import settings
from app.core.database import close_pool
from app.core.errors import registrar_manejadores
from app.routers import auth, businesses, catalog, health, radar

API_PREFIX = "/api/v1"


@asynccontextmanager
async def lifespan(_: FastAPI):
    yield
    close_pool()


app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="API REST de LeadLaunch: radar comercial y copiloto adaptativo local.",
    lifespan=lifespan,
)

registrar_manejadores(app)

app.include_router(health.router)
app.include_router(auth.router, prefix=API_PREFIX)
app.include_router(businesses.router, prefix=API_PREFIX)
app.include_router(catalog.router, prefix=API_PREFIX)
app.include_router(radar.router, prefix=API_PREFIX)
