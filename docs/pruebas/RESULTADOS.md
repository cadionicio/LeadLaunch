# LeadLaunch — Resultados de la matriz de pruebas (QA)

**Fecha de ejecución:** 24/11/2026 03:54:40 (hora de Lima) · **Entorno:** producción, `https://leadlaunch-api.onrender.com`
**Responsable QA:** Ernesto · **Referencia:** documento maestro §7.2 y RNF-05
**Resultado Fase 1:** **7 de 7 casos ejecutables aprobados** (CP-04, CP-05 y CP-09 dependen de los endpoints de la Fase 2).

> Las solicitudes se ejecutaron en vivo contra el backend desplegado. Cada respuesta quedó registrada en
> [`ejecucion_2026-11-24.json`](ejecucion_2026-11-24.json) (tokens JWT omitidos) y resumida en las imágenes de `evidencias/`.
> Se usaron cuentas exclusivas de QA (`qa_*@correo-prueba.com`), sin tocar la cuenta demo de Ana.

## 1. Matriz (5 casos de éxito y 5 de falla)

| ID | Tipo | Caso | Entrada / escenario | Esperado | Obtenido | Resultado | Evidencia |
|---|---|---|---|---|---|---|---|
| **CP-01** | Éxito | Registro y Login JWT | Credenciales válidas en /auth/register y /auth/login | 201 / 200 + JWT | 201 (registro) · 200 (login) · JWT emitido | ✅ OK | [CP-01.png](evidencias/CP-01.png) |
| **CP-02** | Éxito | Alta de producto gastronómico | nombre='Torta Temática 15 porciones', precio_base=95.00 | 201 + producto en catálogo | 201 negocio · 201 producto TT-015 S/ 95.00 · 200 catálogo con 1 producto | ✅ OK | [CP-02.png](evidencias/CP-02.png) |
| **CP-03** | Éxito | Escaneo de radar local | POST /radar/scan con radio 3.0 km en rubro REPOSTERIA | 201 + hallazgos ≤3★ y total_escaneos +1 | 201 · 4 competidores · 21 reseñas · 13 fricciones ≤3★ · fuente DATASET_FALLBACK · dashboard total_escaneos=1 | ✅ OK | [CP-03.png](evidencias/CP-03.png) |
| **CP-04** | Éxito | Campaña adaptativa IA | POST /campaigns/generate tras escaneo | 201 + alerta, post y guion | Endpoint aún no implementado | ⏳ Pendiente (Fase 2) | — |
| **CP-05** | Éxito | Cotización + cierre ganado | Cotización express + PATCH /status a GANADA | total_neto exacto + acumuladores | Endpoint aún no implementado | ⏳ Pendiente (Fase 2) | — |
| **CP-06** | Falla | Acceso sin token JWT | GET /products sin header Authorization | 401 | 401 NO_AUTENTICADO | ✅ OK | [CP-06.png](evidencias/CP-06.png) |
| **CP-07** | Falla | Integridad de dominio | precio_base = -20.00 / whatsapp_numero = 'abc' | 422 | 422 VALIDACION campos=[precio_base] · 422 VALIDACION campos=[whatsapp_numero] | ✅ OK | [CP-07.png](evidencias/CP-07.png) |
| **CP-08** | Falla | Inyección SQL | Textos con '; DROP TABLE / DELETE / OR '1'='1 | Neutralizado; integridad intacta | 403 firewall de Render con DROP TABLE · 3 inyecciones guardadas como texto literal (201) · catálogo 200 con 4 productos · negocio activo | ✅ OK | [CP-08.png](evidencias/CP-08.png) |
| **CP-09** | Falla | Violación de política | es_entrega_express=true con permite_express=false | 400 | Endpoint aún no implementado | ⏳ Pendiente (Fase 2) | — |
| **CP-10** | Falla | Aislamiento multi-tenant | Recurso de otro emprendedor con token ajeno | 404 | 404 NEGOCIO_NO_ENCONTRADO | ✅ OK | [CP-10.png](evidencias/CP-10.png) |

## 2. Observaciones

1. **CP-08 · Dos capas de defensa.** El payload clásico `'; DROP TABLE negocios; --` fue detenido por el **firewall de Render (403 "Blocked")** antes de llegar a la API. Para probar la capa de la aplicación se enviaron tres variantes que sí llegan al backend (`DELETE FROM productos`, `OR '1'='1'` y `UPDATE negocios SET activo=false`): todas se guardaron **como texto literal** y ninguna afectó la base, porque el backend usa consultas parametrizadas. El catálogo y el dashboard siguieron respondiendo con normalidad.
2. **CP-03 · Fuente de datos demo.** El radar usa hoy `DATASET_FALLBACK` (negocios y reseñas ficticios, decisión D14) y lo indica con `es_demo: true` y un `aviso`. El filtro ≤ 3 estrellas y la clasificación por tipo de fricción se verificaron sobre esos datos.
3. **Tiempos.** Las primeras llamadas tardaron unos 2,5 s y el escaneo 7,4 s, porque el plan gratuito de Render arranca en frío. Las siguientes respondieron en menos de 1 s, y las validaciones (401/422) en unos 0,2 s.
4. **Pendientes (Fase 2):** CP-04 (campaña IA), CP-05 (cotización y cierre GANADA) y CP-09 (express no permitido → 400) se ejecutarán cuando existan esos endpoints.

