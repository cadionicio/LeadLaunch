"""Pruebas de CP2: productos (HU-01), reglas (HU-02) y dashboard (HU-06 base). Relacionadas con CP-02 y CP-07."""
import pytest

from tests.conftest import NEGOCIO_VALIDO

PASSWORD = "Segura123"
PRODUCTO = {"sku": "tor-001", "nombre": "Torta de chocolate", "descripcion": "Para 12 porciones",
            "precio_base": 75.00, "tiempo_prep_horas": 24, "stock_disponible": 8, "permite_express": True}


@pytest.fixture
def negocio(db_client, correo_unico):
    r = db_client.post("/api/v1/auth/register",
                       json={"nombre_completo": "Ana Rodríguez", "correo": correo_unico, "password": PASSWORD})
    token = r.json()["access_token"]
    h = {"Authorization": f"Bearer {token}"}
    n = db_client.post("/api/v1/businesses", json=NEGOCIO_VALIDO, headers=h).json()
    return n["negocio_id"], h


def test_producto_sin_token_401(client):  # CP-06
    r = client.get("/api/v1/businesses/00000000-0000-0000-0000-000000000000/products")
    assert r.status_code == 401


def test_producto_precio_negativo_422(client):  # CP-07 (la validación ocurre antes de tocar la BD)
    r = client.post("/api/v1/businesses/00000000-0000-0000-0000-000000000000/products",
                    json={**PRODUCTO, "precio_base": -20}, headers={"Authorization": "Bearer x"})
    assert r.status_code in (401, 422)


def test_crear_listar_productos(db_client, negocio):  # CP-02
    nid, h = negocio
    r = db_client.post(f"/api/v1/businesses/{nid}/products", json=PRODUCTO, headers=h)
    assert r.status_code == 201, r.text
    p = r.json()
    assert p["sku"] == "TOR-001" and p["precio_base"] == 75.0 and p["activo"] is True

    db_client.post(f"/api/v1/businesses/{nid}/products",
                   json={**PRODUCTO, "sku": "CHE-002", "nombre": "Cheesecake de frutos", "precio_base": 65},
                   headers=h)
    r = db_client.get(f"/api/v1/businesses/{nid}/products?page=1&size=1", headers=h)
    assert r.status_code == 200
    pag = r.json()
    assert pag["total"] == 2 and len(pag["items"]) == 1 and pag["items"][0]["nombre"] == "Cheesecake de frutos"


def test_producto_validaciones(db_client, negocio):
    nid, h = negocio
    db_client.post(f"/api/v1/businesses/{nid}/products", json=PRODUCTO, headers=h)
    r = db_client.post(f"/api/v1/businesses/{nid}/products", json={**PRODUCTO, "sku": "TOR-001"}, headers=h)
    assert r.status_code == 422 and r.json()["error"]["codigo"] == "SKU_DUPLICADO"

    for cambio, campo in [({"precio_base": 0}, "precio_base"), ({"precio_base": -20}, "precio_base"),
                          ({"tiempo_prep_horas": 0}, "tiempo_prep_horas"), ({"stock_disponible": -1}, "stock_disponible"),
                          ({"sku": "TOR 001"}, "sku")]:
        r = db_client.post(f"/api/v1/businesses/{nid}/products", json={**PRODUCTO, "sku": "X-1", **cambio}, headers=h)
        assert r.status_code == 422, cambio
        assert campo in r.json()["error"]["campos"]


