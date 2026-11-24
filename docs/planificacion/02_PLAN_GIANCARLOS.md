# PLAN INDIVIDUAL — GIANCARLOS
**Rol:** Desarrollador de la app Android (Kotlin)
**Rama:** `giancarlos/android` · **Carpeta:** `/android`
**Leer antes:** `00_PLAN_GENERAL.md` (§5 ramas, §7 contrato API, §8 decisiones) y la Especificación UX/UI (fichas S01–S17, tokens §6).

---

## 1. Responsabilidades
1. Crear el proyecto Android y su arquitectura (MVVM: `ui` → `viewmodel` → `repository` → `data/remote`).
2. Implementar el sistema de diseño (colores, tipografía, componentes) de la especificación UX.
3. Construir las pantallas S01–S17 y conectarlas a la API con Retrofit.
4. Manejar sesión JWT, estados de carga/vacío/error y el envío a WhatsApp por `Intent`.

## 2. Stack recomendado
- Kotlin + **Jetpack Compose** + Material 3 + Navigation Compose.
- **Retrofit 2 + OkHttp** (interceptor que agrega `Authorization: Bearer <token>`) + Gson/Moshi + Corrutinas (`Dispatchers.IO`).
- **DataStore** para guardar token y `negocio_id`.
- **osmdroid** para el mapa del radar (decisión D10).
- `minSdk 24`, diseño base 360 × 800 dp.

## 3. Estructura sugerida
```text
android/app/src/main/java/com/leadlaunch/
├── data/remote/      (ApiService, DTOs, AuthInterceptor, RetrofitClient)
├── data/local/       (SessionStore con DataStore)
├── data/repository/  (AuthRepository, BusinessRepository, ProductRepository, RulesRepository, RadarRepository)
├── ui/theme/         (Color.kt, Type.kt, Shape.kt — tokens)
├── ui/components/    (PrimaryButton, AppTextField, CardKPI, ProductRow, FilterChip, FrictionChip,
│                      SwitchRule, StatusBadge, EmptyState, BottomNavBar, AppBar)
├── ui/screens/       (s01_splash, s02_login, s03_register, s04_business, s05_home, s06_catalog,
│                      s07_product, s08_rules, s09_radar, s10_findings, ...)
└── navigation/       (NavGraph, rutas)
```

---

## 4. Tareas por checkpoint

### CP0 · Arranque — Lunes 23/11 · 23:30
- [ ] Clonar el repo y cambiar a `giancarlos/android` (ver `00_PLAN_GENERAL.md` §5.2).
- [ ] Crear proyecto Android en `/android` (Empty Compose Activity, paquete `com.leadlaunch`).
- [ ] Agregar dependencias (Retrofit, OkHttp, Navigation, DataStore, osmdroid).
- [ ] `ui/theme`: tokens del §6 de la especificación UX (primario `#175CF4`, primario oscuro `#113CBA`, fondo `#F8FAFE`, texto `#16233E`, secundario `#68738A`, bordes `#E3EAF4`, éxito `#129E61`, error `#D94D69`, índigo `#7754E8`; radios 12–16 dp; botones 48–52 dp).
- [ ] NavGraph con rutas vacías S01–S10 y barra inferior de 5 destinos (Inicio, Catálogo, Cotizar, Campañas, Más).
- [ ] Commit + push y PR → `develop`.

### CP1 · Cuenta y negocio — Martes 24/11 · 13:00
- [ ] Componentes base: `PrimaryButton` (enabled/disabled/loading), `AppTextField` (error debajo), `AppBar`.
- [ ] `RetrofitClient` con `BASE_URL` configurable + `AuthInterceptor` + `SessionStore`.
- [ ] Manejo de errores HTTP: leer `{error:{codigo, mensaje, campos}}`; 401 → limpiar sesión e ir a S02; sin internet → snackbar "Reintentar".
- [ ] **S01 Splash:** si hay token y `negocio_id` → S05; si hay token sin negocio → S04; si no → S02.
- [ ] **S02 Login:** validar correo y contraseña; botón bloqueado durante la petición; error "No pudimos iniciar sesión. Revisa tus datos."; Google y "¿Olvidaste tu contraseña?" deshabilitados con "Próximamente" (D6).
- [ ] **S03 Crear cuenta:** checks en vivo (8 caracteres, mayúscula, número); correo existente → error junto al campo sin borrar lo demás.
- [ ] **S04 Registrar negocio:** nombre, categoría (3 rubros con IDs 1–3), descripción (obligatoria aunque no esté en la maqueta), distrito, ubicación (mapa osmdroid: tocar para fijar pin o usar ubicación del dispositivo con permiso), WhatsApp (solo dígitos, ej. `51987654321`), "Radio del radar" (D5).
- [ ] Mientras el backend no esté desplegado: usar respuestas simuladas con el formato del §7; cambiar a la URL real apenas César la comparta.
- [ ] PR → `develop`.

### CP2 · Catálogo, reglas, dashboard, radar — Martes 24/11 · 21:00
- [ ] **S05 Inicio:** saludo con nombre, tarjetas KPI (productos activos, fricciones, tasa de conversión, ventas ganadas), tarjeta de última campaña (o vacío). Sin mini-gráficos ni % de variación inventados. Skeleton al cargar.
- [ ] **S06 Catálogo:** lista paginada, buscador local, chips Todos/Activos/Sin stock, `ProductRow` con placeholder de inicial (D7), FAB + → S07.
- [ ] **S07 Producto (modo crear):** nombre, SKU, descripción, precio (> 0), stock (≥ 0), tiempo de preparación **en horas** (D4), switch express; 422 SKU duplicado → error en el campo; volver a S06 y refrescar.
- [ ] **S08 Reglas de precios:** cargar con `GET /rules`, switches y porcentajes de recargo express y descuento bienvenida, check Yape/Plin (sin tarifas, D8), botón guardar → snackbar "Reglas actualizadas".
- [ ] *(Meta extra)* **S09 Radar:** mapa con círculo del radio y pin del negocio, selector de radio, botón "Escanear competencia" con progreso.
- [ ] *(Meta extra)* **S10 Hallazgos:** pestañas Competidores / Fricciones, tarjetas con calificación y chips; aviso "Datos de referencia (demo)" si `fuente_datos = DATASET_FALLBACK` (D12).
- [ ] PR → `develop`.

### Fase 2 (por definir)
- [ ] S11 y S12 (campaña IA: generar, loader, copiar post y guion).
- [ ] S13, paso de datos del prospecto (nombre, teléfono, express, cliente nuevo) y S14 (vista previa + WhatsApp Intent con `https://wa.me/...`; manejar "WhatsApp no instalado").
- [ ] S15 historial (pestaña Cotizar abre S15, D9), S16 resultados, S17 perfil + cerrar sesión.
- [ ] Estados vacío/error/carga en todas las pantallas y accesibilidad (targets ≥ 48 dp, `contentDescription`).

---

## 5. Notas
- Si la imagen del tablero (`sistema_ui_de_leadlaunch_en_español.png`) no está en el repo, pedirla a César; es la referencia visual.
- Los datos de la maqueta (Ana, Dulce Punto, Torta de chocolate…) son solo de demo; la app muestra lo que devuelve la API.
- Montos en soles siempre con formato `S/ 75.00`.

## 6. Dependencias
- **Necesito de César:** URL del backend (CP1) y avisos de cambios de contrato.
- **Necesito de Ernesto:** usuario de prueba con negocio y productos cargados en Neon para probar S05–S08.
