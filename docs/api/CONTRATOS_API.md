# LeadLaunch — Contratos de la API REST
**Responsable:** César · Cualquier cambio se avisa en el grupo antes del push.

## URL del backend desplegado
- **Base:** `https://leadlaunch-api.onrender.com/api/v1`
- **Swagger:** https://leadlaunch-api.onrender.com/docs
- **Salud:** https://leadlaunch-api.onrender.com/health · https://leadlaunch-api.onrender.com/health/db
- Plan gratuito de Render: tras 15 min sin uso el servidor "duerme" y la primera llamada puede tardar ~50 s. En la app, usar timeouts de al menos 60 s y un loader.

## Estado de implementación
| Endpoint | Estado |
|---|---|
| `GET /health`, `GET /health/db` | ✅ Implementado |
| `POST /auth/register` | ✅ Implementado (CP1) |
| `POST /auth/login` | ✅ Implementado (CP1) |
| `POST /businesses` | ✅ Implementado (CP1) |
| `GET /businesses/{id}` | ✅ Implementado (CP1) · extra, para S17/S04 edición |
| `GET /businesses/{id}/dashboard` | ✅ Implementado (CP2) |
| `POST`/`GET /businesses/{id}/products` | ✅ Implementado (CP2) |
| `GET`/`PUT /businesses/{id}/rules` | ✅ Implementado (CP2) |
| `POST /businesses/{id}/radar/scan` | ✅ Implementado (CP2) · por ahora solo con datos de demostración (ver abajo) |

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

## Notas de comportamiento (CP2)
- **Productos**
  - `sku`: letras, números, `-` y `_` (sin espacios), máx. 30; se guarda en MAYÚSCULAS. Duplicado en el mismo negocio → `422 SKU_DUPLICADO`.
  - `precio_base` > 0 con máx. 2 decimales · `tiempo_prep_horas` ≥ 1 (en **horas**, decisión D4) · `stock_disponible` ≥ 0.
  - Valores por defecto si no se envían: `tiempo_prep_horas` 24, `stock_disponible` 0, `permite_express` true.
  - `GET /products` devuelve solo activos, ordenados por nombre. `size` máx. 100.
  - Respuesta de producto: `{producto_id, sku, nombre, descripcion, precio_base, tiempo_prep_horas, stock_disponible, permite_express, activo, creado_en}`.
- **Reglas**
  - `GET /rules` responde `{politica:{...}, reglas:[{regla_id, tipo_regla, descripcion, valor_porcentaje, monto_minimo, cantidad_minima, activa}]}`.
  - `PUT /rules` exige `politica` completa; `reglas` es opcional y solo actualiza los tipos enviados (los demás quedan igual). Cada tipo una sola vez.
  - `tipo_regla` ∈ `RECARGO_EXPRESS`, `DESC_BIENVENIDA`, `DESC_VOLUMEN` (tarifas por medio de pago no existen, decisión D8). `valor_porcentaje` 0–100.
  - Las reglas solo afectan **cotizaciones nuevas**.
- **Dashboard**: lee la vista materializada; se refresca al crear negocio, producto o escaneo. `ultima_alerta_adaptativa` es `null` hasta que exista una campaña (Fase 2).
- **Radar** (`POST /radar/scan`, cuerpo opcional `{radio_km}`; sin cuerpo usa el radio del negocio)
  - Hoy la única fuente es `DATASET_FALLBACK`: negocios y reseñas **ficticios** ubicados alrededor del negocio. La respuesta trae `es_demo: true` y un `aviso`; **la app debe mostrar ese aviso** (D12).
  - Solo se cuentan reseñas de 1 a 3 estrellas con texto, clasificadas en los 5 tipos de `cat_tipos_friccion`.
  - Respuesta: `{escaneo_id, fuente_datos, es_demo, aviso, radio_busqueda_km, total_competidores, total_analizados, total_fricciones, nuevas_fricciones, ejecutado_en, competidores:[{competidor_id, nombre, distrito, latitud, longitud, distancia_km, calificacion_prom, total_resenas, fricciones:[{tipo_codigo, tipo_nombre, calificacion, texto_resena, resumen_dolor}]}], resumen_fricciones:[{tipo_codigo, tipo_nombre, cantidad}]}`.
  - `competidores` viene ordenado por distancia. Reescanear no duplica fricciones ya guardadas (`nuevas_fricciones` = 0).
  - Códigos de fricción: `DEMORA_ENTREGA`, `CALIDAD_PRESENTACION`, `PRECIO_DELIVERY`, `MALA_ATENCION`, `SIN_OPCIONES_URGENTES`.
- **Usuario de prueba con datos**: `ana@dulcepunto.com` / `Dulce123` (negocio Dulce Punto con 4 productos de la maqueta).


## Fase 1

Resumen de formatos. Base: `https://leadlaunch-api.onrender.com/api/v1`. Todo endpoint (salvo auth) exige `Authorization: Bearer <token>`.

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
| 8 | `POST /businesses/{id}/radar/scan` | `{radio_km}` (opcional) | `201` — ver formato completo en "Notas de comportamiento (CP2)" |

Rubros (semilla, IDs fijos para la app): `1` Repostería y Pastelería Artesanal · `2` Comida Rápida y Bocaditos · `3` Cafeterías y Postres.

---

