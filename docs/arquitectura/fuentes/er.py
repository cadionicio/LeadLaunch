"""Genera el diagrama entidad-relación de LeadLaunch a partir del DDL real (database/01_ddl.sql)."""
import re
import sys

DDL = open(sys.argv[1], encoding="utf-8").read()
MODO = sys.argv[2]  # "completo" | "resumen"

GRUPOS = {
    "Catálogo (configuración)": ["cat_rubros", "cat_tipos_friccion", "cat_politicas_negocio", "cat_reglas_negocio"],
    "Parque (entidades)": ["usuarios", "negocios", "productos", "competidores_locales"],
    "Eventos (transacciones)": ["eventos_escaneo_radar", "hallazgos_friccion", "campanas_adaptativas",
                                "cotizaciones_cierre", "cotizacion_detalles", "bitacora_auditoria"],
}
COLOR = {"Catálogo (configuración)": "#7754E8", "Parque (entidades)": "#175CF4", "Eventos (transacciones)": "#113CBA"}
# Atributos que se muestran en la versión resumida (además de PK y FK)
CLAVE = {
    "cat_rubros": ["codigo", "nombre"], "cat_tipos_friccion": ["codigo", "nombre"],
    "cat_politicas_negocio": ["permite_express", "permite_acumular_desc", "acepta_yape_plin"],
    "cat_reglas_negocio": ["tipo_regla", "valor_porcentaje", "activa"],
    "usuarios": ["correo", "password_hash", "rol"],
    "negocios": ["razon_comercial", "radio_cobertura_km", "monto_ventas_ganadas"],
    "productos": ["sku", "precio_base", "stock_disponible"],
    "competidores_locales": ["nombre", "calificacion_prom"],
    "eventos_escaneo_radar": ["fuente_datos", "total_fricciones"],
    "hallazgos_friccion": ["calificacion", "resumen_dolor"],
    "campanas_adaptativas": ["alerta_adaptativa", "post_redes", "guion_reel_15s"],
    "cotizaciones_cierre": ["total_neto", "snapshot_reglas_json", "estado"],
    "cotizacion_detalles": ["cantidad", "precio_unitario_snap"],
    "bitacora_auditoria": ["entidad_afectada", "accion", "detalle_json"],
}

TIPOS = [("UUID", "uuid"), ("SMALLSERIAL", "smallserial"), ("BIGSERIAL", "bigserial"), ("SMALLINT", "smallint"),
         ("INTEGER", "int"), ("BOOLEAN", "bool"), ("TIMESTAMPTZ", "timestamptz"), ("JSONB", "jsonb"),
         ("TEXT", "text"), ("DATE", "date")]


def tipo_corto(t):
    t = t.upper()
    m = re.match(r"(VARCHAR|NUMERIC)\(([\d,]+)\)", t)
    if m:
        return ("varchar" if m.group(1) == "VARCHAR" else "numeric") + f"({m.group(2)})"
    for a, b in TIPOS:
        if t.startswith(a):
            return b
    return t.lower()


tablas, fks = {}, []
for nombre, cuerpo in re.findall(r"CREATE TABLE (\w+) \((.*?)\n\)", DDL, re.S):
    cols = []
    for ln in cuerpo.split("\n"):
        ln = ln.strip().rstrip(",")
        m = re.match(r"(\w+)\s+([A-Z]+(?:\([\d,]+\))?)", ln)
        if not m or m.group(1) in ("CHECK", "UNIQUE", "PRIMARY"):
            continue
        col, tipo = m.group(1), tipo_corto(m.group(2))
        pk = "PRIMARY KEY" in ln
        ref = re.search(r"REFERENCES (\w+)\((\w+)\)", ln)
        null = "NOT NULL" not in ln and not pk
        cols.append((col, tipo, pk, bool(ref), null))
        if ref:
            fks.append((nombre, col, ref.group(1), ref.group(2), null, "UNIQUE" in ln))
    tablas[nombre] = cols

F = "Inter"


