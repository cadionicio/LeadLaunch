"""Configuración de pruebas.

- Las pruebas marcadas con `db` necesitan una base de PRUEBA con el DDL ya ejecutado.
  Define TEST_DATABASE_URL (¡nunca la base de Neon de producción/demo!). Sin esa variable se omiten.
"""
import os
import uuid

import pytest
from fastapi.testclient import TestClient

from app.core import database
from app.core.config import settings
from app.main import app

TEST_DATABASE_URL = os.getenv("TEST_DATABASE_URL")


@pytest.fixture(scope="session")
def client():
    return TestClient(app)


@pytest.fixture(scope="session")
def db_client():
    if not TEST_DATABASE_URL:
        pytest.skip("TEST_DATABASE_URL no definida: se omiten pruebas con base de datos")
    database.close_pool()
    settings.database_url = TEST_DATABASE_URL
    yield TestClient(app)
    database.close_pool()


@pytest.fixture
def correo_unico():
    return f"prueba_{uuid.uuid4().hex[:10]}@correo-prueba.com"


NEGOCIO_VALIDO = {
    "rubro_id": 1,
    "razon_comercial": "Dulce Punto",
    "descripcion": "Repostería artesanal y tortas personalizadas en Miraflores.",
    "whatsapp_numero": "51987654321",
    "distrito": "Miraflores",
    "latitud": -12.121100,
    "longitud": -77.029700,
    "radio_cobertura_km": 3.0,
}
