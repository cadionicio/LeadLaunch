package com.leadlaunch.data.remote

import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Simulador local con el formato exacto del contrato §7.
 * Permite probar S01-S10 mientras el backend se despliega en Render/Koyeb.
 */
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

    var rules = RulesDto(
        permiteExpress = true,
        recargoExpressPct = 15.0,
        permiteDescBienvenida = true,
        descBienvenidaPct = 10.0,
        aceptaYapePlin = true
    )

    var lastScan: RadarScanResponse? = null

    suspend fun login(request: LoginRequest): ApiResult<AuthResponse> {
        delay(500)
        if (request.correo.isBlank() || request.password.length < 6) {
            return ApiResult.Error("No pudimos iniciar sesión. Revisa tus datos.")
        }
        val hasBusiness = request.correo.equals("demo@leadlaunch.pe", ignoreCase = true)
        if (hasBusiness && currentBusiness == null) {
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
                token = "mock-jwt-${UUID.randomUUID()}",
                usuario = UserDto(
                    usuarioId = "usr-001",
                    nombreCompleto = if (hasBusiness) "Ana Torres" else "Emprendedor",
                    correo = request.correo
                ),
                negocioId = if (hasBusiness) currentBusiness?.negocioId else currentBusiness?.negocioId
            )
        )
    }

    suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> {
        delay(500)
        if (registeredEmails.contains(request.correo.lowercase())) {
            return ApiResult.Error(
                message = "Este correo ya está registrado.",
                code = "EMAIL_EXISTS",
                fieldErrors = mapOf("correo" to "Ya existe una cuenta con este correo.")
            )
        }
        registeredEmails.add(request.correo.lowercase())
        currentBusiness = null
        return ApiResult.Success(
            AuthResponse(
                token = "mock-jwt-${UUID.randomUUID()}",
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
        delay(500)
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
        delay(450)
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
        val findingsCount = lastScan?.hallazgos?.size ?: 4
        return ApiResult.Success(
            DashboardResponse(
                negocioId = biz.negocioId,
                razonComercial = biz.razonComercial,
                distrito = biz.distrito,
                productosActivos = activeCount,
                friccionesDetectadas = findingsCount,
                tasaConversionPct = 38.5,
                totalVentasGanadas = 5,
                montoVentasGanadas = 415.00,
                ultimaAlertaAdaptativa = "3 pastelerías cercanas tienen quejas por demora en entregas de fin de semana. Destaca tu servicio Express de 6 horas."
            )
        )
    }

    suspend fun getProducts(): ApiResult<ProductListResponse> {
        delay(350)
        return ApiResult.Success(ProductListResponse(items = products.toList()))
    }

    suspend fun createProduct(request: ProductCreateRequest): ApiResult<ProductDto> {
        delay(450)
        if (products.any { it.sku.equals(request.sku, ignoreCase = true) }) {
            return ApiResult.Error(
                message = "El SKU ya existe en tu catálogo.",
                code = "DUPLICATE_SKU",
                fieldErrors = mapOf("sku" to "Este SKU ya está registrado en tu catálogo.")
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

    suspend fun getRules(): ApiResult<RulesDto> {
        delay(300)
        return ApiResult.Success(rules)
    }

    suspend fun updateRules(newRules: RulesDto): ApiResult<RulesDto> {
        delay(400)
        rules = newRules
        return ApiResult.Success(rules)
    }

    suspend fun scanRadar(radiusKm: Double): ApiResult<RadarScanResponse> {
        delay(1200)
        val bizLat = currentBusiness?.latitud ?: -11.9912
        val bizLon = currentBusiness?.longitud ?: -77.0706
        val scan = RadarScanResponse(
            escaneoId = "scan-${UUID.randomUUID().toString().take(8)}",
            fuenteDatos = "DATASET_FALLBACK",
            radioBusquedaKm = radiusKm,
            competidores = listOf(
                CompetitorDto("comp-1", "Pastelería San Jorge", "Los Olivos", bizLat + 0.005, bizLon - 0.004, 3.2, 2),
                CompetitorDto("comp-2", "Dulcería El Trigal", "Los Olivos", bizLat - 0.004, bizLon + 0.006, 2.9, 2),
                CompetitorDto("comp-3", "Café & Postres Norte", "Independencia", bizLat + 0.008, bizLon + 0.003, 3.6, 1)
            ),
            hallazgos = listOf(
                FindingDto(
                    hallazgoId = "h-1",
                    competidorNombre = "Pastelería San Jorge",
                    tipoFriccion = "Demora en entrega",
                    calificacion = 2,
                    textoResena = "Pedí una torta para las 4pm y llegó a las 7pm, casi se acaba el cumpleaños.",
                    resumenDolor = "Incumplimiento de horario de entrega en fechas clave."
                ),
                FindingDto(
                    hallazgoId = "h-2",
                    competidorNombre = "Dulcería El Trigal",
                    tipoFriccion = "Falta de disponibilidad",
                    calificacion = 1,
                    textoResena = "Nunca tienen tortas listas para el mismo día los sábados.",
                    resumenDolor = "Sin opción express para pedidos de último minuto."
                ),
                FindingDto(
                    hallazgoId = "h-3",
                    competidorNombre = "Dulcería El Trigal",
                    tipoFriccion = "Mala atención",
                    calificacion = 2,
                    textoResena = "Tardan horas en responder por WhatsApp y no mandan precios claros.",
                    resumenDolor = "Demora al cotizar y falta de transparencia en precios."
                ),
                FindingDto(
                    hallazgoId = "h-4",
                    competidorNombre = "Café & Postres Norte",
                    tipoFriccion = "Precio elevado",
                    calificacion = 3,
                    textoResena = "Los bocaditos son ricos pero cobran S/ 25 de delivery dentro del mismo distrito.",
                    resumenDolor = "Costo de envío percibido como excesivo en zona local."
                )
            )
        )
        lastScan = scan
        return ApiResult.Success(scan)
    }
}
