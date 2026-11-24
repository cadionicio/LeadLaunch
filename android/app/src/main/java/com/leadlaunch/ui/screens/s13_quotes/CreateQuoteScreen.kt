package com.leadlaunch.ui.screens.s13_quotes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.*
import com.leadlaunch.data.repository.ProductRepository
import com.leadlaunch.data.repository.QuoteRepository
import com.leadlaunch.data.repository.RulesRepository
import com.leadlaunch.ui.components.*
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuoteScreen(
    productRepository: ProductRepository,
    rulesRepository: RulesRepository,
    quoteRepository: QuoteRepository,
    onQuoteCreated: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var products by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
    var rulesSnapshot by remember { mutableStateOf<RulesConfigPayload?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Mapa productoId -> cantidad
    var quantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Paso de datos del prospecto (ModalBottomSheet)
    var showProspectSheet by remember { mutableStateOf(false) }
    var prospectName by remember { mutableStateOf("") }
    var prospectPhone by remember { mutableStateOf("51") }
    var isExpress by remember { mutableStateOf(false) }
    var isNewClient by remember { mutableStateOf(true) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            loading = true
            errorMsg = null
            when (val res = productRepository.getProducts()) {
                is ApiResult.Success -> products = res.data.items
                is ApiResult.Error -> errorMsg = res.message
            }
            when (val r = rulesRepository.getRules()) {
                is ApiResult.Success -> rulesSnapshot = r.data
                is ApiResult.Error -> Unit
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    val filteredProducts = remember(products, searchQuery) {
        products.filter {
            searchQuery.isBlank() ||
                it.nombre.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalUnits = quantities.values.sum()
    val estimatedSubtotal = remember(products, quantities) {
        products.sumOf { p -> (quantities[p.productoId] ?: 0) * p.precioBase }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Nueva Cotización", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!loading && errorMsg == null && products.isNotEmpty()) {
                Surface(
                    color = SurfaceWhite,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$totalUnits unidad(es) seleccionada(s)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Subtotal: ${formatSoles(estimatedSubtotal)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        PrimaryButton(
                            text = "Continuar con datos del cliente",
                            enabled = totalUnits > 0,
                            onClick = { showProspectSheet = true }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SimulatedDataBanner(Modifier.padding(bottom = 12.dp))
            Text(
                text = "Selecciona los productos y cantidades para armar el pedido.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar producto o SKU") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                errorMsg != null -> {
                    EmptyState(
                        icon = Icons.Outlined.Inventory2,
                        title = "Error al cargar productos",
                        subtitle = errorMsg!!,
                        actionText = "Reintentar",
                        onAction = { loadData() }
                    )
                }
                filteredProducts.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.Inventory2,
                        title = "Sin productos disponibles",
                        subtitle = "Agrega productos en tu catálogo para poder cotizar."
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredProducts, key = { it.productoId }) { product ->
                            QuoteLine(
                                product = product,
                                quantity = quantities[product.productoId] ?: 0,
                                onQuantityChange = { newQty ->
                                    quantities = if (newQty <= 0) {
                                        quantities - product.productoId
                                    } else {
                                        quantities + (product.productoId to newQty)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showProspectSheet) {
        ModalBottomSheet(
            onDismissRequest = { if (!submitting) showProspectSheet = false },
            containerColor = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text("Datos del prospecto", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Los recargos y descuentos se calcularán según tus reglas vigentes.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))

                AppTextField(
                    value = prospectName,
                    onValueChange = { prospectName = it; nameError = null },
                    label = "Nombre del cliente *",
                    placeholder = "Ej. María Torres",
                    errorText = nameError
                )
                Spacer(Modifier.height(12.dp))

                AppTextField(
                    value = prospectPhone,
                    onValueChange = { input ->
                        prospectPhone = input.filter { it.isDigit() }
                        phoneError = null
                    },
                    label = "WhatsApp del cliente (solo dígitos, opcional)",
                    placeholder = "51987654321",
                    errorText = phoneError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(12.dp))

                SwitchRule(
                    title = "Entrega Express",
                    subtitle = "Aplica recargo express configurado en tus reglas",
                    checked = isExpress,
                    onCheckedChange = { isExpress = it }
                )
                SwitchRule(
                    title = "Cliente nuevo",
                    subtitle = "Aplica descuento de bienvenida si está activo",
                    checked = isNewClient,
                    onCheckedChange = { isNewClient = it }
                )

                Spacer(Modifier.height(18.dp))
                PrimaryButton(
                    text = "Calcular y ver vista previa",
                    loading = submitting,
                    onClick = {
                        val cleanPhone = prospectPhone.trim()
                        nameError = if (prospectName.trim().length < 2) "Ingresa el nombre del cliente." else null
                        phoneError = if (cleanPhone.isNotBlank() && cleanPhone != "51" && cleanPhone.length !in 9..15) {
                            "Ingresa entre 9 y 15 dígitos (o déjalo vacío)."
                        } else null

                        if (nameError == null && phoneError == null) {
                            scope.launch {
                                submitting = true
                                val itemsReq = quantities.entries
                                    .filter { it.value > 0 }
                                    .map { QuoteItemRequest(it.key, it.value) }
                                val req = QuoteCreateRequest(
                                    nombreProspecto = prospectName.trim(),
                                    telefonoProspecto = cleanPhone.takeIf { it.length >= 9 },
                                    esEntregaExpress = isExpress,
                                    esClienteNuevo = isNewClient,
                                    items = itemsReq
                                )
                                when (val res = quoteRepository.createQuote(req, products, rulesSnapshot)) {
                                    is ApiResult.Success -> {
                                        showProspectSheet = false
                                        onQuoteCreated()
                                    }
                                    is ApiResult.Error -> {
                                        showProspectSheet = false
                                        snackbarHostState.showSnackbar(res.message)
                                    }
                                }
                                submitting = false
                            }
                        }
                    }
                )
            }
        }
    }
}
