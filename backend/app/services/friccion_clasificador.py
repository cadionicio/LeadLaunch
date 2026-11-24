"""Clasificación algorítmica de reseñas en tipos de fricción (sin IA: código puro).

Cada tipo tiene patrones de palabras clave. Gana el tipo con más coincidencias; en empate,
el que aparece primero en PATRONES. Si ningún patrón coincide, la reseña no se clasifica.
"""
import re
import unicodedata

PATRONES: dict[str, list[str]] = {
    "DEMORA_ENTREGA": [
        r"\btarde\b", r"\bdemor", r"\bretras", r"\batras", r"\bnunca llego", r"\bespere\b", r"\besperamos\b",
        r"\bhora pactada", r"\bpuntual", r"\ba tiempo\b",
    ],
    "CALIDAD_PRESENTACION": [
        r"\bdanad", r"\baplast", r"\broto\b", r"\brota\b", r"\bmal empac", r"\bsec[oa]\b", r"\bfresc",
        r"\brancio", r"\bderretid", r"\bderramad", r"\bfri[oa]s?\b", r"\bincomplet", r"\bpresentacion",
        r"\bdecoracion",
    ],
    "PRECIO_DELIVERY": [
        r"\bcar[oa]s?\b", r"\bprecio", r"\benvio", r"\bdelivery elevado", r"\bcosto", r"\bcobran\b",
        r"\bporcion",
    ],
    "MALA_ATENCION": [
        r"\batencion\b", r"\bresponden\b", r"\bcontestan\b", r"\bgroser", r"\btrato\b", r"\bmensajes\b",
        r"\bignor", r"\breclam", r"\bnadie me aviso", r"\blent[oa]\b",
    ],
    "SIN_OPCIONES_URGENTES": [
        r"\bmismo dia\b", r"\bhoy mismo\b", r"\bpara hoy\b", r"\burgente", r"\bultimo momento\b",
        r"\bexpress\b", r"\banticipacion\b", r"\bpedido previo\b", r"\bno hacen pedidos\b",
        r"\bno aceptan pedidos\b",
    ],
}

_COMPILADOS = {tipo: [re.compile(p) for p in pats] for tipo, pats in PATRONES.items()}


def normalizar(texto: str) -> str:
    sin_tildes = unicodedata.normalize("NFKD", texto.lower())
    return "".join(c for c in sin_tildes if not unicodedata.combining(c))


def clasificar(texto: str) -> str | None:
    """Devuelve el código de cat_tipos_friccion o None si no se reconoce."""
    t = normalizar(texto)
    mejor, puntaje = None, 0
    for tipo, patrones in _COMPILADOS.items():
        n = sum(1 for p in patrones if p.search(t))
        if n > puntaje:
            mejor, puntaje = tipo, n
    return mejor


def es_friccion(calificacion: int, texto: str | None) -> bool:
    """Filtro del radar: solo reseñas de 1 a 3 estrellas con texto."""
    return 1 <= calificacion <= 3 and bool(texto and texto.strip())
