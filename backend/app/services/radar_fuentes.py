"""Fuentes de competidores y reseñas para el radar.

Hoy solo existe la fuente DATASET_FALLBACK: un dataset de demostración con negocios y reseñas
FICTICIOS (database/fallback/radar_dataset.json). Los competidores se ubican alrededor del negocio
que escanea, según sus desplazamientos norte_km / este_km.

Pendiente (decisión D14): una fuente real con reseñas (p. ej. Google Places). Overpass/OSM solo da
ubicaciones, no reseñas, y nunca se deben mezclar reseñas ficticias con nombres de negocios reales.
"""
import json
import math
from dataclasses import dataclass, field
from functools import lru_cache
from pathlib import Path

from app.core.config import settings
from app.core.errors import AppError

FUENTE_DATASET = "DATASET_FALLBACK"
AVISO_DEMO = "Datos de referencia (demo): negocios y reseñas ficticios, no son opiniones reales."
_KM_POR_GRADO = 111.32


@dataclass
class Resena:
    calificacion: int
    texto: str


@dataclass
class Competidor:
    fuente_externa_id: str
    nombre: str
    latitud: float
    longitud: float
    distancia_km: float
    calificacion_prom: float | None
    resenas: list[Resena] = field(default_factory=list)


def _ruta_dataset() -> Path:
    if settings.radar_dataset_path:
        return Path(settings.radar_dataset_path)
    return Path(__file__).resolve().parents[3] / "database" / "fallback" / "radar_dataset.json"


@lru_cache(maxsize=1)
def _cargar_dataset() -> dict:
    ruta = _ruta_dataset()
    try:
        return json.loads(ruta.read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        raise AppError(503, "RADAR_NO_DISPONIBLE",
                       f"No se pudo leer el dataset de respaldo del radar ({type(exc).__name__}).")


def distancia_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Distancia Haversine en km."""
    r = 6371.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp, dl = math.radians(lat2 - lat1), math.radians(lon2 - lon1)
    a = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


def competidores_dataset(negocio_id: str, rubro_codigo: str, lat0: float, lon0: float,
                         radio_km: float) -> list[Competidor]:
    datos = _cargar_dataset()
    resultado: list[Competidor] = []
    for c in datos.get("competidores", []):
        if c.get("rubro_codigo") != rubro_codigo:
            continue
        lat = lat0 + c["norte_km"] / _KM_POR_GRADO
        lon = lon0 + c["este_km"] / (_KM_POR_GRADO * math.cos(math.radians(lat0)))
        lat = max(-90.0, min(90.0, round(lat, 6)))
        lon = ((lon + 180) % 360) - 180
        d = distancia_km(lat0, lon0, lat, lon)
        if d > radio_km:
            continue
        resultado.append(Competidor(
            # Un registro por negocio que escanea: la ubicación es relativa a cada negocio.
            fuente_externa_id=f"{c['id']}@{negocio_id}",
            nombre=c["nombre"],
            latitud=lat,
            longitud=round(lon, 6),
            distancia_km=round(d, 2),
            calificacion_prom=c.get("calificacion_prom"),
            resenas=[Resena(int(r["calificacion"]), r.get("texto") or "") for r in c.get("resenas", [])],
        ))
    return sorted(resultado, key=lambda x: x.distancia_km)
