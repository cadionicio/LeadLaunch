# LEADLAUNCH — PLAN DE LA FASE 2 (50 % restante)
**Versión:** 1.0 · **Fecha:** 24/11/2026 · **Responsable:** César
**Punto de partida:** release `v0.5.0` (Fase 1 cerrada) · **Meta:** release `v1.0.0`
**Leer junto con:** `00_PLAN_GENERAL.md`, `docs/api/CONTRATOS_API.md` y el documento maestro (§3–§7).

---

## 0. Estado de cierre de la Fase 1 (verificado el 24/11 a las 04:10)

| Verificación | Resultado |
|---|---|
| `main` = `develop`, con el tag `v0.5.0` | ✅ Mismo contenido; release publicado |
| Backend en `main`: 33 pruebas automáticas contra PostgreSQL | ✅ 33/33 |
| `database/01_ddl.sql` + `02_seed.sql` frente al documento maestro | ✅ Coincidencia 100 % |
| Producción (Render + Neon): `/health/db`, login de Ana y dashboard | ✅ 200 · 4 productos · 1 escaneo · 13 fricciones |
| App Android S01–S10 contra la API real; S11–S17 con aviso de datos simulados | ✅ En `main` |
| QA: matriz CP-01, 02, 03, 06, 07, 08 y 10 en producción | ✅ 7/7 OK (`docs/pruebas/RESULTADOS.md`) |
| Archivos indebidos en `main` (`.env`, `repomix`, `.kotlin/`, `local.properties`) | ✅ Ninguno |
| Ramas `giancarlos/android` y `ernesto/db-qa` | ⚠️ Atrasadas respecto a `develop` (normal). Hacer `git merge origin/develop` antes de trabajar |

**Puntos de historia:** Fase 1 = HU-01 + HU-02 + HU-03 = 26/47. **Fase 2 = HU-04 + HU-05 + HU-06 = 21/47.**

---

## 1. Alcance de la Fase 2

| HU | Historia | Pts | Pantallas | Endpoints |
|---|---|:-:|---|---|
| **HU-04** | Campaña adaptativa con IA (alerta + post + guion 15 s) | 8 | S11, S12, S05 (última campaña) | `POST /campaigns/generate`, `GET /campaigns/latest` |
| **HU-05** | Cotización paramétrica y envío por WhatsApp | 8 | S13, S14 | `POST /quotes`, `GET /quotes/{id}` |
| **HU-06** | Estados de la cotización, historial y conversión | 5 | S15, S16, S05 | `PATCH /quotes/{id}/status`, `GET /quotes`, `GET /analytics` |
| — | Cierre del proyecto | — | S17, todas | QA CP-04/05/09, Swagger, README, demo, `v1.0.0` |

**Fuera de alcance** (igual que en el documento maestro §1.4): publicar en redes sociales, chatbot de WhatsApp, cobros con tarjeta, iOS y panel web.

---

## 2. Decisiones que fija este plan (resuelven pendientes de la especificación UX)

