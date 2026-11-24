"""Compara la base de DATABASE_URL (backend/.env) con el esquema del documento maestro (§4.3).

Uso (desde la carpeta backend, con el entorno virtual activo):
    python -m scripts.verificar_bd

Solo LEE la base: no crea, modifica ni borra nada.
Revisa tablas, columnas (tipo, nulos, default), restricciones CHECK/PK/FK/UNIQUE, índices,
fillfactor, triggers, funciones, la vista materializada, la extensión pgcrypto y la semilla.
"""
import json
import re
import sys
from pathlib import Path

import psycopg

from app.core.config import settings

ESPERADO = Path(__file__).with_name("esquema_esperado.json")


def huella(conn: psycopg.Connection) -> dict:
    q = lambda sql: conn.execute(sql).fetchall()  # noqa: E731
    h: dict = {}

    h["extensiones"] = sorted(r[0] for r in q("SELECT extname FROM pg_extension WHERE extname <> 'plpgsql'"))

    h["tablas"] = {}
    for (tabla,) in q("""SELECT table_name FROM information_schema.tables
                         WHERE table_schema='public' AND table_type='BASE TABLE' ORDER BY 1"""):
        cols = q(f"""SELECT column_name, data_type, character_maximum_length, numeric_precision,
                            numeric_scale, is_nullable, coalesce(column_default,'')
                       FROM information_schema.columns
                      WHERE table_schema='public' AND table_name='{tabla}' ORDER BY column_name""")
        restr = q(f"""SELECT pg_get_constraintdef(c.oid) FROM pg_constraint c
                       WHERE c.conrelid = 'public.{tabla}'::regclass
                         AND c.contype <> 'n' ORDER BY 1""")  # PG18 guarda NOT NULL como restricción; ya se revisa en columnas
        opts = q(f"SELECT coalesce(reloptions, '{{}}') FROM pg_class WHERE oid='public.{tabla}'::regclass")[0][0]
        h["tablas"][tabla] = {
            "columnas": {c[0]: [c[1], c[2], c[3], c[4], c[5], c[6]] for c in cols},
            "restricciones": sorted(r[0] for r in restr),
            "opciones": sorted(opts),
        }

    h["indices"] = sorted(
        re.sub(r"INDEX \S+ ON", "INDEX ON", r[0])
        for r in q("SELECT indexdef FROM pg_indexes WHERE schemaname='public' AND indexname NOT LIKE '%_pkey' "
                   "AND indexname NOT LIKE '%_key'")
    )
    h["triggers"] = sorted(
        f"{r[0]} ON {r[1]} -> {r[2]}"
        for r in q("""SELECT t.tgname, c.relname, p.proname FROM pg_trigger t
                      JOIN pg_class c ON c.oid=t.tgrelid JOIN pg_proc p ON p.oid=t.tgfoid
                      WHERE NOT t.tgisinternal""")
    )
    h["funciones"] = sorted(
        r[0] for r in q("""SELECT p.proname FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace
                           WHERE n.nspname='public'""")
    )
    h["vista_materializada"] = sorted(
        r[0] for r in q("""SELECT a.attname FROM pg_attribute a
                           WHERE a.attrelid = to_regclass('public.mv_resumen_comercial_negocio')
                             AND a.attnum > 0 AND NOT a.attisdropped""")
    ) if q("SELECT to_regclass('public.mv_resumen_comercial_negocio')")[0][0] else []
    h["semilla"] = {
        "cat_rubros": sorted(r[0] for r in q("SELECT codigo FROM cat_rubros"))
        if "cat_rubros" in h["tablas"] else [],
        "cat_tipos_friccion": sorted(r[0] for r in q("SELECT codigo FROM cat_tipos_friccion"))
        if "cat_tipos_friccion" in h["tablas"] else [],
    }
    return h


