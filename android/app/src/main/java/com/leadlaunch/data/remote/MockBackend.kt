package com.leadlaunch.data.remote

import kotlinx.coroutines.delay
import java.util.UUID

object MockBackend {

    private val registeredEmails = mutableSetOf("demo@leadlaunch.pe")
    var currentBusiness: BusinessResponse? = null

    val products = mutableListOf(
        ProductDto(
            productoId = "prod-001",
            sku = "TORT-CHOC-15",
            nombre = "Torta de Chocolate Artesanal",
            descripcion = "Bizcocho húmedo relleno de fudge casero (15 porciones)",
            precioBase = 75.00,
            tiempoPrepHoras = 6,
            stockDisponible = 5,
            permiteExpress = true,
            activo = true
        ),
        ProductDto(
            productoId = "prod-002",
            sku = "CUP-RED-12",
            nombre = "Caja de 12 Cupcakes Red Velvet",
            descripcion = "Con frosting de queso crema y toppings personalizados",
            precioBase = 48.00,
            tiempoPrepHoras = 3,
            stockDisponible = 8,
            permiteExpress = true,
            activo = true
        ),
        ProductDto(
            productoId = "prod-003",
            sku = "BOC-MIX-50",
            nombre = "Pack 50 Bocaditos Mixtos",
            descripcion = "Empanaditas, alfajores y triples para eventos",
            precioBase = 65.00,
            tiempoPrepHoras = 12,
            stockDisponible = 0,
            permiteExpress = false,
            activo = true
        )
    )

    var rulesConfig = RulesConfigPayload(
        politica = PolicyPayloadDto(
            permiteExpress = true,
            permiteAcumularDesc = false,
            aceptaYapePlin = true,
            horarioAtencion = "09:00 - 20:00"
        ),
        reglas = listOf(
            RuleItemDto(tipoRegla = "RECARGO_EXPRESS", valorPorcentaje = 15.0, activa = true),
            RuleItemDto(tipoRegla = "DESC_BIENVENIDA", valorPorcentaje = 10.0, activa = true)
        )
    )

    var lastScan: RadarScanResponse? = null

    suspend fun login(request: LoginRequest): ApiResult<AuthResponse> {
        delay(450)
        if (request.correo.isBlank() || request.password.length < 6) {
            return ApiResult.Error("No pudimos iniciar sesión. Revisa tus datos.")
        }
        val isDemo = request.correo.equals("demo@leadlaunch.pe", ignoreCase = true)
        if (isDemo && currentBusiness == null) {
            currentBusiness = BusinessResponse(
                negocioId = "neg-demo-001",
                rubroId = 1,
                razonComercial = "Dulce Detalle",
                descripcion = "Tortas temáticas y bocaditos artesanales en Los Olivos",
                whatsappNumero = "51987654321",
                distrito = "Los Olivos",
                latitud = -11.9912,
                longitud = -77.0706,
                radioCoberturaKm = 3.0
            )
        }
        return ApiResult.Success(
            AuthResponse(
                accessToken = "mock-jwt-${UUID.randomUUID()}",
                usuario = UserDto(
                    usuarioId = "usr-001",
                    nombreCompleto = if (isDemo) "Ana Torres" else "Emprendedor",
                    correo = request.correo
                ),
                negocioId = currentBusiness?.negocioId
            )
        )
    }

    suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> {
        delay(450)
        if (registeredEmails.contains(request.correo.lowercase())) {
            return ApiResult.Error(
                message = "Este correo ya está registrado.",
                code = "CORREO_DUPLICADO",
                errorFields = listOf("correo")
            )
        }
        registeredEmails.add(request.correo.lowercase())
        currentBusiness = null
        return ApiResult.Success(
            AuthResponse(
                accessToken = "mock-jwt-${UUID.randomUUID()}",
                usuario = UserDto(
                    usuarioId = UUID.randomUUID().toString(),
                    nombreCompleto = request.nombreCompleto,
                    correo = request.correo
                ),
                negocioId = null
            )
        )
    }

    suspend fun createBusiness(request: BusinessCreateRequest): ApiResult<BusinessResponse> {
        delay(450)
        val created = BusinessResponse(
            negocioId = "neg-${UUID.randomUUID().toString().take(8)}",
            rubroId = request.rubroId,
            razonComercial = request.razonComercial,
            descripcion = request.descripcion,
            whatsappNumero = request.whatsappNumero,
            distrito = request.distrito,
            latitud = request.latitud,
            longitud = request.longitud,
            radioCoberturaKm = request.radioCoberturaKm
        )
        currentBusiness = created
        return ApiResult.Success(created)
    }

