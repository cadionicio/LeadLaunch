from uuid import UUID

from pydantic import BaseModel


class DashboardOut(BaseModel):
    negocio_id: UUID
    razon_comercial: str
    rubro_nombre: str
    distrito: str
    total_escaneos: int
    total_cotizaciones: int
    total_ventas_ganadas: int
    monto_ventas_ganadas: float
    tasa_conversion_pct: float
    productos_activos: int
    fricciones_detectadas: int
    ultima_alerta_adaptativa: str | None
