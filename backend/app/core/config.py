"""Configuración central: se lee de variables de entorno o del archivo .env."""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    app_name: str = "LeadLaunch API"
    app_version: str = "0.1.0"

    database_url: str = ""

    jwt_secret: str = "cambiar-en-produccion"
    jwt_algoritmo: str = "HS256"
    jwt_expira_min: int = 1440

    gemini_api_key: str = ""
    overpass_url: str = "https://overpass-api.de/api/interpreter"


settings = Settings()
