# PLAN INDIVIDUAL — ERNESTO
**Rol:** Pruebas de la API (QA)
**Rama:** `ernesto/db-qa` · **Carpeta:** `/docs/pruebas`
**Leer antes:** `00_PLAN_GENERAL.md` (§5 ramas), `docs/api/CONTRATOS_API.md` y el documento maestro §7.2 (matriz de pruebas).

> **Estado 24/11 03:55:** matriz ejecutada contra producción: 7/7 casos de Fase 1 OK. Ver `docs/pruebas/RESULTADOS.md`.
>
> Actualización 24/11: la base de datos en Neon, los scripts de `database/` y el dataset del radar ya están hechos (los asumió César). **No ejecutes scripts de creación en Neon ni modifiques `database/`.**

---

## 1. Responsabilidades
1. Armar la colección de pruebas de la API (Postman o Bruno).
2. Ejecutar la matriz CP-01 a CP-10 contra el backend desplegado y guardar evidencias.
3. Reportar a César cualquier resultado distinto al esperado.

## 2. Datos para probar
- **Base de la API:** `https://leadlaunch-api.onrender.com/api/v1` · **Swagger:** https://leadlaunch-api.onrender.com/docs
- **Usuario con datos:** `ana@dulcepunto.com` / `Dulce123` (negocio Dulce Punto con 4 productos).
- Para pruebas de registro, usa correos nuevos (ej. `qa1@correo-prueba.com`, `qa2@...`).
- El servidor gratis "duerme": la primera llamada puede tardar ~50 s.

---

## 3. Tareas por checkpoint

### CP1 · Martes 24/11 · 13:00
- [x] Traer lo último a tu rama: `git checkout ernesto/db-qa`, `git fetch origin`, `git merge origin/develop`.
- [x] Colección en `docs/pruebas/` con variables `base_url`, `token`, `negocio_id`.
- [x] **CP-01** Registro y login: `POST /auth/register` → 201 con `access_token`; `POST /auth/login` → 200.
- [x] **CP-06** Sin token: `GET /businesses/{negocio_id}/products` sin `Authorization` → 401.
- [x] **CP-07** Integridad: negocio con `whatsapp_numero: "abc"` → 422; producto con `precio_base: -20` → 422.
- [x] Evidencias (capturas o export) en `docs/pruebas/evidencias/` y tabla en `docs/pruebas/RESULTADOS.md` (ID, fecha, entrada, esperado, obtenido, OK/Falla).
- [x] Commit, push y avisar a César para el PR a `develop`.

### CP2 · Martes 24/11 · 21:00
- [x] **CP-02** Alta de producto: `POST /products` con `{"sku":"TT-015","nombre":"Torta Temática 15 porciones","precio_base":95.00}` → 201.
- [x] **CP-03** Radar: `POST /radar/scan` con `{"radio_km":3.0}` → 201, `total_fricciones` > 0 y luego el dashboard con `total_escaneos` +1.
- [x] **CP-10** Aislamiento: con el token de OTRO usuario, `GET /businesses/{negocio_id_de_Ana}/products` → 404.
- [x] **CP-08** Inyección SQL (adelanto): registrar con `nombre_completo: "'; DROP TABLE negocios; --"` → 201 y la API sigue funcionando (el texto se guarda tal cual).
- [x] Actualizar `RESULTADOS.md`, commit, push y avisar.

### Fase 2 (por definir)
- [ ] **CP-04** Campaña IA, **CP-05** Cotización + GANADA, **CP-09** express no permitido → 400 (cuando existan los endpoints).

## 4. Dependencias
- **Necesito de César:** endpoints desplegados (ya disponibles para CP1 y CP2).
