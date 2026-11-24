# LEADLAUNCH — PLAN GENERAL DEL EQUIPO
**Versión:** 1.0 · **Fecha:** 23/11/2026 · **Líder / dueño del repositorio:** César
**Equipo:** César · Giancarlos · Ernesto
**Fuentes:** `Documento_Maestro_de_Especificacion_y_Arquitectura_-_LeadLaunch.md` (base funcional) y `LEADLAUNCH — ESPECIFICACIÓN UX/UI v1.0` (base visual).

> Este documento es para todo el equipo. Cada persona tiene además su propio plan: `01_PLAN_CESAR.md`, `02_PLAN_GIANCARLOS.md`, `03_PLAN_ERNESTO.md`.

---

## 1. Qué vamos a construir (resumen en 30 segundos)

LeadLaunch es una app **Android nativa (Kotlin)** + una **API REST propia (FastAPI, Python)** + **PostgreSQL en la nube (Neon)** para emprendedores de repostería y comida local. El ciclo es:

1. **Radar:** busca competidores cercanos y filtra sus reseñas ≤ 3 estrellas (fricciones).
2. **Campaña IA:** con esas fricciones + el catálogo, Gemini genera alerta, post y guion de 15 s.
3. **Cierre:** cotización calculada en el backend (recargo express / descuento bienvenida), se envía por WhatsApp y se mide la conversión.

Todo con costo $0 (Render/Koyeb, Neon, Gemini Free Tier, OpenStreetMap).

---

## 2. Metodología

Trabajamos con **Scrum comprimido** guiado por **checkpoints de integración**:

- El backlog son las 6 historias del documento maestro (HU-01 a HU-06, 47 puntos).
- Dividimos el proyecto en **2 fases**. La **Fase 1 (50 %)** se entrega mañana martes 24/11.
- Cada fase tiene **checkpoints (CP)**: momentos fijos donde todos suben su avance, se hace merge a `develop` y se prueba que todo funcione junto.
- Cada persona trabaja en **su propia rama** y en **su propia carpeta** del repositorio, así casi nunca chocamos (conflictos de merge).
- **Daily rápido** (10 min, por WhatsApp o llamada) al inicio de cada bloque de trabajo: qué hice, qué haré, qué me bloquea.
- **Definición de "Terminado" (DoD)** para cualquier tarea:
  1. El código compila/corre sin errores.
  2. Cumple el criterio de aceptación de la HU o la ficha de pantalla.
  3. Está subido (commit + push) a la rama personal con mensaje claro.
  4. Se probó al menos el caso feliz y un caso de error.

### Por qué la Fase 1 equivale al 50 %
| HU | Contenido | Puntos | Fase |
|---|---|:-:|:-:|
| HU-01 | Cuenta, negocio y catálogo | 8 | 1 |
| HU-02 | Políticas y reglas de precio | 5 | 1 |
| HU-03 | Radar local y fricciones | 13 | 1 |
| HU-04 | Campaña con IA | 8 | 2 |
| HU-05 | Cotización + WhatsApp | 8 | 2 |
| HU-06 | Estados + dashboard de conversión | 5 | 2 |
| | **Fase 1 = 26 / 47 puntos ≈ 55 %** | | |

---

## 3. Responsables por área

| Persona | Área principal | Rama personal | Carpeta que le pertenece |
|---|---|---|---|
| **César** | Líder, GitHub, Backend API (FastAPI): auth, negocio, catálogo, reglas, radar, IA, cotizaciones, despliegue del backend, integración y merges | `cesar/backend` | `/backend` |
| **Giancarlos** | App Android (Kotlin): diseño (tokens y componentes), las 17 pantallas, Retrofit + JWT, mapa, WhatsApp Intent | `giancarlos/android` | `/android` |
| **Ernesto** | Base de datos en Neon (ejecutar DDL y semillas), dataset de respaldo del radar, colección de pruebas y ejecución de la matriz CP-01 a CP-10 | `ernesto/db-qa` | `/database` y `/docs/pruebas` |

Reglas de convivencia:
- **No editar la carpeta de otro.** Si necesitas algo ahí, pídelo en el grupo.
- El archivo `docs/api/CONTRATOS_API.md` lo mantiene **César**. Si cambia un endpoint, avisa en el grupo antes de hacer push.
- Las claves (Gemini, base de datos, JWT) **nunca** se suben. Van en `.env` (ignorado por Git); en el repo solo existe `.env.example`.

