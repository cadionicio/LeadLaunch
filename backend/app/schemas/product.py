from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field, field_validator


class ProductIn(BaseModel):
    sku: str = Field(min_length=1, max_length=30, pattern=r"^[A-Za-z0-9_-]+$",
                     description="Código único por negocio. Letras, números, - y _. Se guarda en mayúsculas.")
    nombre: str = Field(min_length=2, max_length=120)
    descripcion: str | None = Field(default=None, max_length=300)
    precio_base: float = Field(gt=0, le=99_999_999.99, description="En soles, mayor que 0")
    tiempo_prep_horas: int = Field(default=24, ge=1, le=32767, description="Horas de preparación (≥ 1)")
    stock_disponible: int = Field(default=0, ge=0)
    permite_express: bool = True

    @field_validator("sku")
    @classmethod
    def _sku(cls, v: str) -> str:
        return v.strip().upper()

    @field_validator("nombre")
    @classmethod
    def _nombre(cls, v: str) -> str:
        v = " ".join(v.split())
        if len(v) < 2:
            raise ValueError("El nombre es obligatorio.")
        return v

    @field_validator("descripcion")
    @classmethod
    def _descripcion(cls, v: str | None) -> str | None:
        if v is None:
            return None
        v = " ".join(v.split())
        return v or None

    @field_validator("precio_base")
    @classmethod
    def _dos_decimales(cls, v: float) -> float:
        if round(v, 2) != v:
            raise ValueError("El precio admite como máximo 2 decimales.")
        return v


class ProductOut(BaseModel):
    producto_id: UUID
    sku: str
    nombre: str
    descripcion: str | None
    precio_base: float
    tiempo_prep_horas: int
    stock_disponible: int
    permite_express: bool
    activo: bool
    creado_en: datetime


class ProductPage(BaseModel):
    items: list[ProductOut]
    page: int
    size: int
    total: int