def nodo(t, color):
    filas = []
    for col, tipo, pk, fk, null in tablas[t]:
        if MODO == "resumen" and not (pk or fk or col in CLAVE[t]):
            continue
        marca = "PK" if pk else ("FK" if fk else "")
        mc = color if pk else "#68738A"
        nombre_col = f"<B>{col}</B>" if pk else col
        filas.append(
            f'<TR><TD ALIGN="LEFT" WIDTH="22">{f'<FONT POINT-SIZE="9" COLOR="{mc}"><B>{marca}</B></FONT>' if marca else ' '}</TD>'
            f'<TD ALIGN="LEFT" PORT="{col}"><FONT POINT-SIZE="11" COLOR="#16233E">{nombre_col}</FONT></TD>'
            f'<TD ALIGN="RIGHT"><FONT POINT-SIZE="9" COLOR="#68738A">{tipo}{"?" if null else ""}</FONT></TD></TR>')
    ocultas = len(tablas[t]) - len(filas)
    if ocultas:
        filas.append(f'<TR><TD></TD><TD ALIGN="LEFT" COLSPAN="2"><FONT POINT-SIZE="9" COLOR="#68738A"><I>+ {ocultas} atributos</I></FONT></TD></TR>')
    return (f'  {t} [label=<<TABLE BORDER="1" COLOR="#C9D5E8" CELLBORDER="0" CELLSPACING="0" CELLPADDING="4" BGCOLOR="white">'
            f'<TR><TD COLSPAN="3" BGCOLOR="{color}" ALIGN="LEFT"><FONT COLOR="white" POINT-SIZE="12"><B>{t}</B></FONT></TD></TR>'
            + "".join(filas) + "</TABLE>>];")


out = [f'digraph ER {{ graph [rankdir=TB, splines=ortho, nodesep=0.55, ranksep=0.75, newrank=true, fontname="{F}", bgcolor="white", pad=0.3];',
       f'  node [shape=plain, fontname="{F}"]; edge [fontname="{F}", color="#5B6B85", penwidth=1.3, arrowsize=0.9, dir=both];']
for i, (g, ts) in enumerate(GRUPOS.items()):
    out += [nodo(t, COLOR[g]) for t in ts]
import json
json.dump([{"padre": p_, "hijo": h_, "fk_nula": n_, "unica": u_} for h_, c_, p_, pc_, n_, u_ in fks],
          open("er_rels.json", "w"))
for hijo, col, padre, pcol, null, unico in fks:
    # sin flechas: los símbolos de cardinalidad los dibuja cardinalidad.py en el extremo correcto
    out.append(f'  {padre} -> {hijo} [dir=none];')
# Bitácora: sin FK, la alimentan triggers
out.append('  cotizaciones_cierre -> bitacora_auditoria [style=dashed, dir=forward, arrowhead=vee, color="#7754E8", xlabel=<<FONT POINT-SIZE="10" COLOR="#7754E8">trigger</FONT>>];')
# Vista materializada
out.append(f'  mv [label=<<TABLE BORDER="1" STYLE="dashed" COLOR="#129E61" CELLBORDER="0" CELLPADDING="5" BGCOLOR="white">'
           f'<TR><TD BGCOLOR="#129E61"><FONT COLOR="white" POINT-SIZE="12"><B>mv_resumen_comercial_negocio</B></FONT></TD></TR>'
           f'<TR><TD ALIGN="LEFT"><FONT POINT-SIZE="10" COLOR="#16233E">Vista materializada del dashboard<BR ALIGN="LEFT"/>'
           f'1 acceso indexado por negocio_id</FONT></TD></TR></TABLE>>];')
out.append('  negocios -> mv [style=dashed, dir=forward, arrowhead=vee, color="#129E61"];')
out.append('  { rank=same; usuarios; cat_rubros; cat_tipos_friccion; }')
out.append('  { rank=same; negocios; competidores_locales; }')
out.append('  { rank=same; cat_politicas_negocio; cat_reglas_negocio; productos; eventos_escaneo_radar; cotizaciones_cierre; }')
out.append('  { rank=same; campanas_adaptativas; hallazgos_friccion; cotizacion_detalles; bitacora_auditoria; mv; }')
ley = "".join(f'<TR><TD BGCOLOR="{c}" WIDTH="14" HEIGHT="14"></TD><TD ALIGN="LEFT"><FONT POINT-SIZE="11" COLOR="#16233E">{g}</FONT></TD></TR>' for g, c in COLOR.items())
ley += '<TR><TD></TD><TD ALIGN="LEFT"><FONT POINT-SIZE="10" COLOR="#68738A">PK clave primaria · FK clave foránea · ? admite nulo<BR ALIGN="LEFT"/>Notación pata de gallo: ‖ uno · ○ cero · ⋔ muchos</FONT></TD></TR>'
out.append(f'  leyenda [label=<<TABLE BORDER="0" CELLSPACING="4" CELLPADDING="2">{ley}</TABLE>>];')
out.append('  { rank=same; leyenda; usuarios; }')
out.append('  leyenda -> usuarios [style=invis, dir=none];')
out.append("}")
print("\n".join(out))
