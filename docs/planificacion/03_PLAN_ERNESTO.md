# PLAN INDIVIDUAL — ERNESTO
**Rol:** Base de datos en la nube y pruebas (QA)
**Rama:** `ernesto/db-qa` · **Carpetas:** `/database`, `/docs/pruebas`
**Leer antes:** `00_PLAN_GENERAL.md` (§5 ramas, §7 contrato API) y el documento maestro §4.3 (DDL) y §7.2 (matriz de pruebas).

---

## 1. Responsabilidades
1. Crear la base de datos PostgreSQL en **Neon** (plan gratuito) y ejecutar el DDL.
2. Preparar las semillas (rubros, tipos de fricción y datos demo).
3. Armar el dataset de respaldo del radar (`radar_dataset.json`).
4. Crear la colección de pruebas de la API y ejecutar la matriz CP-01 a CP-10 con evidencias.

---

## 2. Tareas por checkpoint

### CP0 · Arranque — Lunes 23/11 · 23:30
- [ ] Clonar el repo y cambiar a `ernesto/db-qa` (ver `00_PLAN_GENERAL.md` §5.2).
- [ ] Crear cuenta y proyecto en Neon (región más cercana, PostgreSQL 15+).
- [ ] Copiar el DDL del documento maestro (§4.3) en `database/01_ddl.sql` **sin el bloque de semilla** y la semilla (rubros + tipos de fricción) en `database/02_seed.sql`.
- [ ] Ejecutar ambos scripts en el SQL Editor de Neon y verificar que existen las 13 tablas, la vista materializada y los triggers.
- [ ] Enviar a César el `DATABASE_URL` **por privado** (nunca en el repo ni en el grupo).
- [ ] Commit + push y PR → `develop`.

### CP1 · Cuenta y negocio — Martes 24/11 · 13:00
- [ ] `database/fallback/radar_dataset.json` con 3–5 competidores cerca de Miraflores (La Dulcería, Postres & Más, Sweet House…) y 5–8 reseñas cada uno, mezclando calificaciones 1–5. Formato sugerido:
  ```json
  [{ "fuente_externa_id": "demo-001", "nombre": "La Dulcería", "rubro_codigo": "REPOSTERIA",
     "distrito": "Miraflores", "latitud": -12.1211, "longitud": -77.0297, "calificacion_prom": 2.8,
     "resenas": [{ "calificacion": 2, "texto": "El pedido llegó una hora tarde." }] }]
  ```
  Las reseñas deben cubrir los 5 tipos de fricción (demora, presentación, precio/envío, atención, sin opción urgente). Son **datos inventados de demo** y así se debe indicar en el archivo.
- [ ] Crear la colección de pruebas (Postman o Bruno) en `docs/pruebas/` con variables `base_url` y `token`.
- [ ] Ejecutar contra el backend desplegado: **CP-01** (registro y login), **CP-06** (sin token → 401), **CP-07** (precio negativo / WhatsApp "abc" → 422).
- [ ] Guardar evidencia (captura o export) en `docs/pruebas/evidencias/`.
- [ ] PR → `develop`.

### CP2 · Catálogo, reglas, dashboard, radar — Martes 24/11 · 21:00
- [ ] `database/03_seed_demo.sql` o script con la API: usuario Ana Rodríguez, negocio Dulce Punto y los 4 productos de la maqueta (TOR-001, CHE-002, CUP-006, POS-011). Pasar las credenciales de prueba a Giancarlos.
- [ ] Ejecutar **CP-02** (alta de producto) y **CP-03** (escaneo de radar con radio 3 km → hallazgos ≤ 3 estrellas y `total_escaneos + 1`).
- [ ] Revisar que Swagger (`/docs` del backend) muestre todos los endpoints de la Fase 1; reportar a César lo que falte.
- [ ] Llenar la tabla de resultados en `docs/pruebas/RESULTADOS.md` (ID, fecha, resultado, evidencia).
- [ ] PR → `develop`.

### Fase 2 (por definir)
- [ ] Ejecutar CP-04, CP-05, CP-08, CP-09, CP-10.
- [ ] Revisar en la BD que la bitácora y los acumuladores se actualizan al cambiar una cotización a GANADA.

---

## 3. Consultas útiles para verificar
```sql
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
SELECT * FROM cat_rubros;
SELECT negocio_id, razon_comercial, total_escaneos FROM negocios;
SELECT * FROM mv_resumen_comercial_negocio;
SELECT * FROM bitacora_auditoria ORDER BY registrado_en DESC LIMIT 10;
```

## 4. Dependencias
- **César necesita de mí:** `DATABASE_URL` (CP0) y `radar_dataset.json` (antes de CP2).
- **Giancarlos necesita de mí:** usuario de prueba con negocio y productos (CP2).
- **Necesito de César:** URL del backend desplegado para correr las pruebas (CP1).
