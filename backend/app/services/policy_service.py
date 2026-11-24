"""Capa 2: PolicyService — políticas y reglas comerciales.

Las reglas guardadas aquí solo afectan cotizaciones NUEVAS: cada cotización guarda su propio
snapshot de precios y reglas (snapshot_reglas_json) al emitirse.
"""
from uuid import UUID

from app.core.database import get_conn
from app.core.errors import AppError
from app.repositories import reglas_repo
from app.schemas.rules import PoliticaOut, ReglaOut, RulesIn, RulesOut
from app.services.business_service import verificar_propiedad


def _leer(conn, negocio_id: UUID) -> RulesOut:
    politica = reglas_repo.obtener_politica(conn, negocio_id)
    if politica is None:
        raise AppError(404, "POLITICA_NO_ENCONTRADA", "El negocio no tiene políticas configuradas.")
    return RulesOut(
        politica=PoliticaOut(**politica),
        reglas=[ReglaOut(**r) for r in reglas_repo.listar_reglas(conn, negocio_id)],
    )


def obtener_reglas(usuario_id: UUID, negocio_id: UUID) -> RulesOut:
    with get_conn() as conn:
        verificar_propiedad(conn, negocio_id, usuario_id)
        return _leer(conn, negocio_id)


def actualizar_reglas(usuario_id: UUID, negocio_id: UUID, datos: RulesIn) -> RulesOut:
    """Política + reglas en una sola transacción. Los tipos de regla no enviados quedan como estaban."""
    with get_conn() as conn:
        verificar_propiedad(conn, negocio_id, usuario_id)
        reglas_repo.guardar_politica(conn, negocio_id, datos.politica.model_dump())
        for regla in datos.reglas:
            reglas_repo.guardar_regla(conn, negocio_id, regla.model_dump())
        return _leer(conn, negocio_id)
