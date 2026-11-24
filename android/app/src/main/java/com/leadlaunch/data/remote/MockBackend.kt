package com.leadlaunch.data.remote

import android.net.Uri
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

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
    var lastCampaign: CampaignResponse? = null
    var lastQuote: QuoteResponse? = null

    val quotes = mutableListOf(
        QuoteResponse(
            cotizacionId = "cot-demo-0278",
            codigo = "COT-0278",
            nombreProspecto = "María Torres",
            telefonoProspecto = "51987654321",
            esEntregaExpress = true,
            esClienteNuevo = true,
            subtotal = 205.00,
            montoRecargo = 30.75,
            montoDescuento = 20.50,
            totalNeto = 215.25,
            mensajeWhatsapp = "Hola María, aquí tienes tu cotización de Dulce Punto:\n- 1x Torta de Chocolate (S/ 75.00)\n- 2x Cheesecake de Frutos (S/ 130.00)\nRecargo Express (15%): S/ 30.75\nDescuento Bienvenida (10%): -S/ 20.50\nTotal a pagar: S/ 215.25\nAceptamos Yape y Plin.",
            whatsappUrl = "https://wa.me/51987654321?text=Hola%20Mar%C3%ADa%2C%20aqu%C3%AD%20tienes%20tu%20cotizaci%C3%B3n%20por%20S%2F%20215.25",
            estado = "ENVIADA_WA",
            creadoEn = "Hoy · 10:15",
            detalles = listOf(
                QuoteDetailDto("prod-001", "Torta de Chocolate Artesanal", 1, 75.00, 75.00),
                QuoteDetailDto("prod-002", "Cheesecake de Frutos", 2, 65.00, 130.00)
            )
        ),
        QuoteResponse(
            cotizacionId = "cot-demo-0277",
            codigo = "COT-0277",
            nombreProspecto = "Carlos Mendoza",
            telefonoProspecto = "51999111222",
            esEntregaExpress = false,
            esClienteNuevo = false,
            subtotal = 123.00,
            montoRecargo = 0.00,
            montoDescuento = 0.00,
            totalNeto = 123.00,
            mensajeWhatsapp = "Hola Carlos, tu pedido está cotizado en S/ 123.00.",
            estado = "GANADA",
            creadoEn = "Ayer · 18:40",
            detalles = listOf(
                QuoteDetailDto("prod-001", "Torta de Chocolate Artesanal", 1, 75.00, 75.00),
                QuoteDetailDto("prod-002", "Caja de 12 Cupcakes Red Velvet", 1, 48.00, 48.00)
            )
        ),
        QuoteResponse(
            cotizacionId = "cot-demo-0276",
            codigo = "COT-0276",
            nombreProspecto = "Lucía Prado",
            telefonoProspecto = "51988222333",
            esEntregaExpress = false,
            esClienteNuevo = true,
            subtotal = 75.00,
            montoRecargo = 0.00,
            montoDescuento = 7.50,
            totalNeto = 67.50,
            mensajeWhatsapp = "Hola Lucía, tu cotización con 10% de bienvenida es S/ 67.50.",
            estado = "EMITIDA",
            creadoEn = "Ayer · 14:20",
            detalles = listOf(
                QuoteDetailDto("prod-001", "Torta de Chocolate Artesanal", 1, 75.00, 75.00)
            )
        )
    )

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
        val wonQuotes = quotes.filter { it.estado == "GANADA" }
        val totalQuotes = quotes.size
        val wonAmount = wonQuotes.sumOf { it.totalNeto }
        val convRate = if (totalQuotes > 0) ((wonQuotes.size.toDouble() / totalQuotes) * 1000.0).roundToInt() / 10.0 else 0.0

        return ApiResult.Success(
            DashboardResponse(
                negocioId = biz.negocioId,
                razonComercial = biz.razonComercial,
                rubroNombre = "Repostería y Pastelería Artesanal",
                distrito = biz.distrito,
                totalEscaneos = if (lastScan != null) 2 else 1,
                totalCotizaciones = totalQuotes,
                totalVentasGanadas = wonQuotes.size,
                montoVentasGanadas = wonAmount,
                tasaConversionPct = convRate,
                productosActivos = activeCount,
                friccionesDetectadas = findingsCount,
                ultimaAlertaAdaptativa = lastCampaign?.alertaAdaptativa
                    ?: "3 pastelerías cercanas tienen quejas por demora en entregas de fin de semana. Destaca tu servicio Express de 6 horas."
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

    // --- FASE 2: Mock de Campañas IA (CP3) ---
    suspend fun generateCampaign(request: CampaignGenerateRequest): ApiResult<CampaignResponse> {
        delay(1200)
        val frictionsText = request.fricciones.ifEmpty { listOf("Demora en entrega", "Sin stock") }.joinToString(", ")
        val bizName = currentBusiness?.razonComercial ?: "tu negocio"
        val campaign = CampaignResponse(
            campanaId = "camp-${UUID.randomUUID().toString().take(8)}",
            alertaAdaptativa = "Oportunidad basada en ($frictionsText): destaca tu preparación ágil y confirmación inmediata por WhatsApp para diferenciarte en tu distrito.",
            postRedes = "¿Buscas un postre fresco sin esperar horas ni sorpresas en el envío? En $bizName preparamos tu pedido con horario garantizado y precios transparentes. Escríbenos hoy por WhatsApp y aprovecha tu beneficio de bienvenida. (${request.objetivo} · Tono: ${request.tono})",
            guionReel15s = "0-3s (Gancho): ¿Te ha pasado que pides una torta y llega tarde?\n4-10s (Propuesta): En $bizName horneamos al día y te confirmamos hora exacta de entrega, incluso en modalidad Express.\n11-15s (CTA): Haz clic en el enlace de WhatsApp y recibe tu cotización en menos de 1 minuto.",
            estado = "BORRADOR",
            creadoEn = "Ahora"
        )
        lastCampaign = campaign
        return ApiResult.Success(campaign)
    }

    // --- FASE 2: Mock de Cotizaciones (CP4 y CP5) ---
    suspend fun createQuote(
        request: QuoteCreateRequest,
        catalogSnapshot: List<ProductDto>,
        rules: RulesConfigPayload = rulesConfig,
        businessName: String = currentBusiness?.razonComercial ?: "LeadLaunch"
    ): ApiResult<QuoteResponse> {
        delay(500)
        if (request.esEntregaExpress && !rules.politica.permiteExpress) {
            return ApiResult.Error(
                message = "Tu política comercial actual no permite entregas Express. Actívala en Reglas de precios.",
                code = "REGLA_NEGOCIO"
            )
        }

        val detalles = request.items.mapNotNull { item ->
            val prod = catalogSnapshot.find { it.productoId == item.productoId }
                ?: products.find { it.productoId == item.productoId }
            prod?.let {
                val lineTotal = round2(it.precioBase * item.cantidad)
                QuoteDetailDto(
                    productoId = it.productoId,
                    nombreProducto = it.nombre,
                    cantidad = item.cantidad,
                    precioUnitarioSnap = it.precioBase,
                    importeLinea = lineTotal
                )
            }
        }

        if (detalles.isEmpty()) {
            return ApiResult.Error("Selecciona al menos un producto válido.")
        }

        val subtotal = round2(detalles.sumOf { it.importeLinea })
        val expressPct = rules.reglas.find { it.tipoRegla == "RECARGO_EXPRESS" && it.activa }?.valorPorcentaje ?: 0.0
        val welcomePct = rules.reglas.find { it.tipoRegla == "DESC_BIENVENIDA" && it.activa }?.valorPorcentaje ?: 0.0

        val recargo = if (request.esEntregaExpress) round2(subtotal * expressPct / 100.0) else 0.0
        val descuento = if (request.esClienteNuevo) round2(subtotal * welcomePct / 100.0) else 0.0
        val totalNeto = round2(subtotal + recargo - descuento)

        val code = "COT-${(1000..9999).random()}"
        val linesText = detalles.joinToString("\n") {
            "• ${it.cantidad}x ${it.nombreProducto} (S/ ${String.format(Locale.US, "%.2f", it.importeLinea)})"
        }
        val msg = buildString {
            append("Hola ${request.nombreProspecto}, aquí tienes tu cotización *$code* de *$businessName*:\n\n")
            append(linesText)
            append("\n\nSubtotal: S/ ${String.format(Locale.US, "%.2f", subtotal)}")
            if (recargo > 0) append("\nRecargo Express (${expressPct.toInt()}%): +S/ ${String.format(Locale.US, "%.2f", recargo)}")
            if (descuento > 0) append("\nDescuento Bienvenida (${welcomePct.toInt()}%): -S/ ${String.format(Locale.US, "%.2f", descuento)}")
            append("\n*Total a pagar: S/ ${String.format(Locale.US, "%.2f", totalNeto)}*")
            if (rules.politica.aceptaYapePlin) append("\n\nAceptamos Yape y Plin. ¿Confirmamos tu pedido?")
        }

        val cleanPhone = request.telefonoProspecto?.filter { it.isDigit() }.orEmpty()
        val encodedMsg = Uri.encode(msg)
        val waUrl = if (cleanPhone.isNotBlank()) {
            "https://wa.me/$cleanPhone?text=$encodedMsg"
        } else {
            "https://wa.me/?text=$encodedMsg"
        }

        val created = QuoteResponse(
            cotizacionId = "cot-${UUID.randomUUID().toString().take(8)}",
            codigo = code,
            nombreProspecto = request.nombreProspecto,
            telefonoProspecto = request.telefonoProspecto,
            esEntregaExpress = request.esEntregaExpress,
            esClienteNuevo = request.esClienteNuevo,
            subtotal = subtotal,
            montoRecargo = recargo,
            montoDescuento = descuento,
            totalNeto = totalNeto,
            mensajeWhatsapp = msg,
            whatsappUrl = waUrl,
            estado = "EMITIDA",
            creadoEn = "Ahora",
            detalles = detalles
        )
        quotes.add(0, created)
        lastQuote = created
        return ApiResult.Success(created)
    }

    suspend fun getQuotes(): ApiResult<QuoteListResponse> {
        delay(250)
        return ApiResult.Success(QuoteListResponse(items = quotes.toList(), total = quotes.size))
    }

    suspend fun updateQuoteStatus(quoteId: String, newStatus: String): ApiResult<QuoteResponse> {
        delay(300)
        val idx = quotes.indexOfFirst { it.cotizacionId == quoteId }
        if (idx == -1) return ApiResult.Error("Cotización no encontrada.")
        val updated = quotes[idx].copy(estado = newStatus)
        quotes[idx] = updated
        if (lastQuote?.cotizacionId == quoteId) {
            lastQuote = updated
        }
        return ApiResult.Success(updated)
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}
