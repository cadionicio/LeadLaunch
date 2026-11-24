# LeadLaunch — Contratos de la API REST
**Responsable:** César · Cualquier cambio se avisa en el grupo antes del push.

## Estado de implementación
| Endpoint | Estado |
|---|---|
| `GET /health`, `GET /health/db` | ✅ Implementado |
| `POST /auth/register` | ✅ Implementado (CP1) |
| `POST /auth/login` | ✅ Implementado (CP1) |
| `POST /businesses` | ✅ Implementado (CP1) |
| `GET /businesses/{id}` | ✅ Implementado (CP1) · extra, para S17/S04 edición |
| `GET /businesses/{id}/dashboard` | ⏳ CP2 |
| `POST`/`GET /businesses/{id}/products` | ⏳ CP2 |
| `GET`/`PUT /businesses/{id}/rules` | ⏳ CP2 |
| `POST /businesses/{id}/radar/scan` | ⏳ CP2 |

## Notas de comportamiento (CP1)
- **Contraseña:** 8–64 caracteres, al menos una mayúscula y un número. Si no cumple → `422`, `campos: ["password"]`.
- **Correo:** se guarda en minúsculas. Correo ya registrado → `422 CORREO_REGISTRADO`, `campos: ["correo"]`.
- **Login fallido** (correo o contraseña) → `401 CREDENCIALES_INVALIDAS`, mensaje "No pudimos iniciar sesión. Revisa tus datos.".
- **Token:** dura 24 h. Sin token o token inválido/expirado → `401 NO_AUTENTICADO` (la app debe limpiar sesión e ir a S02).
- **Alta de negocio:** crea también la política por defecto y las reglas base (`RECARGO_EXPRESS` 15 %, `DESC_BIENVENIDA` 10 %).
  - Un negocio por cuenta en el MVP: segundo intento → `400 NEGOCIO_EXISTENTE`.
  - `rubro_id` inexistente → `422 RUBRO_INVALIDO`.
  - `whatsapp_numero`: solo dígitos, 10–15, con código de país (ej. `51987654321`).
  - Lat/long se redondean a 6 decimales; radio a 2 (0.5–25).
- **Negocio de otro usuario** → `404 NEGOCIO_NO_ENCONTRADO` (aislamiento multi-tenant).
- La respuesta de negocio incluye `rubro_nombre`, `activo` y `creado_en` además de los campos enviados.


## Fase 1

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