---

## 4. Estructura del repositorio (monorepo)

```text
leadlaunch/
├── README.md
├── .gitignore
├── backend/                 ← César (FastAPI)
│   ├── app/
│   │   ├── main.py
│   │   ├── core/            (config, seguridad JWT, errores)
│   │   ├── routers/         (Capa 1: auth, businesses, products, rules, radar, campaigns, quotes)
│   │   ├── services/        (Capa 2: AuthService, BusinessService, CatalogService, PolicyService,
│   │   │                     RadarService, AdaptiveAIService, QuoteService)
│   │   ├── repositories/    (Capa 3: acceso a datos, SQL parametrizado)
│   │   └── schemas/         (DTOs Pydantic)
│   ├── tests/
│   ├── requirements.txt
│   └── .env.example
├── android/                 ← Giancarlos (proyecto Android Studio)
├── database/                ← Ernesto
│   ├── 01_ddl.sql
│   ├── 02_seed.sql
│   ├── 03_seed_demo.sql     (datos de la maqueta: Dulce Punto, productos, etc.)
│   └── fallback/radar_dataset.json
└── docs/
    ├── planificacion/       (estos 4 archivos)
    ├── especificaciones/    (los 2 Markdown fuente)
    ├── api/CONTRATOS_API.md ← César
    └── pruebas/             ← Ernesto (colección Postman/Bruno + evidencias)
```

---

## 5. Estrategia de ramas

```text
main                 ← solo versiones estables (v0.5.0 = Fase 1, v1.0.0 = final). Protegida.
 └── develop         ← integración. Aquí se juntan los avances en cada checkpoint.
      ├── cesar/backend
      ├── giancarlos/android
      └── ernesto/db-qa
```

- **Nadie hace push directo a `main` ni a `develop`.** Todo entra por **Pull Request (PR)**.
- `cesar/backend`, `giancarlos/android`, `ernesto/db-qa` → PR hacia `develop` en cada checkpoint.
- `develop` → PR hacia `main` solo al cerrar una fase (la aprueba César).
- Antes de abrir un PR, cada uno **trae lo último de `develop`** a su rama (ver comandos abajo).

### 5.1 Comandos que ejecuta SOLO César (una vez)

```bash
# 1) En GitHub: crear repositorio "leadlaunch" (privado) VACÍO (sin README, sin .gitignore, sin licencia).
# 2) En tu PC, dentro de la carpeta local del proyecto (ya tiene la estructura base):
git init
git branch -M main
git add .
git commit -m "chore: estructura inicial del proyecto y planificación"
git remote add origin https://github.com/<usuario>/leadlaunch.git
git push -u origin main

# 3) Crear develop a partir de main y subirla
git checkout -b develop
git push -u origin develop

# 4) Crear las 3 ramas personales a partir de develop y subirlas
git checkout -b cesar/backend develop
git push -u origin cesar/backend
git checkout -b giancarlos/android develop
git push -u origin giancarlos/android
git checkout -b ernesto/db-qa develop
git push -u origin ernesto/db-qa

# 5) Volver a tu rama de trabajo
git checkout cesar/backend
```

Luego, en GitHub:
1. **Settings → Collaborators** → invitar a Giancarlos y Ernesto (rol *Write*).
2. **Settings → General → Default branch** → cambiar a `develop`.
3. **Settings → Branches → Add rule** para `main` y para `develop`: marcar *Require a pull request before merging*.

### 5.2 Comandos para Giancarlos y Ernesto (una vez)

```bash
git clone https://github.com/<usuario>/leadlaunch.git
cd leadlaunch
git checkout giancarlos/android      # Ernesto: git checkout ernesto/db-qa
git config user.name "Tu Nombre"
git config user.email "tu-correo@ejemplo.com"
```

### 5.3 Rutina diaria (todos)

```bash
git checkout <mi-rama>
git pull                              # traer lo último de MI rama
# ... trabajar ...
git add .
git commit -m "feat(HU-01): pantalla de login conectada a la API"
git push
```

### 5.4 Antes de cada checkpoint (todos)

```bash
git checkout <mi-rama>
git fetch origin
git merge origin/develop              # traer lo que otros ya integraron
# si hay conflictos: resolver, luego
git add . && git commit -m "merge: develop en <mi-rama>"
git push
```
Después: en GitHub → **Pull requests → New** → base `develop` ← compare `<mi-rama>` → crear PR. César revisa y hace *Merge*.

