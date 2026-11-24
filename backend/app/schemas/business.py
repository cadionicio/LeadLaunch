from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field, field_validator


def _texto(v: str) -> str:
    return " ".join(v.split())


class BusinessIn(BaseModel):
    rubro_id: int = Field(ge=1, le=32767, description="1 Repostería · 2 Comida rápida · 3 Cafeterías")
    razon_comercial: str = Field(min_length=2, max_length=120)
    descripcion: str = Field(min_length=1, max_length=400)
    whatsapp_numero: str = Field(pattern=r"^[0-9]{10,15}$", description="Solo dígitos con código de país, ej. 51987654321")
    distrito: str = Field(min_length=2, max_length=80)
    latitud: float = Field(ge=-90, le=90)
    longitud: float = Field(ge=-180, le=180)
    radio_cobertura_km: float = Field(default=3.0, ge=0.5, le=25.0)

    @field_validator("razon_comercial", "descripcion", "distrito")
    @classmethod
    def _limpiar(cls, v: str) -> str:
        v = _texto(v)
        if not v:
            raise ValueError("Campo obligatorio.")
        return v


class BusinessOut(BaseModel):
    negocio_id: UUID
    usuario_id: UUID
    rubro_id: int
    rubro_nombre: str
    razon_comercial: str
    descripcion: str
    whatsapp_numero: str
    distrito: str
    latitud: float
    longitud: float
    radio_cobertura_km: float
    activo: bool
    creado_en: datetime
