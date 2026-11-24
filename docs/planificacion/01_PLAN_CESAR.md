# PLAN INDIVIDUAL — CÉSAR
**Rol:** Líder del equipo · Dueño del repositorio · Backend API (FastAPI)
**Rama:** `cesar/backend` · **Carpetas:** `/backend`, `docs/api/CONTRATOS_API.md`
**Leer antes:** `00_PLAN_GENERAL.md` (§5 ramas, §7 contrato API, §8 decisiones)

---

## 1. Responsabilidades
1. Crear el repositorio, las ramas y las reglas de protección (comandos en `00_PLAN_GENERAL.md` §5.1).
2. Revisar y aprobar los Pull Requests hacia `develop` y `main`.
3. Construir la API REST en 4 capas: routers (Capa 1) → services (Capa 2) → repositories (Capa 3) → PostgreSQL.
4. Desplegar el backend en Render o Koyeb con variables de entorno.
5. Mantener `docs/api/CONTRATOS_API.md` y avisar cualquier cambio de contrato.
6. Dirigir la llamada de cada checkpoint.

---

## 2. Tareas por checkpoint

### CP0 · Arranque — Lunes 23/11 · 23:30
- [x] Crear repo `leadlaunch`, ramas `develop`, `cesar/backend`, `giancarlos/android`, `ernesto/db-qa`.
- [x] Invitar colaboradores, default branch = `develop`, proteger `main` y `develop`.
- [x] Subir a `develop` la estructura de carpetas, `README.md`, `.gitignore` (Python + Android) y los 4 planes en `docs/planificacion/`.
- [x] Copiar los 2 Markdown fuente a `docs/especificaciones/`.
- [x] Crear esqueleto FastAPI: `app/main.py`, `core/config.py` (lee `.env`), `GET /health`.
- [x] `requirements.txt`: `fastapi`, `uvicorn[standard]`, `pydantic-settings`, `psycopg[binary]` o `sqlalchemy`, `passlib[bcrypt]` o `argon2-cffi`, `python-jose` o `pyjwt`, `httpx`.
- [x] `.env.example`: `DATABASE_URL`, `JWT_SECRET`, `JWT_EXPIRA_MIN`, `GEMINI_API_KEY`, `OVERPASS_URL`.
- [x] Manejador global de errores con el formato `{error:{codigo, mensaje, campos}}`.
- [x] Base en Neon creada con el DDL oficial y verificada al 100 % (`scripts/aplicar_ddl.py`, `scripts/verificar_bd.py`).

### CP1 · Cuenta y negocio — Martes 24/11 · 13:00
- [x] `AuthService` + router: `POST /auth/register` (201, correo único, hash bcrypt/Argon2) y `POST /auth/login` (200/401).
- [x] Respuesta de auth incluye `negocio_id` o `null` (decisión D2).
- [x] Dependencia `get_current_user` (valida JWT) y verificación de propiedad: si el `{id}` no es del usuario → **404**.
- [x] `BusinessService`: `POST /businesses` en **una transacción**: negocio + `cat_politicas_negocio` por defecto + reglas base (`RECARGO_EXPRESS` 15 %, `DESC_BIENVENIDA` 10 %, ambas activas).
- [x] Validaciones Pydantic: WhatsApp `^[0-9]{10,15}$`, radio 0.50–25.00, lat/lon en rango, descripción obligatoria.
- [x] Desplegar en Render/Koyeb y pasar la URL a Giancarlos y Ernesto.
- [x] Publicar `CONTRATOS_API.md` con lo ya implementado.
- [x] PR `cesar/backend` → `develop`.

### Asumido de `database/` (antes de Ernesto)
- [x] `database/01_ddl.sql` y `02_seed.sql` (copia fiel del §4.3, verificados al 100 %).
- [x] `database/fallback/radar_dataset.json`: 9 competidores ficticios, 37 reseñas, cubre los 5 tipos de fricción.
- [x] `scripts/cargar_demo.py`: cuenta Ana + Dulce Punto + 4 productos de la maqueta.

### CP2 · Catálogo, reglas, dashboard, radar — Martes 24/11 · 21:00
- [x] `CatalogService`: `POST /businesses/{id}/products` (precio > 0, horas ≥ 1, stock ≥ 0, SKU único → 422 `SKU_DUPLICADO`).
- [x] `GET /businesses/{id}/products` paginado, solo activos.
- [x] `PolicyService`: `GET` y `PUT /businesses/{id}/rules` (porcentajes 0–100).
- [x] `GET /businesses/{id}/dashboard` leyendo `mv_resumen_comercial_negocio`.
- [x] Refrescar la vista con `REFRESH MATERIALIZED VIEW CONCURRENTLY` tras crear producto y tras escanear (D11).
- [x] `RadarService` — `POST /businesses/{id}/radar/scan`:
  - [ ] ~~Intentar Overpass API~~ → reemplazado por la decisión D14 (Overpass no da reseñas; fuente real en Fase 2).
  - [x] Leer `database/fallback/radar_dataset.json` y marcar `fuente_datos = DATASET_FALLBACK`.
  - [x] Filtrar reseñas con calificación ≤ 3 y texto no vacío (código puro, sin IA).
  - [x] Clasificar cada reseña en `cat_tipos_friccion` (por palabras clave: "demora", "tarde", "caro", "envío", "dañado", "atención", "urgente"...).
  - [x] Persistir escaneo, competidores (upsert por `fuente_externa_id`) y hallazgos en una transacción. Responder 201 con el formato del §7.
- [ ] PR → `develop`. Revisar PRs de Giancarlos y Ernesto. Merge `develop` → `main`, tag `v0.5.0`:
  ```bash
  git checkout main && git pull
  git merge origin/develop   # (o por PR en GitHub)
  git tag v0.5.0 && git push origin main --tags
  ```

### Fase 2 (por definir)
- [ ] `AdaptiveAIService`: `POST /campaigns/generate` — 1 sola llamada al modelo Flash vigente, respuesta JSON con `alerta_adaptativa`, `post_redes`, `guion_reel_15s`; timeout, evitar llamadas duplicadas.
- [ ] `QuoteService`: `POST /quotes` — liquidación paramétrica, `snapshot_reglas_json`, mensaje y URL `wa.me`; 400 si pide express con `permite_express = false`.
- [ ] `PATCH /quotes/{quote_id}/status` + refresco de la vista.
- [ ] Endpoints extra según P10: `GET /quotes` (historial), `GET /quotes/{id}`, `PATCH /products/{id}`.

---

## 3. Notas técnicas importantes
- **SQL siempre parametrizado** (nunca f-strings con datos del usuario) → cubre CP-08.
- El `CHECK (total_neto = subtotal + monto_recargo - monto_descuento)` de la BD obliga a redondear a 2 decimales con `Decimal`, no `float`.
- El trigger de cotizaciones ya actualiza acumuladores y bitácora; no duplicar esa lógica en Python.
- `REFRESH ... CONCURRENTLY` requiere el índice único `idx_mv_resumen_negocio` (ya está en el DDL).
- Render gratis duerme: el primer request puede tardar; no es un bug.

## 4. Dependencias
- **Necesito de Ernesto:** ejecución de las pruebas CP-01 a CP-10 con evidencias.
- **Giancarlos necesita de mí:** URL del backend desplegado (CP1) y contrato actualizado.
