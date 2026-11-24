"""Dibuja la notación de pata de gallo (Information Engineering) sobre el SVG de Graphviz.

Graphviz solo calcula disposición y rutas (aristas sin flechas). Para cada relación se determina
qué extremo de la ruta toca a la tabla padre y cuál a la hija (por distancia al rectángulo de cada
nodo), y se dibujan los símbolos en el extremo correcto:
  padre: ‖  (uno obligatorio)      o |  (cero o uno, si la FK admite nulo)
  hijo : ⋔| (uno o muchos)          ⋔o (cero o muchos)   ‖ (uno, si la FK es UNIQUE)
Uso: python3 cardinalidad.py entrada.svg salida.svg relaciones.json
"""
import json
import math
import re
import sys
from html import unescape

svg = open(sys.argv[1], encoding="utf-8").read()
rels = {f"{r['padre']}->{r['hijo']}": r for r in json.load(open(sys.argv[3]))}
COLOR = "#5B6B85"

# --- rectángulos de los nodos ---
cajas = {}
for m in re.finditer(r'<g id="node\d+" class="node">\s*<title>(.*?)</title>(.*?)</g>', svg, re.S):
    nums = [float(v) for v in re.findall(r"(-?\d+\.?\d*),(-?\d+\.?\d*)", m.group(2)) for v in v]
    xs, ys = nums[0::2], nums[1::2]
    cajas[unescape(m.group(1))] = (min(xs), min(ys), max(xs), max(ys))


def dist_caja(p, c):
    x, y = p
    dx = max(c[0] - x, 0, x - c[2])
    dy = max(c[1] - y, 0, y - c[3])
    return math.hypot(dx, dy)


def puntos(d):
    return [(float(a), float(b)) for a, b in re.findall(r"(-?\d+\.?\d*),(-?\d+\.?\d*)", d)]


def linea(p, q):
    return f'<line x1="{p[0]:.1f}" y1="{p[1]:.1f}" x2="{q[0]:.1f}" y2="{q[1]:.1f}" stroke="{COLOR}" stroke-width="1.4"/>'


def simbolos(fin, ant, partes):
    """fin: punto sobre el borde del nodo; ant: punto previo de la ruta. partes: lista de símbolos desde el nodo hacia afuera."""
    ux, uy = fin[0] - ant[0], fin[1] - ant[1]
    n = math.hypot(ux, uy) or 1
    ux, uy = ux / n, uy / n                 # dirección hacia el nodo
    px, py = -uy, ux                        # perpendicular
    at = lambda d, s=0: (fin[0] - ux * d + px * s, fin[1] - uy * d + py * s)
    out, d = [], 0.0
    for s in partes:
        if s == "pata":                     # pata de gallo: tres patas que tocan el borde
            apex = at(12)
            out += [linea(apex, at(0, -6.5)), linea(apex, at(0, 0)), linea(apex, at(0, 6.5))]
            d = 12
        elif s == "barra":
            d += 5
            out.append(linea(at(d, -6), at(d, 6)))
        elif s == "circulo":
            d += 8
            c = at(d)
            out.append(f'<circle cx="{c[0]:.1f}" cy="{c[1]:.1f}" r="4" fill="white" stroke="{COLOR}" stroke-width="1.4"/>')
            d += 4
    return "".join(out)


def procesar(m):
    titulo = unescape(m.group(1))
    cuerpo = m.group(2)
    clave = titulo.replace("&#45;", "-")
    r = rels.get(clave)
    if not r:
        return m.group(0)
    d = re.search(r'<path[^>]* d="([^"]+)"', cuerpo).group(1)
    pts = puntos(d)
    a, b = pts[0], pts[-1]
    pa, pb = pts[1], pts[-2]
    cp, ch = cajas[r["padre"]], cajas[r["hijo"]]
    # ¿qué extremo toca al padre?
    if dist_caja(a, cp) + dist_caja(b, ch) <= dist_caja(b, cp) + dist_caja(a, ch):
        fin_p, ant_p, fin_h, ant_h = a, pa, b, pb
    else:
        fin_p, ant_p, fin_h, ant_h = b, pb, a, pa
    lado_padre = ["barra", "circulo"] if r["fk_nula"] else ["barra", "barra"]
    if r["unica"]:
        lado_hijo = ["barra", "barra"]
    else:
        lado_hijo = ["pata", "circulo"] if r["fk_nula"] else ["pata", "barra"]
    extra = simbolos(fin_p, ant_p, lado_padre) + simbolos(fin_h, ant_h, lado_hijo)
    return m.group(0).replace("</g>", extra + "</g>", 1) if False else m.group(0)[:-4] + extra + "</g>"


svg = re.sub(r'<g id="edge\d+" class="edge">\s*<title>(.*?)</title>(.*?)</g>', procesar, svg, flags=re.S)
open(sys.argv[2], "w", encoding="utf-8").write(svg)
print(f"relaciones dibujadas: {len(rels)} · nodos: {len(cajas)}")
