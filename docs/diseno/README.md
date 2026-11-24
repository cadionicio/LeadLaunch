# LeadLaunch — Diseño de pantallas (referencia visual)

Maquetas **de diseño** (no son capturas de la app). Son la referencia visual para programar y para la presentación.
Resolución 1080 × 2400 px (base 360 × 800 dp). Especificación completa: `docs/especificaciones/LEADLAUNCH_Especificacion_UX_UI_v1.md` (§7, fichas S01–S17).

El nombre de cada archivo coincide con la **ruta** de la app (`navigation/Routes.kt`) y con la carpeta del código:
`S05_home.png` ↔ `Routes.S05_HOME = "s05_home"` ↔ `ui/screens/s05_home/`.

| Archivo | Pantalla | Fase | Código en la app |
|---|---|:-:|---|
| `S01_splash.png` | Splash / bienvenida | 1 | `s01_splash/SplashScreen.kt` |
| `S02_login.png` | Iniciar sesión | 1 | `s02_login/LoginScreen.kt` |
| `S03_register.png` | Crear cuenta | 1 | `s03_register/RegisterScreen.kt` |
| `S04_business.png` | Registrar negocio | 1 | `s04_business/RegisterBusinessScreen.kt` |
| `S05_home.png` | Dashboard / Inicio | 1 | `s05_home/HomeScreen.kt` |
| `S06_catalog.png` | Catálogo de productos | 1 | `s06_catalog/CatalogScreen.kt` |
| `S07_product.png` | Agregar / editar producto | 1 | `s07_product/CreateProductScreen.kt` |
| `S08_rules.png` | Reglas de precios | 1 | `s08_rules/RulesScreen.kt` |
| `S09_radar.png` | Radar local | 1 | `s09_radar/RadarScreen.kt` |
| `S10_findings.png` | Hallazgos y fricciones | 1 | `s10_findings/FindingsScreen.kt` |
| `S11_campaigns.png` | Campañas con IA | 2 | `s11_campaigns/CampaignCreateScreen.kt` |
| `S12_campaign_result.png` | Campaña generada | 2 | `s12_campaign_result/CampaignResultScreen.kt` |
| `S13_create_quote.png` | Nueva cotización | 2 | `s13_quotes/CreateQuoteScreen.kt` |
| `S14_quote_preview.png` | Vista previa de cotización | 2 | `s14_quote_preview/QuotePreviewScreen.kt` |
| `S15_quotes.png` | Historial de cotizaciones | 2 | `s15_quotes_history/QuotesHistoryScreen.kt` |
| `S16_analytics.png` | Resultados y conversión | 2 | `s16_analytics/AnalyticsScreen.kt` |
| `S17_more.png` | Mi perfil y configuración | 2 | `s17_profile/ProfileSettingsScreen.kt` |

## Diferencias aprobadas entre el diseño y la implementación
El diseño se sigue en estructura, jerarquía, colores y textos. Estas diferencias son **intencionales** (decisiones de `00_PLAN_GENERAL.md` §8 y `04_PLAN_FASE2.md` §2):

| Pantalla | En el diseño | En la app | Decisión |
|---|---|---|---|
| S02 | Botón "Continuar con Google" y "¿Olvidaste tu contraseña?" activos | Visibles con "(Próximamente)" | D6 |
| S04 | "Radio de búsqueda" con 5 km | "Radio del radar", de 0,5 a 25 km | D5 |
| S05 | Mini-gráficas y variaciones (+8 pp, +12 %) | Solo KPIs reales; sin variaciones mientras no haya histórico | Especificación UX S05 |
| S06/S07 | Fotos de productos | Placeholder con la inicial del producto | D7 |
| S07 | Tiempo de preparación "45 min" | En **horas** (≥ 1) | D4 |
| S08 | Medios de pago con tarjeta +2,5 % y Yape/Plin −3 % | Solo el check "Acepta Yape/Plin" | D8 |
| S10 | Datos de competidores reales | Dataset demo con el aviso "Datos de referencia (demo)" | D12 / D14 |
| S12 | "Guion para reel (15–30 s)" | "Guion para reel (15 s)" | D18 |
| S13 | "3 productos seleccionados" | El contador debe decir unidades o productos de forma coherente con S14 | Especificación UX §9 |
| S14 | Ejemplo: subtotal 217.00 + 32.55 − 21.70 = **227.85** | Montos calculados por el backend; antes de enviar, la app pregunta "¿Enviaste la cotización?" | D21–D24 |
| S16 | Fechas "ene" en el eje | Fechas reales del periodo elegido (7/30/90 días) | D27 |
| S17 | Sin "Cerrar sesión" en la especificación original | El diseño ya incluye "Cerrar sesión" ✔ | — |

Los textos de ejemplo (Ana Rodríguez, Dulce Punto, COT-0278, S/ 2,340…) son **datos de maqueta**, no datos reales.
