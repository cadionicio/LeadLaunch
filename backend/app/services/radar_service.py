"""Capa 2: RadarService — escaneo de competidores y fricciones (HU-03, lógica compleja 1).

1. Obtiene competidores del rubro dentro del radio (hoy: dataset de respaldo, ver radar_fuentes.py).
2. Filtra reseñas de 1 a 3 estrellas con texto (código puro, sin IA).
3. Clasifica cada reseña en cat_tipos_friccion por palabras clave.
4. Persiste escaneo + competidores + hallazgos en UNA transacción y refresca el dashboard.
   Una reseña ya guardada para el mismo negocio y competidor no se duplica.
"""
from collections import Counter
from uuid import UUID

from app.core.database import get_conn
from app.repositories import negocios_repo, radar_repo
from app.schemas.radar import CompetidorOut, FriccionOut, RadarScanOut, ResumenFriccionOut
from app.services import friccion_clasificador as clasificador
from app.services import radar_fuentes
from app.services.business_service import verificar_propiedad


def _resumen(tipo_nombre: str, texto: str) -> str:
    texto = " ".join(texto.split())
    resumen = f"{tipo_nombre}: {texto}"
    return resumen if len(resumen) <= 250 else resumen[:247].rstrip() + "..."


def escanear(usuario_id: UUID, negocio_id: UUID, radio_km: float | None) -> RadarScanOut:
    with get_conn() as conn:
        negocio = verificar_propiedad(conn, negocio_id, usuario_id)
        radio = float(radio_km if radio_km is not None else negocio["radio_cobertura_km"])
        rubro = radar_repo.rubro_codigo(conn, negocio["rubro_id"])
        tipos = radar_repo.tipos_friccion(conn)

        competidores = radar_fuentes.competidores_dataset(
            str(negocio_id), rubro, negocio["latitud"], negocio["longitud"], radio
        )
        fuente = radar_fuentes.FUENTE_DATASET

        # Análisis (antes de persistir, para guardar el evento con sus totales).
        total_analizados = 0
        analisis: list[tuple[radar_fuentes.Competidor, list[tuple[str, int, str]]]] = []
        for c in competidores:
            total_analizados += len(c.resenas)
            fricciones = []
            for r in c.resenas:
                if not clasificador.es_friccion(r.calificacion, r.texto):
                    continue
                tipo = clasificador.clasificar(r.texto)
                if tipo and tipo in tipos:
                    fricciones.append((tipo, r.calificacion, r.texto.strip()))
            analisis.append((c, fricciones))
        total_fricciones = sum(len(f) for _, f in analisis)

        escaneo = radar_repo.crear_escaneo(conn, negocio_id, fuente, radio, total_analizados, total_fricciones)

        competidores_out: list[CompetidorOut] = []
        conteo: Counter[str] = Counter()
        nuevas = 0
        for c, fricciones in analisis:
            competidor_id = radar_repo.upsert_competidor(
                conn, c.fuente_externa_id, negocio["rubro_id"], c.nombre, negocio["distrito"],
                c.latitud, c.longitud, c.calificacion_prom,
            )
            fricciones_out = []
            for tipo, calificacion, texto in fricciones:
                t = tipos[tipo]
                resumen = _resumen(t["nombre"], texto)
                if not radar_repo.hallazgo_existe(conn, negocio_id, competidor_id, texto):
                    radar_repo.insertar_hallazgo(conn, escaneo["escaneo_id"], negocio_id, competidor_id,
                                                 t["tipo_friccion_id"], calificacion, texto, resumen)
                    nuevas += 1
                conteo[tipo] += 1
                fricciones_out.append(FriccionOut(tipo_codigo=tipo, tipo_nombre=t["nombre"],
                                                  calificacion=calificacion, texto_resena=texto,
                                                  resumen_dolor=resumen))
            competidores_out.append(CompetidorOut(
                competidor_id=competidor_id, nombre=c.nombre, distrito=negocio["distrito"],
                latitud=c.latitud, longitud=c.longitud, distancia_km=c.distancia_km,
                calificacion_prom=c.calificacion_prom, total_resenas=len(c.resenas),
                fricciones=fricciones_out,
            ))

    with get_conn() as conn:
        negocios_repo.refrescar_resumen(conn)

    es_demo = fuente == radar_fuentes.FUENTE_DATASET
    return RadarScanOut(
        escaneo_id=escaneo["escaneo_id"],
        fuente_datos=fuente,
        es_demo=es_demo,
        aviso=radar_fuentes.AVISO_DEMO if es_demo else None,
        radio_busqueda_km=round(radio, 2),
        total_competidores=len(competidores_out),
        total_analizados=total_analizados,
        total_fricciones=total_fricciones,
        nuevas_fricciones=nuevas,
        ejecutado_en=escaneo["ejecutado_en"],
        competidores=competidores_out,
        resumen_fricciones=[
            ResumenFriccionOut(tipo_codigo=k, tipo_nombre=tipos[k]["nombre"], cantidad=v)
            for k, v in conteo.most_common()
        ],
    )
