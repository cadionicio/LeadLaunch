"""Seguridad: hash de contraseñas (bcrypt) y tokens JWT."""
from datetime import datetime, timedelta, timezone
from uuid import UUID

import bcrypt
import jwt
from fastapi import Depends
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.core.config import settings
from app.core.errors import AppError

# bcrypt solo usa los primeros 72 bytes; las contraseñas se limitan a 64 caracteres en el esquema.
_MAX_BYTES = 72


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode("utf-8")[:_MAX_BYTES], bcrypt.gensalt()).decode("utf-8")


def verify_password(password: str, password_hash: str) -> bool:
    try:
        return bcrypt.checkpw(password.encode("utf-8")[:_MAX_BYTES], password_hash.encode("utf-8"))
    except ValueError:
        return False


def crear_token(usuario_id: UUID) -> str:
    ahora = datetime.now(timezone.utc)
    payload = {
        "sub": str(usuario_id),
        "iat": ahora,
        "exp": ahora + timedelta(minutes=settings.jwt_expira_min),
    }
    return jwt.encode(payload, settings.jwt_secret, algorithm=settings.jwt_algoritmo)


def _error_sesion(mensaje: str = "Tu sesión no es válida o expiró. Inicia sesión nuevamente.") -> AppError:
    return AppError(401, "NO_AUTENTICADO", mensaje)


def decodificar_token(token: str) -> UUID:
    try:
        payload = jwt.decode(token, settings.jwt_secret, algorithms=[settings.jwt_algoritmo])
        return UUID(payload["sub"])
    except jwt.ExpiredSignatureError:
        raise _error_sesion("Tu sesión expiró. Inicia sesión nuevamente.")
    except (jwt.InvalidTokenError, KeyError, ValueError):
        raise _error_sesion()


_bearer = HTTPBearer(auto_error=False)


def get_current_user_id(credenciales: HTTPAuthorizationCredentials | None = Depends(_bearer)) -> UUID:
    """Dependencia para endpoints protegidos: devuelve el usuario_id del token o responde 401."""
    if credenciales is None or credenciales.scheme.lower() != "bearer":
        raise _error_sesion("Falta el token de acceso (Authorization: Bearer ...).")
    return decodificar_token(credenciales.credentials)
