"""Regresión: la API debe recuperarse sola si la base corta las conexiones (Neon suspendida)."""
import psycopg

from app.core.config import settings


def test_reconexion_tras_corte_de_la_base(db_client):
    assert db_client.get("/health/db").status_code == 200
    with psycopg.connect(settings.database_url, autocommit=True) as k:
        k.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity "
                  "WHERE datname = current_database() AND pid <> pg_backend_pid()")
    assert [db_client.get("/health/db").status_code for _ in range(3)] == [200, 200, 200]
