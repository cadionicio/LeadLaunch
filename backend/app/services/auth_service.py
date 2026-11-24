"""Capa 2: AuthService — registro e inicio de sesión."""
from psycopg import errors as pg_errors

from app.core.database import get_conn
from app.core.errors import AppError
from app.core.security import crear_token, hash_password, verify_password
from app.repositories import negocios_repo, usuarios_repo
from app.schemas.auth import AuthOut, LoginIn, RegisterIn, UsuarioOut

_ERROR_CREDENCIALES = AppError(401, "CREDENCIALES_INVALIDAS", "No pudimos iniciar sesión. Revisa tus datos.")


def _correo_registrado() -> AppError:
    return AppError(422, "CORREO_REGISTRADO", "Ya existe una cuenta con este correo.", ["correo"])


def registrar(datos: RegisterIn) -> AuthOut:
    password_hash = hash_password(datos.password)
    try:
        with get_conn() as conn:
            if usuarios_repo.obtener_por_correo(conn, datos.correo):
                raise _correo_registrado()
            usuario = usuarios_repo.crear(conn, datos.correo, password_hash, datos.nombre_completo)
    except pg_errors.UniqueViolation:
        # Dos registros simultáneos con el mismo correo.
        raise _correo_registrado()

    return AuthOut(
        access_token=crear_token(usuario["usuario_id"]),
        usuario=UsuarioOut(**usuario),
        negocio_id=None,
    )


def iniciar_sesion(datos: LoginIn) -> AuthOut:
    with get_conn() as conn:
        usuario = usuarios_repo.obtener_por_correo(conn, datos.correo)
        # Mismo mensaje si el correo no existe o la contraseña es incorrecta (no revelar cuál falló).
        if not usuario or not usuario["activo"] or not verify_password(datos.password, usuario["password_hash"]):
            raise _ERROR_CREDENCIALES
        negocio_id = negocios_repo.obtener_id_activo_por_usuario(conn, usuario["usuario_id"])

    return AuthOut(
        access_token=crear_token(usuario["usuario_id"]),
        usuario=UsuarioOut(
            usuario_id=usuario["usuario_id"],
            nombre_completo=usuario["nombre_completo"],
            correo=usuario["correo"],
        ),
        negocio_id=negocio_id,
    )
