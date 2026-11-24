package com.leadlaunch.ui.screens.s07_product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.remote.ProductCreateRequest
import com.leadlaunch.data.repository.ProductRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.AppTextField
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.components.SwitchRule
import com.leadlaunch.ui.theme.BackgroundLight
import kotlinx.coroutines.launch

@Composable
fun CreateProductScreen(
    productRepository: ProductRepository,
    onProductCreated: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("10") }
    var prepHoursText by remember { mutableStateOf("6") }
    var permiteExpress by remember { mutableStateOf(true) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var skuError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var hoursError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Nuevo Producto", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            AppTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = "Nombre del producto",
                placeholder = "Ej. Torta Tres Leches",
                errorText = nameError
            )
            Spacer(Modifier.height(12.dp))

            AppTextField(
                value = sku,
                onValueChange = { sku = it.uppercase(); skuError = null },
                label = "SKU (código único)",
                placeholder = "Ej. TORT-3L-12",
                errorText = skuError
            )
            Spacer(Modifier.height(12.dp))

            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Descripción (opcional)",
                placeholder = "Porciones, sabor o presentación",
                singleLine = false
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = priceText,
                    onValueChange = { priceText = it; priceError = null },
                    label = "Precio base (S/)",
                    placeholder = "75.00",
                    errorText = priceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = stockText,
                    onValueChange = { stockText = it.filter { ch -> ch.isDigit() }; stockError = null },
                    label = "Stock disponible",
                    placeholder = "10",
                    errorText = stockError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))

            // Decisión D4: tiempo de preparación en horas
            AppTextField(
                value = prepHoursText,
                onValueChange = { prepHoursText = it.filter { ch -> ch.isDigit() }; hoursError = null },
                label = "Tiempo de preparación (en horas)",
                placeholder = "6",
                errorText = hoursError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(12.dp))

            SwitchRule(
                title = "Disponible para pedido Express",
                subtitle = "Aplica recargo express configurado en tus reglas",
                checked = permiteExpress,
                onCheckedChange = { permiteExpress = it }
            )

            Spacer(Modifier.height(22.dp))
            PrimaryButton(
                text = "Guardar producto",
                loading = loading,
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    val stock = stockText.toIntOrNull()
                    val hours = prepHoursText.toIntOrNull()

                    nameError = if (name.trim().length < 2) "Ingresa el nombre." else null
                    skuError = if (sku.trim().length < 2) "Ingresa un SKU." else null
                    priceError = if (price == null || price <= 0.0) "Debe ser mayor a 0." else null
                    stockError = if (stock == null || stock < 0) "Debe ser 0 o mayor." else null
                    hoursError = if (hours == null || hours < 1) "Mínimo 1 hora." else null

                    if (nameError == null && skuError == null && priceError == null && stockError == null && hoursError == null) {
                        scope.launch {
                            loading = true
                            val req = ProductCreateRequest(
                                sku = sku.trim(),
                                nombre = name.trim(),
                                descripcion = description.trim().ifBlank { null },
                                precioBase = price!!,
                                tiempoPrepHoras = hours!!,
                                stockDisponible = stock!!,
                                permiteExpress = permiteExpress
                            )
                            when (val res = productRepository.createProduct(req)) {
                                is ApiResult.Success -> onProductCreated()
                                is ApiResult.Error -> {
                                    if ("sku" in res.errorFields || res.code == "SKU_DUPLICADO") {
                                        skuError = res.message
                                    } else {
                                        snackbarHostState.showSnackbar(res.message)
                                    }
                                }
                            }
                            loading = false
                        }
                    }
                }
            )
        }
    }
}