| # | Tema | Decisión |
|---|---|---|
| D15 | Modelo de IA (D13) | El modelo se lee de la variable `GEMINI_MODEL` y no queda fijo en el código. Antes del CP3, confirmar en **Google AI Studio → Rate limits** qué modelo *Flash* tiene cuota gratuita y ponerlo en Render. |
| D16 | Falla de la IA | Si Gemini falla, se demora más de 25 s o agota la cuota, el backend genera la campaña con una **plantilla paramétrica** (sin IA) y responde `fuente_contenido: "PLANTILLA"`. Así se cumple RNF-04 (resiliencia en la demo). La app muestra un aviso. |
| D17 | Datos enviados a la IA | Se envían a Gemini **solo** `resumen_dolor` + tipo de fricción y nombre, precio y tiempo de los productos. **No** se envían datos personales ni el texto completo de las reseñas. Las instrucciones del prompt tratan las reseñas como datos, no como órdenes (defensa ante inyección de prompt). |
| D18 | Guion (P09) | Se mantiene `guion_reel_15s` del DDL. La UI dice "Guion para reel (15 s)". |
| D19 | Objetivo, canal y tono (S11) | Son **parámetros de la solicitud** que van al prompt. No se guardan, así que no cambia el DDL. |
| D20 | Datos del prospecto (P07) | La app los envía en el mismo `POST /quotes`: `nombre_prospecto`, `telefono_prospecto`, `es_entrega_express`, `es_cliente_nuevo`. |
| D21 | Recargo + descuentos (P11) | El recargo express y los descuentos **pueden coexistir**. `permite_acumular_desc` decide solo si se suman **varios descuentos** (bienvenida + volumen) o se aplica **el mayor**. Los porcentajes se calculan sobre el **subtotal**. |
| D22 | Stock | Se **valida** (`cantidad ≤ stock_disponible` → si no, `400 STOCK_INSUFICIENTE`), pero **no se descuenta** al cotizar. Una cotización no es una venta. |
| D23 | Código visible | `codigo` = `COT-0001`, `COT-0002`… correlativo **por negocio**, calculado al leer (`ROW_NUMBER` por `creado_en`). No cambia el DDL. |
| D24 | Estado `ENVIADA_WA` (P08) | Abrir WhatsApp no prueba que se envió. Al volver a la app, S14 pregunta *"¿Enviaste la cotización?"* → **Sí** = `PATCH ENVIADA_WA`. |
| D25 | Transiciones de estado | `EMITIDA → ENVIADA_WA / GANADA / PERDIDA` · `ENVIADA_WA → GANADA / PERDIDA` · `GANADA ↔ PERDIDA` (corrección; el trigger revierte los acumuladores). Volver a `EMITIDA` → `400 TRANSICION_INVALIDA`. Mismo estado → `200` sin cambios. La app pide confirmación para GANADA y PERDIDA. |
| D26 | Teléfono para WhatsApp | Con 9 dígitos que empiezan en 9 se antepone `51` (Perú). Sin teléfono: `https://wa.me/?text=...` y WhatsApp deja elegir el contacto. |
| D27 | Analítica S16 | Endpoint nuevo `GET /analytics?dias=7|30|90`. **Ticket promedio = ventas ganadas ÷ número de ganadas** (0 si no hay). Tasa del periodo = ganadas ÷ cotizaciones creadas en el periodo. |
| D28 | Radar con fuente real (D14) | **Sigue fuera** del MVP: el radar continúa con el dataset demo y su aviso. Se documenta como trabajo futuro. |

---

## 3. Contrato de la API — Fase 2
Base `https://leadlaunch-api.onrender.com/api/v1`. Todo con `Authorization: Bearer`. Mismo formato de error `{error:{codigo, mensaje, campos}}`. Recurso de otro negocio → `404`.
Los campos coinciden con los DTOs que la app **ya tiene** (`Dtos.kt`, sección FASE 2). Lo marcado como **(nuevo)** son campos que la app debe agregar.

### 3.1 `POST /businesses/{id}/campaigns/generate` → `201` (HU-04, lógica compleja 2)
```json
// Request
{ "escaneo_id": null, "fricciones": ["DEMORA_ENTREGA", "PRECIO_DELIVERY"],
  "objetivo": "Atraer más pedidos", "canal": "Redes sociales (Instagram, Facebook, TikTok)", "tono": "Cercano y confiable" }
// Response
{ "campana_id": "uuid", "alerta_adaptativa": "...", "post_redes": "...", "guion_reel_15s": "...",
  "estado": "BORRADOR", "creado_en": "...", "fuente_contenido": "GEMINI" /* o "PLANTILLA" (nuevo) */,
  "fricciones_usadas": 6 /* (nuevo) */ }
```
- `fricciones` es opcional: vacío = todas las del negocio. `escaneo_id` es opcional: null = las más recientes.
- Usa como máximo las 15 fricciones más recientes y el catálogo activo, en **una sola llamada** a Gemini que devuelve JSON con las 3 secciones.
- El negocio no tiene fricciones → `400 SIN_FRICCIONES` ("Primero ejecuta el radar").
- Guarda en `campanas_adaptativas` y refresca la vista (el dashboard muestra `ultima_alerta_adaptativa`).

### 3.2 `GET /businesses/{id}/campaigns/latest` → `200` | `404 SIN_CAMPANAS`
Mismo formato que 3.1. Lo usan S12 (reabrir la campaña) y S05 (tarjeta "Última campaña").

