# LeadLaunch — App Android (`/android`)

App en Kotlin + Jetpack Compose + Material 3 + Retrofit + DataStore + osmdroid.
Responsable: Giancarlos · Rama: `giancarlos/android`.

## 1. Estado actual
| Pantallas | Datos | Interruptor en `AppConfig.kt` |
|---|---|---|
| S01–S10 (Fase 1: cuenta, negocio, catálogo, reglas, dashboard, radar) | **Backend real** en Render | `USE_MOCK_API = false` |
| S11–S16 (Fase 2: campañas, cotizaciones, historial, analítica) | **Simulados**, con aviso "Vista previa con datos simulados (Fase 2)" | `USE_MOCK_PHASE2 = true` |
| S17 (perfil) | Sesión real | — |

Backend: `https://leadlaunch-api.onrender.com/api/v1/` · Contrato: `docs/api/CONTRATOS_API.md`.

## 2. Cómo ejecutar
1. En tu rama: `git checkout giancarlos/android` y luego `git fetch origin` y `git merge origin/develop`.
2. Abre la carpeta `android/` en Android Studio, deja que sincronice Gradle y ejecuta en emulador o celular.
3. Cuenta de prueba con datos reales: **`ana@dulcepunto.com` / `Dulce123`** (Dulce Punto, 4 productos).
4. El servidor gratuito "duerme": la primera llamada puede tardar ~50 s.

## 3. Modo totalmente simulado (sin internet)
En `app/src/main/java/com/leadlaunch/data/remote/AppConfig.kt` pon `USE_MOCK_API = true`.
- Usuario demo: `demo@leadlaunch.pe` / cualquier contraseña de 6+ caracteres → entra directo a S05.

## 4. Conectar la Fase 2 al backend
Cuando existan los endpoints de campañas y cotizaciones, pon `USE_MOCK_PHASE2 = false`.
El aviso de datos simulados desaparece solo.