    suspend fun getDashboard(): ApiResult<DashboardResponse> {
        delay(400)
        val biz = currentBusiness ?: BusinessResponse(
            negocioId = "neg-demo-001",
            rubroId = 1,
            razonComercial = "Dulce Detalle",
            descripcion = "Tortas artesanales",
            whatsappNumero = "51987654321",
            distrito = "Los Olivos",
            latitud = -11.9912,
            longitud = -77.0706,
            radioCoberturaKm = 3.0
        )
        val activeCount = products.count { it.activo && it.stockDisponible > 0 }
        val findingsCount = lastScan?.totalFricciones ?: 4
        return ApiResult.Success(
            DashboardResponse(
                negocioId = biz.negocioId,
                razonComercial = biz.razonComercial,
                rubroNombre = "Repostería y Pastelería Artesanal",
                distrito = biz.distrito,
                totalEscaneos = if (lastScan != null) 2 else 1,
                totalCotizaciones = 13,
                totalVentasGanadas = 5,
                montoVentasGanadas = 415.00,
                tasaConversionPct = 38.5,
                productosActivos = activeCount,
                friccionesDetectadas = findingsCount,
                ultimaAlertaAdaptativa = "3 pastelerías cercanas tienen quejas por demora en entregas de fin de semana. Destaca tu servicio Express de 6 horas."
            )
        )
    }

    suspend fun getProducts(): ApiResult<ProductListResponse> {
        delay(300)
        return ApiResult.Success(ProductListResponse(items = products.toList(), page = 1, size = 20, total = products.size))
    }

    suspend fun createProduct(request: ProductCreateRequest): ApiResult<ProductDto> {
        delay(400)
        if (products.any { it.sku.equals(request.sku, ignoreCase = true) }) {
            return ApiResult.Error(
                message = "Ya existe un producto con ese SKU.",
                code = "SKU_DUPLICADO",
                errorFields = listOf("sku")
            )
        }
        val item = ProductDto(
            productoId = "prod-${UUID.randomUUID().toString().take(6)}",
            sku = request.sku.uppercase(),
            nombre = request.nombre,
            descripcion = request.descripcion,
            precioBase = request.precioBase,
            tiempoPrepHoras = request.tiempoPrepHoras,
            stockDisponible = request.stockDisponible,
            permiteExpress = request.permiteExpress,
            activo = true
        )
        products.add(0, item)
        return ApiResult.Success(item)
    }

    suspend fun getRules(): ApiResult<RulesConfigPayload> {
        delay(300)
        return ApiResult.Success(rulesConfig)
    }

    suspend fun updateRules(newConfig: RulesConfigPayload): ApiResult<RulesConfigPayload> {
        delay(350)
        rulesConfig = newConfig
        return ApiResult.Success(rulesConfig)
    }

    suspend fun scanRadar(radiusKm: Double): ApiResult<RadarScanResponse> {
        delay(900)
        val bizLat = currentBusiness?.latitud ?: -11.9912
        val bizLon = currentBusiness?.longitud ?: -77.0706
        val scan = RadarScanResponse(
            escaneoId = "scan-${UUID.randomUUID().toString().take(8)}",
            fuenteDatos = "DATASET_FALLBACK",
            radioBusquedaKm = radiusKm,
            totalAnalizados = 3,
            totalFricciones = 4,
            competidores = listOf(
                CompetitorDto(
                    competidorId = "comp-1",
                    nombre = "Pastelería San Jorge",
                    distrito = "Los Olivos",
                    latitud = bizLat + 0.005,
                    longitud = bizLon - 0.004,
                    calificacionProm = 3.2,
                    fricciones = listOf(
                        CompetitorFrictionDto("DEMORA_ENTREGA", "Demora en entrega", 2, "Incumplimiento de horario de entrega en fechas clave.")
                    )
                ),
                CompetitorDto(
                    competidorId = "comp-2",
                    nombre = "Dulcería El Trigal",
                    distrito = "Los Olivos",
                    latitud = bizLat - 0.004,
                    longitud = bizLon + 0.006,
                    calificacionProm = 2.9,
                    fricciones = listOf(
                        CompetitorFrictionDto("SIN_STOCK", "Falta de disponibilidad", 1, "Sin opción express para pedidos de último minuto."),
                        CompetitorFrictionDto("MALA_ATENCION", "Mala atención", 2, "Demora al cotizar y falta de transparencia en precios.")
                    )
                ),
                CompetitorDto(
                    competidorId = "comp-3",
                    nombre = "Café & Postres Norte",
                    distrito = "Independencia",
                    latitud = bizLat + 0.008,
                    longitud = bizLon + 0.003,
                    calificacionProm = 3.6,
                    fricciones = listOf(
                        CompetitorFrictionDto("PRECIO_ALTO", "Precio elevado", 3, "Costo de envío percibido como excesivo en zona local.")
                    )
                )
            ),
            resumenFricciones = listOf(
                FrictionSummaryDto("DEMORA_ENTREGA", "Demora en entrega", 1),
                FrictionSummaryDto("SIN_STOCK", "Falta de disponibilidad", 1),
                FrictionSummaryDto("MALA_ATENCION", "Mala atención", 1),
                FrictionSummaryDto("PRECIO_ALTO", "Precio elevado", 1)
            )
        )
        lastScan = scan
        return ApiResult.Success(scan)
    }
}