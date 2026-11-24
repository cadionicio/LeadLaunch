"""Aplica el DDL + semilla del documento maestro (§4.3) en la base de DATABASE_URL (backend/.env).

Uso (desde la carpeta backend, con el entorno virtual activo):
    python -m scripts.aplicar_ddl

Es seguro ejecutarlo dos veces: si las tablas ya existen, no hace nada.
Todo se aplica en una sola transacción: si algo falla, no queda nada a medias.
"""
import re
import sys
from pathlib import Path

import psycopg

from app.core.config import settings

RAIZ = Path(__file__).resolve().parents[2]
DOC = RAIZ / "docs" / "especificaciones" / "Documento_Maestro_LeadLaunch.md"


def extraer_sql() -> str:
    texto = DOC.read_text(encoding="utf-8")
    bloque = re.search(r"```sql\s*\n(.*?)```", texto, re.S)
    if not bloque:
        sys.exit(f"No se encontró el bloque SQL en {DOC}")
    return bloque.group(1)


def main() -> None:
    if not settings.database_url:
        sys.exit("DATABASE_URL está vacía en backend/.env")

    sql = extraer_sql()
    with psycopg.connect(settings.database_url) as conn:
        if conn.execute("SELECT to_regclass('public.usuarios')").fetchone()[0]:
            print("La base ya tiene las tablas de LeadLaunch. No se aplicó nada.")
            return
        conn.execute(sql)
        conn.commit()

        tablas = conn.execute(
            "SELECT count(*) FROM information_schema.tables "
            "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'"
        ).fetchone()[0]
        rubros = conn.execute("SELECT count(*) FROM cat_rubros").fetchone()[0]
        fricciones = conn.execute("SELECT count(*) FROM cat_tipos_friccion").fetchone()[0]
        vista = conn.execute("SELECT to_regclass('public.mv_resumen_comercial_negocio')").fetchone()[0]

    print("DDL aplicado correctamente.")
    print(f"  Tablas: {tablas} (esperado 14)")
    print(f"  Rubros: {rubros} (esperado 3) · Tipos de fricción: {fricciones} (esperado 5)")
    print(f"  Vista del dashboard: {'OK' if vista else 'NO ENCONTRADA'}")


if __name__ == "__main__":
    main()