### 3.3 `POST /businesses/{id}/quotes` → `201` (HU-05, lógica compleja 3)
```json
// Request
{ "nombre_prospecto": "María Torres", "telefono_prospecto": "987654321",
  "es_entrega_express": true, "es_cliente_nuevo": true,
  "items": [ { "producto_id": "uuid-torta", "cantidad": 1 }, { "producto_id": "uuid-cheesecake", "cantidad": 2 } ] }
// Response
{ "cotizacion_id": "uuid", "codigo": "COT-0001", "nombre_prospecto": "María Torres", "telefono_prospecto": "987654321",
  "es_entrega_express": true, "es_cliente_nuevo": true,
  "subtotal": 205.00, "monto_recargo": 30.75, "monto_descuento": 20.50, "total_neto": 215.25,
  "mensaje_whatsapp": "...", "whatsapp_url": "https://wa.me/51987654321?text=...",
  "estado": "EMITIDA", "creado_en": "...",
  "detalles": [ { "producto_id": "...", "nombre_producto": "Torta de chocolate", "cantidad": 1, "precio_unitario_snap": 75.00, "importe_linea": 75.00 } ],
  "reglas_aplicadas": [ { "tipo_regla": "RECARGO_EXPRESS", "valor_porcentaje": 15.0, "monto": 30.75 },
                        { "tipo_regla": "DESC_BIENVENIDA", "valor_porcentaje": 10.0, "monto": 20.50 } ] /* (nuevo) */ }
```
**Algoritmo de liquidación** (en el backend, **sin IA**; todo con `Decimal` y redondeo *half-up* a 2 decimales):
1. Validar: de 1 a 30 ítems, `cantidad` ≥ 1, cada producto **activo y del mismo negocio** (si no → `422 PRODUCTO_INVALIDO`), `cantidad ≤ stock_disponible` (si no → `400 STOCK_INSUFICIENTE`) y ningún producto repetido.
2. `subtotal = Σ cantidad × precio_base` (precio vigente, que queda congelado en el snapshot).
3. **Express:** si `es_entrega_express` y (`politica.permite_express = false` **o** algún producto tiene `permite_express = false`) → `400 EXPRESS_NO_PERMITIDO` (**CP-09**). Si se permite: `recargo = subtotal × RECARGO_EXPRESS%` (0 si la regla está inactiva).
4. **Descuentos candidatos:** `DESC_BIENVENIDA` si `es_cliente_nuevo`; `DESC_VOLUMEN` si Σ cantidades ≥ `cantidad_minima` **y** subtotal ≥ `monto_minimo`. Solo cuentan reglas activas y vigentes (`vigente_desde ≤ hoy ≤ vigente_hasta`).
5. Si `permite_acumular_desc` se suman; si no, se aplica **el mayor**. El descuento nunca supera el subtotal.
6. `total_neto = subtotal + recargo − descuento` (lo exige el `CHECK` del DDL).
7. Guardar en **una transacción**: `cotizaciones_cierre` + `cotizacion_detalles` + `snapshot_reglas_json` (productos con precio, política, reglas aplicadas y fecha). El trigger suma `total_cotizaciones` y escribe en la bitácora. Luego, refrescar la vista.
8. Armar `mensaje_whatsapp` con una plantilla y `whatsapp_url` (D26).

**Ejemplo de verificación (maqueta):** 1 × 75 + 2 × 65 = **205.00** · express 15 % = **30.75** · bienvenida 10 % = **20.50** · total **215.25** ✔

Plantilla del mensaje:
```
¡Hola María Torres! 👋 Te comparto tu cotización COT-0001 de Dulce Punto:
• 1 × Torta de chocolate — S/ 75.00
• 2 × Cheesecake de frutos — S/ 130.00
Subtotal: S/ 205.00
Recargo express (15 %): + S/ 30.75
Descuento bienvenida (10 %): − S/ 20.50
Total: S/ 215.25
Pagos: Yape / Plin ✅      ← solo si acepta_yape_plin
Atendemos 09:00 - 20:00. ¿Confirmamos tu pedido?
```

### 3.4 `GET /businesses/{id}/quotes?estado=&q=&page=1&size=20` → `200` (S15)
```json
{ "items": [ /* QuoteResponse sin detalles */ ], "total": 28, "page": 1, "size": 20,
  "conteo_por_estado": { "EMITIDA": 6, "ENVIADA_WA": 8, "GANADA": 9, "PERDIDA": 5 } /* (nuevo, para los chips) */ }
```
`q` busca por código o nombre del prospecto. Orden: más recientes primero.

### 3.5 `GET /quotes/{quote_id}` → `200` (S14 al reabrir desde S15)
`QuoteResponse` completo, con `detalles` y `reglas_aplicadas`.

### 3.6 `PATCH /quotes/{quote_id}/status` → `200` (HU-06)
`{ "estado": "ENVIADA_WA" | "GANADA" | "PERDIDA" }` → `QuoteResponse`. Aplica las transiciones de D25. El trigger actualiza `total_ventas_ganadas` y `monto_ventas_ganadas` y escribe en la bitácora. Después se refresca la vista, así que el dashboard queda al día en menos de 500 ms.