### 5.5 Convención de commits
`tipo(HU-XX): descripción corta en español`
- `feat` = funcionalidad nueva · `fix` = corrección · `docs` = documentación · `test` = pruebas · `chore` = configuración.
- Ejemplos: `feat(HU-03): fallback de radar con dataset JSON` · `fix(HU-01): validar SKU duplicado con 422`.

---

## 6. Cronograma y checkpoints

### FASE 1 — 50 % (lunes 23/11 noche → martes 24/11 noche)

| Checkpoint | Hora límite | Qué debe existir al hacer merge |
|---|---|---|
| **CP0 · Arranque** | Lun 23/11 · 23:30 | Repo y ramas creadas. Estructura de carpetas. DDL + semillas ejecutadas en Neon. Backend "hola mundo" con `/health` y Swagger visible. Proyecto Android creado con tema (colores/tipografía) y navegación vacía. |
| **CP1 · Cuenta y negocio** | Mar 24/11 · 13:00 | Register, login y alta de negocio funcionando y **desplegados** en Render/Koyeb. App: S01, S02, S03, S04 conectadas a la API real. Pruebas CP-01, CP-06 y CP-07 ejecutadas. |
| **CP2 · Catálogo, reglas, dashboard y radar** | Mar 24/11 · 21:00 | Backend: productos (POST/GET), reglas (GET/PUT), dashboard, radar con fallback. App: S05, S06, S07, S08 conectadas; S09 y S10 al menos con UI. Pruebas CP-02, CP-03 ejecutadas. **Merge `develop` → `main` + tag `v0.5.0`.** |

**Demo de cierre de Fase 1 (lo que debemos poder mostrar):**
Abrir app → crear cuenta → registrar negocio → ver Inicio → crear un producto → verlo en catálogo → cambiar recargo express a 15 % → escanear radar → ver fricciones.

### FASE 2 — 50 % restante (fechas por definir)
- **CP3 · Campaña IA:** HU-04 (backend + S11, S12).
- **CP4 · Cotización y WhatsApp:** HU-05 (backend + S13, paso de datos del prospecto, S14).
- **CP5 · Seguimiento y analítica:** HU-06 (PATCH estado, historial S15, S16, S17, logout).
- **CP6 · Cierre:** matriz de 10 pruebas completa, Swagger final, pantallas de estados (vacío/error/carga), `v1.0.0`.

---

## 7. Contrato de API para la Fase 1 (para trabajar en paralelo)

Giancarlos puede programar la app contra estos formatos aunque el backend aún no esté desplegado. Base: `https://<backend>/api/v1`. Todo endpoint (salvo auth) exige `Authorization: Bearer <token>`.

**Formato de error único:**
```json
{ "error": { "codigo": "SKU_DUPLICADO", "mensaje": "Ya existe un producto con ese SKU.", "campos": ["sku"] } }
```
Códigos: `400` regla de negocio · `401` sin sesión · `404` no existe o es de otro negocio · `422` campo inválido · `500` error interno.

| # | Endpoint | Request | Response |
|---|---|---|---|
| 1 | `POST /auth/register` | `{nombre_completo, correo, password}` | `201 {access_token, token_type:"bearer", usuario:{usuario_id, nombre_completo, correo}, negocio_id:null}` |
| 2 | `POST /auth/login` | `{correo, password}` | `200` mismo formato; `negocio_id` es `null` si aún no registró negocio |
| 3 | `POST /businesses` | `{rubro_id, razon_comercial, descripcion, whatsapp_numero, distrito, latitud, longitud, radio_cobertura_km}` | `201 {negocio_id, ...}` |
| 4 | `GET /businesses/{id}/dashboard` | — | `200 {razon_comercial, rubro_nombre, distrito, total_escaneos, total_cotizaciones, total_ventas_ganadas, monto_ventas_ganadas, tasa_conversion_pct, productos_activos, fricciones_detectadas, ultima_alerta_adaptativa}` |
| 5 | `POST /businesses/{id}/products` | `{sku, nombre, descripcion, precio_base, tiempo_prep_horas, stock_disponible, permite_express}` | `201 {producto_id, ...}` |
| 6 | `GET /businesses/{id}/products?page=1&size=20` | — | `200 {items:[...], page, size, total}` |
| 7 | `PUT /businesses/{id}/rules` | `{politica:{permite_express, permite_acumular_desc, acepta_yape_plin, horario_atencion}, reglas:[{tipo_regla, valor_porcentaje, monto_minimo, cantidad_minima, activa}]}` | `200` mismo formato |
| 7b | `GET /businesses/{id}/rules` *(extra, ver §8)* | — | `200` mismo formato que el PUT |
| 8 | `POST /businesses/{id}/radar/scan` | `{radio_km}` | `201 {escaneo_id, fuente_datos, radio_busqueda_km, total_analizados, total_fricciones, ejecutado_en, competidores:[{competidor_id, nombre, distrito, latitud, longitud, calificacion_prom, fricciones:[{tipo_codigo, tipo_nombre, calificacion, resumen_dolor}]}], resumen_fricciones:[{tipo_codigo, tipo_nombre, cantidad}]}` |

