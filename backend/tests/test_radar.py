"""Pruebas de CP2: radar local y fricciones (HU-03). Relacionadas con CP-03."""
import json
from pathlib import Path

import pytest

from app.services import friccion_clasificador as clf
from tests.conftest import NEGOCIO_VALIDO

DATASET = Path(__file__).resolve().parents[2] / "database" / "fallback" / "radar_dataset.json"


# ---------- Clasificador (sin base de datos) ----------

@pytest.mark.parametrize("texto,esperado", [
    ("El pedido llegó una hora tarde", "DEMORA_ENTREGA"),
    ("La caja llegó aplastada y la decoración dañada", "CALIDAD_PRESENTACION"),
    ("Demasiado caro para el tamaño de la porción", "PRECIO_DELIVERY"),
    ("No responden los mensajes de WhatsApp", "MALA_ATENCION"),
    ("Pedí una torta para el mismo día y no aceptan pedidos urgentes", "SIN_OPCIONES_URGENTES"),
    ("Me gustó el caramelo", None),          # "caramelo" no es "caro"
    ("Todo bien", None),
])
def test_clasificador(texto, esperado):
    assert clf.clasificar(texto) == esperado


def test_filtro_estrellas():
    assert clf.es_friccion(3, "algo") and clf.es_friccion(1, "algo")
    assert not clf.es_friccion(4, "algo") and not clf.es_friccion(2, "   ") and not clf.es_friccion(2, "")


def test_dataset_todas_las_quejas_se_clasifican():
    datos = json.loads(DATASET.read_text(encoding="utf-8"))
    assert "FICTICIOS" in datos["aviso"]
    sin_tipo = [r["texto"] for c in datos["competidores"] for r in c["resenas"]
                if clf.es_friccion(r["calificacion"], r["texto"]) and clf.clasificar(r["texto"]) is None]
    assert sin_tipo == []
    tipos = {clf.clasificar(r["texto"]) for c in datos["competidores"] for r in c["resenas"]
             if clf.es_friccion(r["calificacion"], r["texto"])}
    assert tipos == set(clf.PATRONES)  # el dataset cubre los 5 tipos


def test_radio_invalido_422(client):
    r = client.post("/api/v1/businesses/00000000-0000-0000-0000-000000000000/radar/scan",
                    json={"radio_km": 30}, headers={"Authorization": "Bearer x"})
    assert r.status_code in (401, 422)


# ---------- Escaneo completo (con base de datos de prueba) ----------

@pytest.fixture
def negocio(db_client, correo_unico):
    r = db_client.post("/api/v1/auth/register",
                       json={"nombre_completo": "Ana Rodríguez", "correo": correo_unico, "password": "Segura123"})
    h = {"Authorization": f"Bearer {r.json()['access_token']}"}
    n = db_client.post("/api/v1/businesses", json=NEGOCIO_VALIDO, headers=h).json()
    return n["negocio_id"], h


def test_escaneo_radar(db_client, negocio):  # CP-03
    nid, h = negocio
    r = db_client.post(f"/api/v1/businesses/{nid}/radar/scan", json={"radio_km": 3.0}, headers=h)
    assert r.status_code == 201, r.text
    s = r.json()
    assert s["fuente_datos"] == "DATASET_FALLBACK" and s["es_demo"] is True and s["aviso"]
    assert s["total_competidores"] >= 3
    assert all(c["distancia_km"] <= 3.0 for c in s["competidores"])
    assert s["total_fricciones"] > 0 and s["nuevas_fricciones"] == s["total_fricciones"]
    assert all(f["calificacion"] <= 3 for c in s["competidores"] for f in c["fricciones"])
    assert sum(x["cantidad"] for x in s["resumen_fricciones"]) == s["total_fricciones"]

    d = db_client.get(f"/api/v1/businesses/{nid}/dashboard", headers=h).json()
    assert d["total_escaneos"] == 1                       # trigger trg_escaneo_comportamiento
    assert d["fricciones_detectadas"] == s["total_fricciones"]

    # Segundo escaneo: mismas fricciones, ninguna nueva, no se duplican en el dashboard
    s2 = db_client.post(f"/api/v1/businesses/{nid}/radar/scan", json={"radio_km": 3.0}, headers=h).json()
    assert s2["total_fricciones"] == s["total_fricciones"] and s2["nuevas_fricciones"] == 0
    d2 = db_client.get(f"/api/v1/businesses/{nid}/dashboard", headers=h).json()
    assert d2["total_escaneos"] == 2 and d2["fricciones_detectadas"] == s["total_fricciones"]


def test_escaneo_radio_por_defecto_y_pequeno(db_client, negocio):
    nid, h = negocio
    r = db_client.post(f"/api/v1/businesses/{nid}/radar/scan", headers=h)   # sin cuerpo → radio del negocio (3 km)
    assert r.status_code == 201 and r.json()["radio_busqueda_km"] == 3.0
    r = db_client.post(f"/api/v1/businesses/{nid}/radar/scan", json={"radio_km": 0.5}, headers=h)
    assert r.status_code == 201 and r.json()["total_competidores"] == 0