### 3.7 `GET /businesses/{id}/analytics?dias=30` → `200` (S16)
```json
{ "dias": 30, "desde": "2026-10-26", "hasta": "2026-11-24",
  "cotizaciones": 28, "ganadas": 9, "perdidas": 5, "tasa_conversion_pct": 32.14,
  "ventas_ganadas": 2340.00, "ticket_promedio": 260.00,
  "serie": [ { "fecha": "2026-11-20", "cotizaciones": 3, "ganadas": 1 } ] }
```
`serie` trae un punto por día del periodo, incluidos los días en 0, para que la gráfica no invente curvas.

---

## 4. Checkpoints de la Fase 2

| CP | Entrega | Backend (César) | App (Giancarlos) | QA (Ernesto) | Esfuerzo est. |
|---|---|---|---|---|---|
| **CP3** | Campañas IA | 3.1, 3.2, cliente Gemini + plantilla de respaldo, variables `GEMINI_API_KEY` y `GEMINI_MODEL` en Render | S11/S12 a la API real, S05 "Última campaña", aviso si `fuente_contenido = PLANTILLA` | CP-04 | 4–5 h |
| **CP4** | Cotizaciones | 3.3, 3.5, motor de liquidación con pruebas unitarias de cada regla | S13/S14 a la API real, diálogo "¿Enviaste la cotización?" (D24) | CP-05 (emisión), CP-09 | 5–6 h |
| **CP5** | Seguimiento | 3.4, 3.6, 3.7 | S15 (chips con conteo, cambio de estado con confirmación), S16 a la API real | CP-05 (GANADA + acumuladores), regresión CP-01…10 | 4 h |
| **CP6** | Cierre `v1.0.0` | Swagger revisado, `CONTRATOS_API.md` final, README, ping anti-sueño en Render para la demo | `USE_MOCK_PHASE2 = false` (el aviso desaparece solo), S17 cerrar sesión, estados vacío/error/carga | `RESULTADOS.md` final 10/10 + evidencias | 3 h |

Cada CP termina como en la Fase 1: PR de cada rama → `develop`, prueba de la demo en conjunto y, al final del CP6, `develop` → `main` + release **`v1.0.0`**. Las fechas las define el equipo.

---

## 5. Tareas detalladas

### 5.1 César — Backend (rama `cesar/backend`)
**CP3 · Campañas**
- [ ] `app/core/config.py`: `gemini_model`, `gemini_timeout_s = 25`.
- [ ] `services/ia_cliente.py`: llamada HTTPS a la API de Gemini con `httpx`, petición de respuesta JSON con 3 claves, validación del JSON y reintento 1 vez si viene mal formado.
- [ ] `services/campana_plantilla.py`: respaldo sin IA (texto armado según el tipo de fricción dominante y el producto más rápido o barato).
- [ ] `repositories/campanas_repo.py` + `services/campaign_service.py` + `routers/campaigns.py`.
- [ ] Pruebas: sin fricciones → 400; IA simulada OK → 201 `GEMINI`; IA simulada con error → 201 `PLANTILLA`; negocio ajeno → 404. Las pruebas **nunca** llaman a Gemini de verdad (se simula el cliente).
- [ ] Render: agregar `GEMINI_API_KEY` y `GEMINI_MODEL`. La clave **nunca** va al repo.

**CP4 · Cotizaciones**
- [ ] `services/liquidacion.py`: función **pura** `liquidar(productos, politica, reglas, solicitud) → resultado`, sin BD, para probarla aislada.
- [ ] Pruebas unitarias de la liquidación: ejemplo de la maqueta (215.25), express prohibido por política y por producto, bienvenida sola, volumen solo, ambos con y sin `permite_acumular_desc`, regla inactiva o vencida, redondeos (ej. 33.333…), descuento mayor que el subtotal.
- [ ] `repositories/cotizaciones_repo.py` (inserción con detalles en la misma transacción, `codigo` con `ROW_NUMBER`), `services/quote_service.py`, `routers/quotes.py`.
- [ ] `services/whatsapp.py`: plantilla del mensaje + `wa.me` con `urllib.parse.quote`.

**CP5 · Seguimiento y analítica**
- [ ] `PATCH /status` con la tabla de transiciones (D25); verificar el trigger: GANADA suma, GANADA → PERDIDA resta.
- [ ] `GET /quotes` con filtros, búsqueda, paginación y `conteo_por_estado`.
- [ ] `GET /analytics` con `generate_series` para la serie diaria.

**CP6 · Cierre**
- [ ] Actualizar `CONTRATOS_API.md` (sección Fase 2) y `scripts/cargar_demo.py` (3–4 cotizaciones de ejemplo en distintos estados para la demo).
- [ ] Ping cada 10 min a `/health` (UptimeRobot o cron-job.org, gratis) el día de la exposición.

