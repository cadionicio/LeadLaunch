from typing import Literal
from uuid import UUID

from pydantic import BaseModel, Field, field_validator

TipoRegla = Literal["RECARGO_EXPRESS", "DESC_BIENVENIDA", "DESC_VOLUMEN"]


class PoliticaIn(BaseModel):
    permite_express: bool
    permite_acumular_desc: bool
    acepta_yape_plin: bool
    horario_atencion: str = Field(min_length=1, max_length=100)


class ReglaIn(BaseModel):
    tipo_regla: TipoRegla
    valor_porcentaje: float = Field(ge=0, le=100)
    activa: bool = True
    descripcion: str | None = Field(default=None, max_length=150)
    monto_minimo: float = Field(default=0, ge=0)
    cantidad_minima: int = Field(default=1, ge=1)


class RulesIn(BaseModel):
    politica: PoliticaIn
    reglas: list[ReglaIn] = Field(default_factory=list, max_length=3)

    @field_validator("reglas")
    @classmethod
    def _sin_repetidos(cls, v: list[ReglaIn]) -> list[ReglaIn]:
        tipos = [r.tipo_regla for r in v]
        if len(tipos) != len(set(tipos)):
            raise ValueError("Cada tipo de regla puede aparecer una sola vez.")
        return v


class PoliticaOut(PoliticaIn):
    pass


class ReglaOut(BaseModel):
    regla_id: UUID
    tipo_regla: TipoRegla
    descripcion: str
    valor_porcentaje: float
    monto_minimo: float
    cantidad_minima: int
    activa: bool


class RulesOut(BaseModel):
    politica: PoliticaOut
    reglas: list[ReglaOut]
