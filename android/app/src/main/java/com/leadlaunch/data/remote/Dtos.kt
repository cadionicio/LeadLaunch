package com.leadlaunch.data.remote

import com.google.gson.annotations.SerializedName

// --- Error Estándar del Contrato (§7) ---
data class ApiErrorWrapper(
    val error: ApiErrorDetail? = null
)

data class ApiErrorDetail(
    val codigo: String? = null,
    val mensaje: String? = null,
    val campos: Map<String, String>? = null
)

// --- Auth ---
data class LoginRequest(
    val correo: String,
    val password: String
)

data class RegisterRequest(
    @SerializedName("nombre_completo") val nombreCompleto: String,
    val correo: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val usuario: UserDto,
    @SerializedName("negocio_id") val negocioId: String? = null
)

data class UserDto(
    @SerializedName("usuario_id") val usuarioId: String,
    @SerializedName("nombre_completo") val nombreCompleto: String,
    val correo: String,
    val rol: String = "EMPRENDEDOR"
)

// --- Negocio ---
data class BusinessCreateRequest(
    @SerializedName("rubro_id") val rubroId: Int,
    @SerializedName("razon_comercial") val razonComercial: String,
    val descripcion: String,
    @SerializedName("whatsapp_numero") val whatsappNumero: String,
    val distrito: String,
    val latitud: Double,
    val longitud: Double,
    @SerializedName("radio_cobertura_km") val radioCoberturaKm: Double
)

data class BusinessResponse(
    @SerializedName("negocio_id") val negocioId: String,
    @SerializedName("rubro_id") val rubroId: Int,
    @SerializedName("razon_comercial") val razonComercial: String,
    val descripcion: String,
    @SerializedName("whatsapp_numero") val whatsappNumero: String,
    val distrito: String,
    val latitud: Double,
    val longitud: Double,
    @SerializedName("radio_cobertura_km") val radioCoberturaKm: Double
)

// --- Dashboard (S05) ---
data class DashboardResponse(
    @SerializedName("negocio_id") val negocioId: String,
    @SerializedName("razon_comercial") val razonComercial: String,
    val distrito: String,
    @SerializedName("productos_activos") val productosActivos: Int,
    @SerializedName("fricciones_detectadas") val friccionesDetectadas: Int,
    @SerializedName("tasa_conversion_pct") val tasaConversionPct: Double,
    @SerializedName("total_ventas_ganadas") val totalVentasGanadas: Int,
    @SerializedName("monto_ventas_ganadas") val montoVentasGanadas: Double,
    @SerializedName("ultima_alerta_adaptativa") val ultimaAlertaAdaptativa: String? = null
)

// --- Productos (S06, S07) ---
data class ProductDto(
    @SerializedName("producto_id") val productoId: String,
    val sku: String,
    val nombre: String,
    val descripcion: String? = null,
    @SerializedName("precio_base") val precioBase: Double,
    @SerializedName("tiempo_prep_horas") val tiempoPrepHoras: Int,
    @SerializedName("stock_disponible") val stockDisponible: Int,
    @SerializedName("permite_express") val permiteExpress: Boolean,
    val activo: Boolean = true
)

data class ProductCreateRequest(
    val sku: String,
    val nombre: String,
    val descripcion: String?,
    @SerializedName("precio_base") val precioBase: Double,
    @SerializedName("tiempo_prep_horas") val tiempoPrepHoras: Int,
    @SerializedName("stock_disponible") val stockDisponible: Int,
    @SerializedName("permite_express") val permiteExpress: Boolean
)

data class ProductListResponse(
    val items: List<ProductDto>,
    val total: Int = items.size,
    val page: Int = 1
)

// --- Reglas de Precios (S08) ---
data class RulesDto(
    @SerializedName("permite_express") val permiteExpress: Boolean,
    @SerializedName("recargo_express_pct") val recargoExpressPct: Double,
    @SerializedName("permite_desc_bienvenida") val permiteDescBienvenida: Boolean,
    @SerializedName("desc_bienvenida_pct") val descBienvenidaPct: Double,
    @SerializedName("acepta_yape_plin") val aceptaYapePlin: Boolean
)

// --- Radar y Hallazgos (S09, S10) ---
data class RadarScanRequest(
    @SerializedName("radio_busqueda_km") val radioBusquedaKm: Double
)

data class CompetitorDto(
    @SerializedName("competidor_id") val competidorId: String,
    val nombre: String,
    val distrito: String,
    val latitud: Double,
    val longitud: Double,
    @SerializedName("calificacion_prom") val calificacionProm: Double,
    @SerializedName("total_quejas") val totalQuejas: Int = 0
)

data class FindingDto(
    @SerializedName("hallazgo_id") val hallazgoId: String,
    @SerializedName("competidor_nombre") val competidorNombre: String,
    @SerializedName("tipo_friccion") val tipoFriccion: String,
    val calificacion: Int,
    @SerializedName("texto_resena") val textoResena: String,
    @SerializedName("resumen_dolor") val resumenDolor: String
)

data class RadarScanResponse(
    @SerializedName("escaneo_id") val escaneoId: String,
    @SerializedName("fuente_datos") val fuenteDatos: String, // OVERPASS_OSM, GOOGLE_PLACES, DATASET_FALLBACK
    @SerializedName("radio_busqueda_km") val radioBusquedaKm: Double,
    val competidores: List<CompetitorDto>,
    val hallazgos: List<FindingDto>
)
