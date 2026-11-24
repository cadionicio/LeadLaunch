from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field


class RadarScanIn(BaseModel):
    radio_km: float | None = Field(default=None, ge=0.5, le=25,
                                   description="Si no se envía, se usa el radio registrado del negocio.")


class FriccionOut(BaseModel):
    tipo_codigo: str
    tipo_nombre: str
    calificacion: int
    texto_resena: str
    resumen_dolor: str


class CompetidorOut(BaseModel):
    competidor_id: UUID
    nombre: str
    distrito: str
    latitud: float
    longitud: float
    distancia_km: float
    calificacion_prom: float | None
    total_resenas: int
    fricciones: list[FriccionOut]


class ResumenFriccionOut(BaseModel):
    tipo_codigo: str
    tipo_nombre: str
    cantidad: int


class RadarScanOut(BaseModel):
    escaneo_id: UUID
    fuente_datos: str
    es_demo: bool = Field(description="true si los datos vienen del dataset ficticio de respaldo")
    aviso: str | None
    radio_busqueda_km: float
    total_competidores: int
    total_analizados: int = Field(description="Reseñas revisadas de los competidores dentro del radio")
    total_fricciones: int = Field(description="Reseñas de 1 a 3 estrellas clasificadas en este escaneo")
    nuevas_fricciones: int = Field(description="Fricciones que no estaban guardadas de escaneos anteriores")
    ejecutado_en: datetime
    competidores: list[CompetidorOut]
    resumen_fricciones: list[ResumenFriccionOut]
