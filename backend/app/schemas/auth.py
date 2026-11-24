from uuid import UUID

from pydantic import BaseModel, EmailStr, Field, field_validator


class RegisterIn(BaseModel):
    nombre_completo: str = Field(min_length=2, max_length=120)
    correo: EmailStr = Field(max_length=120)
    password: str = Field(min_length=8, max_length=64)

    @field_validator("nombre_completo")
    @classmethod
    def _limpiar_nombre(cls, v: str) -> str:
        v = " ".join(v.split())
        if len(v) < 2:
            raise ValueError("El nombre es obligatorio.")
        return v

    @field_validator("correo")
    @classmethod
    def _normalizar_correo(cls, v: str) -> str:
        return v.strip().lower()

    @field_validator("password")
    @classmethod
    def _politica_password(cls, v: str) -> str:
        # Criterios de la maqueta S03: mínimo 8, una mayúscula y un número.
        if not any(c.isupper() for c in v):
            raise ValueError("La contraseña debe tener al menos una mayúscula.")
        if not any(c.isdigit() for c in v):
            raise ValueError("La contraseña debe tener al menos un número.")
        return v


class LoginIn(BaseModel):
    correo: EmailStr
    password: str = Field(min_length=1, max_length=64)

    @field_validator("correo")
    @classmethod
    def _normalizar_correo(cls, v: str) -> str:
        return v.strip().lower()


class UsuarioOut(BaseModel):
    usuario_id: UUID
    nombre_completo: str
    correo: str


class AuthOut(BaseModel):
    access_token: str
    token_type: str = "bearer"
    usuario: UsuarioOut
    negocio_id: UUID | None = Field(
        default=None, description="null si el usuario aún no registró su negocio (la app debe ir a S04)."
    )