## 3. Detalle de solicitudes ejecutadas

| Hora | CP | Descripción | Solicitud | Auth | Estado | Tiempo |
|---|---|---|---|---|---|---|
| 03:54:40 | CP-01 | Registro de cuenta nueva | `POST /api/v1/auth/register` | sin token | **201** | 2482 ms |
| 03:54:43 | CP-01 | Login con la cuenta creada | `POST /api/v1/auth/login` | sin token | **200** | 2502 ms |
| 03:54:45 | CP-02 | Alta de negocio (prerrequisito) | `POST /api/v1/businesses` | Bearer <token> | **201** | 2641 ms |
| 03:54:47 | CP-02 | Alta de producto gastronómico | `POST /api/v1/businesses/{negocio_id}/products` | Bearer <token> | **201** | 1577 ms |
| 03:54:48 | CP-02 | Producto listado en el catálogo | `GET /api/v1/businesses/{negocio_id}/products` | Bearer <token> | **200** | 1086 ms |
| 03:54:55 | CP-03 | Escaneo de radar (3 km, REPOSTERIA) | `POST /api/v1/businesses/{negocio_id}/radar/scan` | Bearer <token> | **201** | 7409 ms |
| 03:54:56 | CP-03 | Dashboard tras el escaneo (total_escaneos +1) | `GET /api/v1/businesses/{negocio_id}/dashboard` | Bearer <token> | **200** | 900 ms |
| 03:54:57 | CP-06 | Catálogo sin header Authorization | `GET /api/v1/businesses/{negocio_id}/products` | sin token | **401** | 230 ms |
| 03:54:57 | CP-07 | Producto con precio_base negativo | `POST /api/v1/businesses/{negocio_id}/products` | Bearer <token> | **422** | 205 ms |
| 03:54:57 | CP-07 | Negocio con whatsapp_numero = "abc" | `POST /api/v1/businesses` | Bearer <token> | **422** | 193 ms |
| 03:54:57 | CP-08 | Payload con DROP TABLE (bloqueado por el firewall de Render antes de llegar a la API) | `POST /api/v1/auth/register` | sin token | **403** | 134 ms |
| 03:54:58 | CP-08 | La tabla negocios sigue intacta (dashboard responde) | `GET /api/v1/businesses/{negocio_id}/dashboard` | Bearer <token> | **200** | 1034 ms |
| 03:55:01 | CP-10 | Registro de un segundo emprendedor | `POST /api/v1/auth/register` | sin token | **201** | 2458 ms |
| 03:55:02 | CP-10 | Acceso al catálogo de otro negocio con token ajeno | `GET /api/v1/businesses/{negocio_id}/products` | Bearer <token> | **404** | 865 ms |
| 03:55:21 | CP-08 | Inyección SQL en nombre de producto (variante 1) | `POST /api/v1/businesses/{negocio_id}/products` | Bearer <token> | **201** | 1597 ms |
| 03:55:23 | CP-08 | Inyección SQL en nombre de producto (variante 2) | `POST /api/v1/businesses/{negocio_id}/products` | Bearer <token> | **201** | 1762 ms |
| 03:55:25 | CP-08 | Inyección SQL en nombre de producto (variante 3) | `POST /api/v1/businesses/{negocio_id}/products` | Bearer <token> | **201** | 1587 ms |
| 03:55:26 | CP-08 | Catálogo después de las inyecciones: textos guardados literalmente y tablas intactas | `GET /api/v1/businesses/{negocio_id}/products?size=100` | Bearer <token> | **200** | 1077 ms |
| 03:55:27 | CP-08 | Dashboard responde (negocio sigue activo) | `GET /api/v1/businesses/{negocio_id}/dashboard` | Bearer <token> | **200** | 885 ms |

## 4. Cómo repetir las pruebas
1. En Postman: **Import** → `LeadLaunch_QA.postman_collection.json`.
2. Ejecuta la colección completa con **Run collection**, en orden. Cada solicitud tiene sus *tests* de estado HTTP y guarda tokens e IDs en variables.
3. Los correos de prueba se generan con `{{$timestamp}}`, así que se puede repetir sin choques.
