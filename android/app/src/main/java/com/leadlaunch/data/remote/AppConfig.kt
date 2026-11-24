package com.leadlaunch.data.remote

object AppConfig {
    // Fase 1 (CP1-CP2) conectada al backend desplegado en Render
    const val USE_MOCK_API: Boolean = false

    // Fase 2 (CP3-CP6): cambiar a false cuando César despliegue /campaigns y /quotes en Render
    const val USE_MOCK_PHASE2: Boolean = true

    const val BASE_URL: String = "https://leadlaunch-api.onrender.com/api/v1/"
}