def test_reglas_base_y_actualizacion(db_client, negocio):  # HU-02
    nid, h = negocio
    r = db_client.get(f"/api/v1/businesses/{nid}/rules", headers=h)
    assert r.status_code == 200
    base = r.json()
    assert base["politica"]["permite_express"] is True
    assert {x["tipo_regla"]: x["valor_porcentaje"] for x in base["reglas"]} == {
        "DESC_BIENVENIDA": 10.0, "RECARGO_EXPRESS": 15.0}

    cuerpo = {"politica": {"permite_express": True, "permite_acumular_desc": False, "acepta_yape_plin": True,
                           "horario_atencion": "10:00 - 19:00"},
              "reglas": [{"tipo_regla": "RECARGO_EXPRESS", "valor_porcentaje": 20},
                         {"tipo_regla": "DESC_VOLUMEN", "valor_porcentaje": 5, "cantidad_minima": 10}]}
    r = db_client.put(f"/api/v1/businesses/{nid}/rules", json=cuerpo, headers=h)
    assert r.status_code == 200, r.text
    reglas = {x["tipo_regla"]: x for x in r.json()["reglas"]}
    assert reglas["RECARGO_EXPRESS"]["valor_porcentaje"] == 20.0
    assert reglas["DESC_BIENVENIDA"]["valor_porcentaje"] == 10.0          # no enviada → sin cambios
    assert reglas["DESC_VOLUMEN"]["cantidad_minima"] == 10                 # nueva
    assert r.json()["politica"]["horario_atencion"] == "10:00 - 19:00"

    # Repetir PUT no duplica reglas
    r = db_client.put(f"/api/v1/businesses/{nid}/rules", json=cuerpo, headers=h)
    assert len(r.json()["reglas"]) == 3


def test_reglas_invalidas_422(db_client, negocio):
    nid, h = negocio
    pol = {"permite_express": True, "permite_acumular_desc": False, "acepta_yape_plin": True,
           "horario_atencion": "09:00 - 20:00"}
    r = db_client.put(f"/api/v1/businesses/{nid}/rules",
                      json={"politica": pol, "reglas": [{"tipo_regla": "RECARGO_EXPRESS", "valor_porcentaje": 150}]},
                      headers=h)
    assert r.status_code == 422
    r = db_client.put(f"/api/v1/businesses/{nid}/rules",
                      json={"politica": pol, "reglas": [{"tipo_regla": "TARJETA", "valor_porcentaje": 2.5}]},
                      headers=h)
    assert r.status_code == 422
    r = db_client.put(f"/api/v1/businesses/{nid}/rules",
                      json={"politica": pol, "reglas": [{"tipo_regla": "RECARGO_EXPRESS", "valor_porcentaje": 10},
                                                         {"tipo_regla": "RECARGO_EXPRESS", "valor_porcentaje": 12}]},
                      headers=h)
    assert r.status_code == 422


def test_dashboard(db_client, negocio):
    nid, h = negocio
    r = db_client.get(f"/api/v1/businesses/{nid}/dashboard", headers=h)
    assert r.status_code == 200
    d = r.json()
    assert d["productos_activos"] == 0 and d["tasa_conversion_pct"] == 0.0 and d["ultima_alerta_adaptativa"] is None

    db_client.post(f"/api/v1/businesses/{nid}/products", json=PRODUCTO, headers=h)
    d = db_client.get(f"/api/v1/businesses/{nid}/dashboard", headers=h).json()
    assert d["productos_activos"] == 1
    assert d["razon_comercial"] == "Dulce Punto"


def test_recursos_de_negocio_ajeno_404(db_client, negocio, correo_unico):  # CP-10
    nid, _ = negocio
    otro = db_client.post("/api/v1/auth/register",
                          json={"nombre_completo": "Intruso", "correo": "x_" + correo_unico, "password": PASSWORD})
    h2 = {"Authorization": f"Bearer {otro.json()['access_token']}"}
    for metodo, ruta, cuerpo in [("get", "products", None), ("post", "products", PRODUCTO), ("get", "rules", None),
                                 ("get", "dashboard", None), ("post", "radar/scan", {})]:
        r = getattr(db_client, metodo)(f"/api/v1/businesses/{nid}/{ruta}", headers=h2,
                                       **({"json": cuerpo} if cuerpo is not None else {}))
        assert r.status_code == 404, (ruta, r.text)
