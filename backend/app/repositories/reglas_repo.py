"""Capa 3: políticas (cat_politicas_negocio) y reglas (cat_reglas_negocio)."""
from uuid import UUID

from psycopg import Connection

DESCRIPCIONES = {
    "RECARGO_EXPRESS": "Recargo por preparación prioritaria",
    "DESC_BIENVENIDA": "Descuento para clientes nuevos",
    "DESC_VOLUMEN": "Descuento por volumen",
}


def obtener_politica(conn: Connection, negocio_id: UUID) -> dict | None:
    return conn.execute(
        """SELECT permite_express, permite_acumular_desc, acepta_yape_plin, horario_atencion
             FROM cat_politicas_negocio WHERE negocio_id = %s""",
        (negocio_id,),
    ).fetchone()


def guardar_politica(conn: Connection, negocio_id: UUID, p: dict) -> None:
    conn.execute(
        """INSERT INTO cat_politicas_negocio (negocio_id, permite_express, permite_acumular_desc,
                                              acepta_yape_plin, horario_atencion)
           VALUES (%(negocio_id)s, %(permite_express)s, %(permite_acumular_desc)s,
                   %(acepta_yape_plin)s, %(horario_atencion)s)
           ON CONFLICT (negocio_id) DO UPDATE
              SET permite_express = EXCLUDED.permite_express,
                  permite_acumular_desc = EXCLUDED.permite_acumular_desc,
                  acepta_yape_plin = EXCLUDED.acepta_yape_plin,
                  horario_atencion = EXCLUDED.horario_atencion,
                  actualizado_en = NOW()""",
        {**p, "negocio_id": negocio_id},
    )


def listar_reglas(conn: Connection, negocio_id: UUID) -> list[dict]:
    """Una regla por tipo (la más reciente si hubiera varias)."""
    return conn.execute(
        """SELECT DISTINCT ON (tipo_regla)
                  regla_id, tipo_regla, descripcion, valor_porcentaje::float AS valor_porcentaje,
                  monto_minimo::float AS monto_minimo, cantidad_minima, activa
             FROM cat_reglas_negocio
            WHERE negocio_id = %s
            ORDER BY tipo_regla, vigente_desde DESC, regla_id""",
        (negocio_id,),
    ).fetchall()


def guardar_regla(conn: Connection, negocio_id: UUID, r: dict) -> None:
    """Actualiza la regla existente de ese tipo o la crea si no existe."""
    descripcion = r.get("descripcion") or DESCRIPCIONES[r["tipo_regla"]]
    existente = conn.execute(
        """SELECT regla_id FROM cat_reglas_negocio
            WHERE negocio_id = %s AND tipo_regla = %s
            ORDER BY vigente_desde DESC, regla_id LIMIT 1 FOR UPDATE""",
        (negocio_id, r["tipo_regla"]),
    ).fetchone()
    params = {**r, "descripcion": descripcion, "negocio_id": negocio_id}
    if existente:
        conn.execute(
            """UPDATE cat_reglas_negocio
                  SET valor_porcentaje = round(%(valor_porcentaje)s::numeric, 2), activa = %(activa)s,
                      descripcion = %(descripcion)s, monto_minimo = round(%(monto_minimo)s::numeric, 2),
                      cantidad_minima = %(cantidad_minima)s
                WHERE regla_id = %(regla_id)s""",
            {**params, "regla_id": existente["regla_id"]},
        )
    else:
        conn.execute(
            """INSERT INTO cat_reglas_negocio (negocio_id, tipo_regla, descripcion, monto_minimo,
                                               cantidad_minima, valor_porcentaje, activa)
               VALUES (%(negocio_id)s, %(tipo_regla)s, %(descripcion)s, round(%(monto_minimo)s::numeric, 2),
                       %(cantidad_minima)s, round(%(valor_porcentaje)s::numeric, 2), %(activa)s)""",
            params,
        )