def comparar(esperado: dict, real: dict) -> list[str]:
    p: list[str] = []

    for ext in set(esperado["extensiones"]) - set(real["extensiones"]):
        p.append(f"Falta la extensión {ext}")

    for t in sorted(set(esperado["tablas"]) - set(real["tablas"])):
        p.append(f"Falta la tabla {t}")
    for t in sorted(set(real["tablas"]) - set(esperado["tablas"])):
        p.append(f"Tabla adicional (no está en el documento): {t}")

    for t in sorted(set(esperado["tablas"]) & set(real["tablas"])):
        e, r = esperado["tablas"][t], real["tablas"][t]
        for c in sorted(set(e["columnas"]) - set(r["columnas"])):
            p.append(f"{t}: falta la columna {c}")
        for c in sorted(set(r["columnas"]) - set(e["columnas"])):
            p.append(f"{t}: columna adicional {c}")
        for c in sorted(set(e["columnas"]) & set(r["columnas"])):
            if e["columnas"][c] != r["columnas"][c]:
                p.append(f"{t}.{c}: definición distinta. Esperado {e['columnas'][c]} · Real {r['columnas'][c]}")
        for x in sorted(set(e["restricciones"]) - set(r["restricciones"])):
            p.append(f"{t}: falta la restricción  {x}")
        for x in sorted(set(r["restricciones"]) - set(e["restricciones"])):
            p.append(f"{t}: restricción adicional  {x}")
        if e["opciones"] != r["opciones"]:
            p.append(f"{t}: opciones de almacenamiento distintas. Esperado {e['opciones']} · Real {r['opciones']}")

    for i in sorted(set(esperado["indices"]) - set(real["indices"])):
        p.append(f"Falta el índice  {i}")
    for nombre in ("triggers", "funciones"):
        for x in sorted(set(esperado[nombre]) - set(real[nombre])):
            p.append(f"Falta {nombre[:-1]}: {x}")

    if not real["vista_materializada"]:
        p.append("Falta la vista materializada mv_resumen_comercial_negocio")
    elif esperado["vista_materializada"] != real["vista_materializada"]:
        p.append("La vista materializada tiene columnas distintas a las del documento")

    for tabla, codigos in esperado["semilla"].items():
        faltan = set(codigos) - set(real["semilla"].get(tabla, []))
        if faltan:
            p.append(f"Semilla incompleta en {tabla}: faltan {sorted(faltan)}")
    return p


def main() -> None:
    url = settings.database_url
    if "--generar" in sys.argv:  # solo para regenerar la referencia desde una base limpia con el DDL oficial
        with psycopg.connect(sys.argv[sys.argv.index("--generar") + 1]) as conn:
            ESPERADO.write_text(json.dumps(huella(conn), indent=1, ensure_ascii=False), encoding="utf-8")
        print(f"Referencia guardada en {ESPERADO}")
        return
    if not url:
        sys.exit("DATABASE_URL está vacía en backend/.env")

    esperado = json.loads(ESPERADO.read_text(encoding="utf-8"))
    with psycopg.connect(url) as conn:
        real = huella(conn)
        conn.rollback()

    problemas = comparar(esperado, real)
    print(f"Tablas encontradas: {len(real['tablas'])} (esperado {len(esperado['tablas'])})")
    print(f"Índices: {len(real['indices'])} · Triggers: {len(real['triggers'])} · "
          f"Vista materializada: {'OK' if real['vista_materializada'] else 'NO'}")
    print(f"Semilla: {len(real['semilla']['cat_rubros'])} rubros · "
          f"{len(real['semilla']['cat_tipos_friccion'])} tipos de fricción")
    if problemas:
        print(f"\nRESULTADO: {len(problemas)} diferencia(s) con el documento maestro:")
        for x in problemas:
            print("  -", x)
        sys.exit(1)
    print("\nRESULTADO: la base coincide al 100 % con el documento maestro.")


if __name__ == "__main__":
    main()