Rubros (semilla, IDs fijos para la app): `1` Repostería y Pastelería Artesanal · `2` Comida Rápida y Bocaditos · `3` Cafeterías y Postres.

---

## 8. Decisiones tomadas para avanzar (se pueden revisar)

Estas decisiones resuelven pendientes de la especificación UX (P01–P16) solo lo necesario para la Fase 1:

| # | Tema | Decisión para el MVP |
|---|---|---|
| D1 | Stack backend | **FastAPI (Python) + Pydantic + psycopg/SQLAlchemy Core**. Swagger sale gratis en `/docs`. |
| D2 | ¿Cómo sabe la app si ya hay negocio? | El login/registro devuelve `negocio_id` (o `null`). S01/S02 deciden S04 o S05 con eso. |
| D3 | Cargar S08 (reglas) | Se agrega `GET /businesses/{id}/rules` (el PUT solo no permite mostrar los valores actuales). |
| D4 | Tiempo de preparación (P02) | La UI muestra y pide **horas** (≥ 1). No se cambia la BD. |
| D5 | "Radio de delivery" (P03) | Se renombra en la UI a **"Radio del radar"**. Un solo radio. |
| D6 | Google login y recuperar contraseña (P01) | Se muestran **deshabilitados** con texto "Próximamente". |
| D7 | Fotos de producto (P04) | **Placeholder** con la inicial del producto. Sin almacenamiento de imágenes. |
| D8 | Tarifas por medio de pago (P06) | Se **quitan** de S08. Solo queda el check "Acepta Yape/Plin". |
| D9 | Pestaña "Cotizar" (P12) | Abre **S15 (historial)** con botón "Nueva cotización" → S13. |
| D10 | Mapa en Android | **osmdroid (OpenStreetMap)**, gratis y sin API key. |
| D11 | Refresco del dashboard | El backend ejecuta `REFRESH MATERIALIZED VIEW CONCURRENTLY` después de crear producto, escanear y cambiar estado de cotización. |
| D12 | Datos del radar de respaldo | Si se usa el dataset, la respuesta y la UI dicen "Datos de referencia (demo)". Nunca se presentan como reseñas reales. |
| D13 | Modelo de IA | El documento dice Gemini 1.5 Flash; esa versión puede estar retirada. Usar el modelo **Flash vigente** del Free Tier de Google AI Studio (se confirma en Fase 2). |

Pendientes para Fase 2: P07 (datos del prospecto), P08 (confirmar ENVIADA_WA al volver de WhatsApp), P09 (guion 15 s), P10 (GET de historiales), P11 (acumulación), P13 (multi-negocio), P15/P16.

---

## 9. Riesgos y cómo los manejamos

| Riesgo | Mitigación |
|---|---|
| Render/Koyeb gratis "duerme" y la primera llamada tarda > 10 s | Mostrar loader en la app; abrir el backend 1 min antes de la demo. |
| La API externa de mapas/reseñas falla o no trae reseñas | Fallback con `database/fallback/radar_dataset.json` desde el día 1. |
| Conflictos de Git | Cada uno en su carpeta; merge de `develop` a la rama antes de cada PR. |
| El backend no está listo cuando la app lo necesita | La app usa datos simulados con el formato del §7 y luego cambia la URL. |
| Fuga de claves | `.env` en `.gitignore`; revisar el PR antes del merge. |

---

## 10. Canal y reglas del equipo
- Grupo de WhatsApp del equipo: avisar al hacer push de algo que otros necesitan.
- Bloqueado más de 30 minutos → avisar en el grupo.
- Cada checkpoint: 15 min de llamada para hacer merge y probar la demo juntos.
