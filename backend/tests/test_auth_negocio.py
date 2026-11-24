"""Pruebas de CP1: registro, login y alta de negocio (HU-01). Relacionadas con CP-01, CP-06, CP-07, CP-10."""
from tests.conftest import NEGOCIO_VALIDO

PASSWORD = "Segura123"


# ---------- Sin base de datos ----------

def test_register_password_debil_422(client):
    r = client.post("/api/v1/auth/register",
                    json={"nombre_completo": "Ana", "correo": "ana@test.com", "password": "debil"})
    assert r.status_code == 422
    assert r.json()["error"]["campos"] == ["password"]


def test_register_correo_invalido_422(client):
    r = client.post("/api/v1/auth/register",
                    json={"nombre_completo": "Ana", "correo": "no-es-correo", "password": PASSWORD})
    assert r.status_code == 422
    assert "correo" in r.json()["error"]["campos"]


def test_negocio_sin_token_401(client):  # CP-06
    r = client.post("/api/v1/businesses", json=NEGOCIO_VALIDO)
    assert r.status_code == 401
    assert r.json()["error"]["codigo"] == "NO_AUTENTICADO"


def test_token_falso_401(client):
    r = client.post("/api/v1/businesses", json=NEGOCIO_VALIDO,
                    headers={"Authorization": "Bearer token.falso.123"})
    assert r.status_code == 401


# ---------- Con base de datos de prueba ----------

def _registrar(db_client, correo):
    r = db_client.post("/api/v1/auth/register",
                       json={"nombre_completo": "Ana Rodríguez", "correo": correo, "password": PASSWORD})
    assert r.status_code == 201, r.text
    return r.json()


def _auth(token):
    return {"Authorization": f"Bearer {token}"}


def test_flujo_registro_login_negocio(db_client, correo_unico):  # CP-01 + HU-01
    reg = _registrar(db_client, correo_unico)
    assert reg["negocio_id"] is None and reg["token_type"] == "bearer"

    # Login antes del negocio → negocio_id null (la app va a S04)
    r = db_client.post("/api/v1/auth/login", json={"correo": correo_unico.upper(), "password": PASSWORD})
    assert r.status_code == 200
    assert r.json()["negocio_id"] is None
    token = r.json()["access_token"]

    r = db_client.post("/api/v1/businesses", json=NEGOCIO_VALIDO, headers=_auth(token))
    assert r.status_code == 201, r.text
    negocio = r.json()
    assert negocio["rubro_nombre"] == "Repostería y Pastelería Artesanal"
    assert negocio["radio_cobertura_km"] == 3.0

    # Login después → trae negocio_id (la app va a S05)
    r = db_client.post("/api/v1/auth/login", json={"correo": correo_unico, "password": PASSWORD})
    assert r.json()["negocio_id"] == negocio["negocio_id"]

    # Ver negocio propio
    r = db_client.get(f"/api/v1/businesses/{negocio['negocio_id']}", headers=_auth(token))
    assert r.status_code == 200

    # Segundo negocio → 400 (MVP: uno por cuenta)
    r = db_client.post("/api/v1/businesses", json=NEGOCIO_VALIDO, headers=_auth(token))
    assert r.status_code == 400
    assert r.json()["error"]["codigo"] == "NEGOCIO_EXISTENTE"


def test_correo_duplicado_422(db_client, correo_unico):
    _registrar(db_client, correo_unico)
    r = db_client.post("/api/v1/auth/register",
                       json={"nombre_completo": "Otra", "correo": correo_unico, "password": PASSWORD})
    assert r.status_code == 422
    assert r.json()["error"]["codigo"] == "CORREO_REGISTRADO"


def test_login_incorrecto_401(db_client, correo_unico):
    _registrar(db_client, correo_unico)
    r = db_client.post("/api/v1/auth/login", json={"correo": correo_unico, "password": "Incorrecta1"})
    assert r.status_code == 401
    r = db_client.post("/api/v1/auth/login", json={"correo": "nadie@correo-prueba.com", "password": PASSWORD})
    assert r.status_code == 401
    assert r.json()["error"]["mensaje"] == "No pudimos iniciar sesión. Revisa tus datos."


def test_whatsapp_invalido_422(db_client, correo_unico):  # CP-07
    token = _registrar(db_client, correo_unico)["access_token"]
    r = db_client.post("/api/v1/businesses", json={**NEGOCIO_VALIDO, "whatsapp_numero": "abc"},
                       headers=_auth(token))
    assert r.status_code == 422
    assert r.json()["error"]["campos"] == ["whatsapp_numero"]


def test_rubro_inexistente_422(db_client, correo_unico):
    token = _registrar(db_client, correo_unico)["access_token"]
    r = db_client.post("/api/v1/businesses", json={**NEGOCIO_VALIDO, "rubro_id": 99}, headers=_auth(token))
    assert r.status_code == 422
    assert r.json()["error"]["codigo"] == "RUBRO_INVALIDO"


def test_negocio_ajeno_404(db_client, correo_unico):  # CP-10
    token_a = _registrar(db_client, correo_unico)["access_token"]
    negocio_a = db_client.post("/api/v1/businesses", json=NEGOCIO_VALIDO, headers=_auth(token_a)).json()
    token_b = _registrar(db_client, "b_" + correo_unico)["access_token"]
    r = db_client.get(f"/api/v1/businesses/{negocio_a['negocio_id']}", headers=_auth(token_b))
    assert r.status_code == 404