### 5.2 Giancarlos — App (rama `giancarlos/android`)
- [ ] Agregar a los DTOs los campos **(nuevo)** del §3: `fuente_contenido`, `fricciones_usadas`, `reglas_aplicadas`, `conteo_por_estado`, `page`/`size`, y el DTO de analítica.
- [ ] `ApiService`: `getLatestCampaign`, `getQuote(id)`, `getAnalytics(dias)`, y `getQuotes` con `estado`, `q`, `page` y `size`.
- [ ] S14: abrir `whatsapp_url` con `Intent.ACTION_VIEW`; si WhatsApp no está instalado, ofrecer **copiar el mensaje**; al volver, mostrar el diálogo D24.
- [ ] S15/S16: confirmación antes de GANADA o PERDIDA y refrescar el dashboard después.
- [ ] Mostrar los montos que vienen del backend. **No** recalcular en la app (el cálculo local queda solo en el `MockBackend`).
- [ ] Al terminar CP3–CP5: `USE_MOCK_PHASE2 = false`.

### 5.3 Ernesto — QA (rama `ernesto/db-qa`)
- [ ] **CP-04** campaña: 201 con las 3 secciones no vacías; dashboard con `ultima_alerta_adaptativa`.
- [ ] **CP-05** cotización express de la maqueta = **S/ 215.25** exacto → `PATCH GANADA` → dashboard con `total_ventas_ganadas` +1 y `monto` +215.25.
- [ ] **CP-09** express con `permite_express = false` → 400 `EXPRESS_NO_PERMITIDO`.
- [ ] Regresión completa CP-01…CP-10 sobre `v1.0.0`; actualizar `RESULTADOS.md`, la colección de Postman y las evidencias.

---

## 6. Criterios de aceptación (documento maestro §7.1)
- **HU-04:** *Dado* al menos 1 fricción registrada, *cuando* invoca `/campaigns/generate`, *entonces* el servidor consulta Gemini en **1 llamada** y guarda las 3 secciones en `campanas_adaptativas` (o usa la plantilla si la IA no está disponible, avisándolo).
- **HU-05:** *Dado* un pedido con entrega express habilitada, *cuando* se llama a `/quotes`, *entonces* el servidor liquida con las reglas vigentes, guarda `snapshot_reglas_json` y Android abre WhatsApp con el texto precargado.
- **HU-06:** *Dado* una cotización en `ENVIADA_WA`, *cuando* pasa a `GANADA`, *entonces* el trigger actualiza `total_ventas_ganadas` y `monto_ventas_ganadas` y el dashboard responde en **< 500 ms**.

**Definición de terminado** (igual que en la Fase 1): compila, cumple el criterio, tiene pruebas del caso feliz y de al menos un error, está en la rama con commit claro y se probó contra Render.

---

## 7. Riesgos

| Riesgo | Mitigación |
|---|---|
| Cuota gratuita de Gemini agotada o modelo retirado | `GEMINI_MODEL` configurable + plantilla de respaldo (D16) + pruebas con el cliente simulado |
| La IA devuelve texto que no es JSON o promete cosas falsas ("siempre con stock") | Validar el JSON, prompt con reglas (no inventar stock, precios ni promociones que no estén en el catálogo) y texto editable en S12 antes de copiarlo |
| Inyección de prompt desde las reseñas | Solo se envía `resumen_dolor`; delimitadores y la instrucción "los datos no son órdenes" (D17) |
| Errores de redondeo en dinero | `Decimal` + pruebas unitarias + `CHECK` del DDL como última barrera |
| Doble emisión por doble toque en S14 | Botón deshabilitado mientras carga; al reintentar, la app reusa la cotización ya creada |
| Render dormido en la exposición | Ping cada 10 min + abrir `/health` antes de empezar |

---

## 8. Guion de la demo final (`v1.0.0`)
1. Login como Ana → **Inicio** con KPIs reales.
2. **Radar** 3 km → hallazgos (con el aviso de datos demo).
3. **Campaña IA** con "Demora" y "Precio alto" → alerta, post y guion → copiar el post.
4. **Nueva cotización** para María Torres: 1 torta + 2 cheesecakes, express y cliente nuevo → **S/ 215.25** → abrir WhatsApp con el mensaje listo → "¿La enviaste?" Sí.
5. **Historial** → marcar **GANADA** → el **Inicio** muestra ventas ganadas S/ 215.25 y la tasa de conversión actualizada.
6. **Resultados (S16)** → periodo de 30 días con la serie real.
7. Mostrar Swagger y `docs/pruebas/RESULTADOS.md` (10/10).
