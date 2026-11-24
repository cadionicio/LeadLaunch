# LeadLaunch — App Android (`/android`)

Implementación completa de los checkpoints **CP0, CP1 y CP2** (`S01–S10`) en Kotlin + Jetpack Compose + Material 3 + Retrofit + DataStore + osmdroid.

## 1. Cómo integrarlo en tu rama `giancarlos/android`
1. Clona el repositorio del grupo y cambia a tu rama:
   ```bash
   git checkout -b giancarlos/android
   ```
2. Descomprime la carpeta `android/` en la raíz del repositorio.
3. Abre `android/` en Android Studio, deja que sincronice Gradle y ejecuta en emulador o celular físico.

## 2. Cómo probar ahora (Modo Simulado)
Por defecto `AppConfig.USE_MOCK_API = true`:
- **Usuario demo con negocio ya creado:** `demo@leadlaunch.pe` / cualquier contraseña de 6+ caracteres → entra directo a **S05 Inicio**.
- **Registro nuevo (S03):** cualquier correo nuevo + contraseña con 8 caracteres, mayúscula y número → pasa a **S04 Registrar Negocio** con mapa osmdroid.

## 3. Cómo conectar al backend real de César
Cuando César comparta la URL desplegada, edita `app/src/main/java/com/leadlaunch/data/remote/AppConfig.kt`:
```kotlin
const val USE_MOCK_API: Boolean = false
const val BASE_URL: String = "https://TU-URL-DE-RENDER/api/v1/"
```
