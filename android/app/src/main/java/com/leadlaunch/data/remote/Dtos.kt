package com.leadlaunch.data.remote

import com.google.gson.annotations.SerializedName

// --- Error Estándar del Contrato (§7) ---
data class ApiErrorWrapper(
    val error: ApiErrorDetail? = null
)

data class ApiErrorDetail(
    val codigo: String? = null,
    val mensaje: String? = null,
    val campos: List<String> = emptyList()
)

// --- Auth (#1, #2) ---
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
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String = "bearer",
    val usuario: UserDto,
    @SerializedName("negocio_id") val negocioId: String? = null
)

data class UserDto(
    @SerializedName("usuario_id") val usuarioId: String,
    @SerializedName("nombre_completo") val nombreCompleto: String,
    val correo: String,
    val rol: String = "EMPRENDEDOR"
)

// --- Negocio (#3) ---
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

// --- Dashboard (#4) ---
data class DashboardResponse(
    @SerializedName("negocio_id") val negocioId: String? = null,
    @SerializedName("razon_comercial") val razonComercial: String,
    @SerializedName("rubro_nombre") val rubroNombre: String? = null,
    val distrito: String,
    @SerializedName("total_escaneos") val totalEscaneos: Int = 0,
    @SerializedName("total_cotizaciones") val totalCotizaciones: Int = 0,
    @SerializedName("total_ventas_ganadas") val totalVentasGanadas: Int,
    @SerializedName("monto_ventas_ganadas") val montoVentasGanadas: Double,
    @SerializedName("tasa_conversion_pct") val tasaConversionPct: Double,
    @SerializedName("productos_activos") val productosActivos: Int,
    @SerializedName("fricciones_detectadas") val friccionesDetectadas: Int,
    @SerializedName("ultima_alerta_adaptativa") val ultimaAlertaAdaptativa: String? = null
)

// --- Productos (#5, #6) ---
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
    val page: Int = 1,
    val size: Int = 20,
    val total: Int = items.size
)

// --- Reglas y Políticas (#7, #7b) ---
data class PolicyPayloadDto(
    @SerializedName("permite_express") val permiteExpress: Boolean,
    @SerializedName("permite_acumular_desc") val permiteAcumularDesc: Boolean = false,
    @SerializedName("acepta_yape_plin") val aceptaYapePlin: Boolean,
    @SerializedName("horario_atencion") val horarioAtencion: String = "09:00 - 20:00"
)

data class RuleItemDto(
    @SerializedName("tipo_regla") val tipoRegla: String,
    @SerializedName("valor_porcentaje") val valorPorcentaje: Double,
    @SerializedName("monto_minimo") val montoMinimo: Double = 0.0,
    @SerializedName("cantidad_minima") val cantidadMinima: Int = 1,
    val activa: Boolean = true
)

data class RulesConfigPayload(
    val politica: PolicyPayloadDto,
    val reglas: List<RuleItemDto>
)

// --- Radar y Hallazgos (#8) ---
data class RadarScanRequest(
    @SerializedName("radio_km") val radioKm: Double
)

data class CompetitorFrictionDto(
    @SerializedName("tipo_codigo") val tipoCodigo: String,
    @SerializedName("tipo_nombre") val tipoNombre: String,
    val calificacion: Int,
    @SerializedName("resumen_dolor") val resumenDolor: String
)

data class CompetitorDto(
    @SerializedName("competidor_id") val competidorId: String,
    val nombre: String,
    val distrito: String,
    val latitud: Double,
    val longitud: Double,
    @SerializedName("calificacion_prom") val calificacionProm: Double,
    val fricciones: List<CompetitorFrictionDto> = emptyList()
)

data class FrictionSummaryDto(
    @SerializedName("tipo_codigo") val tipoCodigo: String,
    @SerializedName("tipo_nombre") val tipoNombre: String,
    val cantidad: Int
)

data class RadarScanResponse(
    @SerializedName("escaneo_id") val escaneoId: String,
    @SerializedName("fuente_datos") val fuenteDatos: String,
    @SerializedName("radio_busqueda_km") val radioBusquedaKm: Double,
    @SerializedName("total_analizados") val totalAnalizados: Int,
    @SerializedName("total_fricciones") val totalFricciones: Int,
    @SerializedName("ejecutado_en") val ejecutadoEn: String? = null,
    val competidores: List<CompetitorDto>,
    @SerializedName("resumen_fricciones") val resumenFricciones: List<FrictionSummaryDto> = emptyList()
)

// --- FASE 2: Campañas IA (#9 - CP3) ---
data class CampaignGenerateRequest(
    @SerializedName("escaneo_id") val escaneoId: String? = null,
    val fricciones: List<String> = emptyList(),
    val objetivo: String = "Atraer más pedidos",
    val canal: String = "Redes sociales (Instagram, Facebook, TikTok)",
    val tono: String = "Cercano y confiable"
)

data class CampaignResponse(
    @SerializedName("campana_id") val campanaId: String,
    @SerializedName("alerta_adaptativa") val alertaAdaptativa: String,
    @SerializedName("post_redes") val postRedes: String,
    @SerializedName("guion_reel_15s") val guionReel15s: String,
    val estado: String = "BORRADOR",
    @SerializedName("creado_en") val creadoEn: String? = null
)

// --- FASE 2: Cotizaciones y WhatsApp (#10, #11 - CP4 y CP5) ---
data class QuoteItemRequest(
    @SerializedName("producto_id") val productoId: String,
    val cantidad: Int
)

data class QuoteCreateRequest(
    @SerializedName("nombre_prospecto") val nombreProspecto: String,
    @SerializedName("telefono_prospecto") val telefonoProspecto: String? = null,
    @SerializedName("es_entrega_express") val esEntregaExpress: Boolean = false,
    @SerializedName("es_cliente_nuevo") val esClienteNuevo: Boolean = true,
    val items: List<QuoteItemRequest>
)

data class QuoteDetailDto(
    @SerializedName("producto_id") val productoId: String,
    @SerializedName("nombre_producto") val nombreProducto: String,
    val cantidad: Int,
    @SerializedName("precio_unitario_snap") val precioUnitarioSnap: Double,
    @SerializedName("importe_linea") val importeLinea: Double
)

data class QuoteResponse(
    @SerializedName("cotizacion_id") val cotizacionId: String,
    val codigo: String? = null,
    @SerializedName("nombre_prospecto") val nombreProspecto: String,
    @SerializedName("telefono_prospecto") val telefonoProspecto: String? = null,
    @SerializedName("es_entrega_express") val esEntregaExpress: Boolean = false,
    @SerializedName("es_cliente_nuevo") val esClienteNuevo: Boolean = true,
    val subtotal: Double,
    @SerializedName("monto_recargo") val montoRecargo: Double = 0.0,
    @SerializedName("monto_descuento") val montoDescuento: Double = 0.0,
    @SerializedName("total_neto") val totalNeto: Double,
    @SerializedName("mensaje_whatsapp") val mensajeWhatsapp: String,
    @SerializedName("whatsapp_url") val whatsappUrl: String? = null,
    val estado: String = "EMITIDA",
    @SerializedName("creado_en") val creadoEn: String? = null,
    val detalles: List<QuoteDetailDto> = emptyList()
)

data class QuoteListResponse(
    val items: List<QuoteResponse>,
    val total: Int = items.size
)

data class QuoteStatusUpdateRequest(
    val estado: String // ENVIADA_WA, GANADA, PERDIDA
)
