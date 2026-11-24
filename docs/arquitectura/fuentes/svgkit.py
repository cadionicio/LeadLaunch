"""Mini librería SVG para diagramas de arquitectura con coordenadas exactas (paleta LeadLaunch)."""
from html import escape

AZUL, AZUL_OSC, INDIGO, VERDE, ROSA = "#175CF4", "#113CBA", "#7754E8", "#129E61", "#D94D69"
TEXTO, GRIS, BORDE, SUAVE, FONDO = "#16233E", "#68738A", "#C9D5E8", "#EAF0FF", "#F8FAFE"
LINEA = "#5B6B85"
FUENTE = "Inter, Arial, sans-serif"


class Lienzo:
    def __init__(self, w, h):
        self.w, self.h, self.el = w, h, []

    def rect(self, x, y, w, h, fill="white", stroke=BORDE, sw=1.2, rx=10, dash=False):
        d = ' stroke-dasharray="6 4"' if dash else ""
        self.el.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{rx}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"{d}/>')

    def texto(self, x, y, s, size=13, weight=400, fill=TEXTO, anchor="start", italic=False):
        st = ' font-style="italic"' if italic else ""
        self.el.append(f'<text x="{x}" y="{y}" font-family="{FUENTE}" font-size="{size}" font-weight="{weight}" fill="{fill}" text-anchor="{anchor}"{st}>{escape(s)}</text>')

    def comp(self, x, y, w, h, titulo, sub="", dash=False, fill="white", stroke=BORDE, tcolor=TEXTO, size=13):
        self.rect(x, y, w, h, fill=fill, stroke=stroke, dash=dash)
        cy = y + h / 2
        if sub:
            self.texto(x + w / 2, cy - 3, titulo, size, 600, tcolor, "middle")
            for i, linea in enumerate(sub.split("\n")):
                self.texto(x + w / 2, cy + 14 + i * 14, linea, 11, 400, GRIS, "middle")
        else:
            self.texto(x + w / 2, cy + 5, titulo, size, 600, tcolor, "middle")

    def banda(self, x, y, w, h, titulo, sub, color, fill="white"):
        self.rect(x, y, w, h, fill=fill, stroke=color, sw=1.6, rx=12)
        self.texto(x + 16, y + 24, titulo, 13, 700, color)
        if sub:
            self.texto(x + 16 + len(titulo) * 8.6 + 14, y + 24, sub, 12, 400, GRIS)

    def flecha(self, pts, dash=False, doble=False, color=LINEA, sw=1.6, etiqueta=None, epos=None, eanchor="start"):
        d = "M " + " L ".join(f"{x},{y}" for x, y in pts)
        da = ' stroke-dasharray="6 4"' if dash else ""
        ms = ' marker-start="url(#flechaI)"' if doble else ""
        self.el.append(f'<path d="{d}" fill="none" stroke="{color}" stroke-width="{sw}"{da} marker-end="url(#flecha)"{ms}/>')
        if etiqueta:
            ex, ey = epos
            for i, l in enumerate(etiqueta.split("\n")):
                self.texto(ex, ey + i * 15, l, 12, 600 if i == 0 else 400, TEXTO if i == 0 else GRIS, eanchor)

    def cilindro(self, x, y, w, h, titulo, sub, color=VERDE, fill=SUAVE):
        ry = 10
        self.el.append(
            f'<path d="M {x},{y + ry} A {w / 2},{ry} 0 0 1 {x + w},{y + ry} L {x + w},{y + h - ry} A {w / 2},{ry} 0 0 1 {x},{y + h - ry} Z" '
            f'fill="{fill}" stroke="{color}" stroke-width="1.6"/>')
        self.el.append(f'<path d="M {x},{y + ry} A {w / 2},{ry} 0 0 0 {x + w},{y + ry}" fill="none" stroke="{color}" stroke-width="1.6"/>')
        self.texto(x + w / 2, y + h / 2 + 4, titulo, 13, 600, TEXTO, "middle")
        for i, l in enumerate(sub.split("\n")):
            self.texto(x + w / 2, y + h / 2 + 21 + i * 14, l, 11, 400, GRIS, "middle")

    def svg(self):
        defs = (f'<defs><marker id="flecha" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="8" markerHeight="8" orient="auto">'
                f'<path d="M0,0 L10,5 L0,10 z" fill="{LINEA}"/></marker>'
                f'<marker id="flechaI" viewBox="0 0 10 10" refX="1" refY="5" markerWidth="8" markerHeight="8" orient="auto">'
                f'<path d="M10,0 L0,5 L10,10 z" fill="{LINEA}"/></marker></defs>')
        return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.w}" height="{self.h}" viewBox="0 0 {self.w} {self.h}">'
                f'<rect width="100%" height="100%" fill="white"/>{defs}' + "".join(self.el) + "</svg>")

    def guardar(self, ruta):
        open(ruta, "w", encoding="utf-8").write(self.svg())


def leyenda(c, x, y):
    c.rect(x, y, 26, 16, rx=4)
    c.texto(x + 34, y + 12, "Implementado (Fase 1)", 11, 400, TEXTO)
    c.rect(x, y + 24, 26, 16, rx=4, dash=True)
    c.texto(x + 34, y + 36, "En desarrollo (Fase 2)", 11, 400, TEXTO)
