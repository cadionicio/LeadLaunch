"""Capa 2: consulta crítica del dashboard (S05) desde la vista materializada."""
from uuid import UUID

from app.core.database import get_conn
from app.core.errors import AppError
from app.repositories import negocios_repo
from app.schemas.dashboard import DashboardOut
from app.services.business_service import verificar_propiedad

_SQL = """SELECT negocio_id, razon_comercial, rubro_nombre, distrito, total_escaneos, total_cotizaciones,
                 total_ventas_ganadas, monto_ventas_ganadas::float AS monto_ventas_ganadas,
                 tasa_conversion_pct::float AS tasa_conversion_pct, productos_activos, fricciones_detectadas,
                 ultima_alerta_adaptativa
            FROM mv_resumen_comercial_negocio WHERE negocio_id = %s"""


def obtener_dashboard(usuario_id: UUID, negocio_id: UUID) -> DashboardOut:
    with get_conn() as conn:
        verificar_propiedad(conn, negocio_id, usuario_id)
        fila = conn.execute(_SQL, (negocio_id,)).fetchone()

    if fila is None:
        # La vista aún no incluye este negocio (p. ej. se creó antes del último refresco): refrescar una vez.
        with get_conn() as conn:
            negocios_repo.refrescar_resumen(conn)
        with get_conn() as conn:
            fila = conn.execute(_SQL, (negocio_id,)).fetchone()
        if fila is None:
            raise AppError(404, "NEGOCIO_NO_ENCONTRADO", "No encontramos este negocio.")
    return DashboardOut(**fila)
