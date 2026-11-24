"""Manejo global de errores con un formato único para la app Android:

    {"error": {"codigo": "SKU_DUPLICADO", "mensaje": "...", "campos": ["sku"]}}
"""
import logging

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

logger = logging.getLogger("leadlaunch")

CODIGOS_HTTP = {
    400: "REGLA_NEGOCIO",
    401: "NO_AUTENTICADO",
    403: "PROHIBIDO",
    404: "NO_ENCONTRADO",
    405: "METODO_NO_PERMITIDO",
    409: "CONFLICTO",
    422: "VALIDACION",
    503: "SERVICIO_NO_DISPONIBLE",
}


class AppError(Exception):
    """Error de negocio. Ejemplo: raise AppError(422, "SKU_DUPLICADO", "Ya existe ese SKU.", ["sku"])"""

    def __init__(self, status_code: int, codigo: str, mensaje: str, campos: list[str] | None = None):
        super().__init__(mensaje)
        self.status_code = status_code
        self.codigo = codigo
        self.mensaje = mensaje
        self.campos = campos or []


def _respuesta(status_code: int, codigo: str, mensaje: str, campos: list[str] | None = None) -> JSONResponse:
    return JSONResponse(
        status_code=status_code,
        content={"error": {"codigo": codigo, "mensaje": mensaje, "campos": campos or []}},
    )


def registrar_manejadores(app: FastAPI) -> None:
    @app.exception_handler(AppError)
    async def _app_error(_: Request, exc: AppError):
        return _respuesta(exc.status_code, exc.codigo, exc.mensaje, exc.campos)

    @app.exception_handler(RequestValidationError)
    async def _validacion(_: Request, exc: RequestValidationError):
        campos = sorted({str(e["loc"][-1]) for e in exc.errors() if e.get("loc")})
        return _respuesta(422, "VALIDACION", "Hay campos inválidos o incompletos.", campos)

    @app.exception_handler(StarletteHTTPException)
    async def _http(_: Request, exc: StarletteHTTPException):
        codigo = CODIGOS_HTTP.get(exc.status_code, "ERROR_HTTP")
        mensaje = exc.detail if isinstance(exc.detail, str) else "Error en la solicitud."
        return _respuesta(exc.status_code, codigo, mensaje)

    @app.exception_handler(Exception)
    async def _inesperado(_: Request, exc: Exception):
        logger.exception("Error no controlado: %s", exc)
        return _respuesta(500, "ERROR_INTERNO", "Ocurrió un error inesperado. Intenta nuevamente.")
