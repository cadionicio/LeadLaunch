package com.leadlaunch.ui.screens.s08_rules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.remote.PolicyPayloadDto
import com.leadlaunch.data.remote.RuleItemDto
import com.leadlaunch.data.remote.RulesConfigPayload
import com.leadlaunch.data.repository.RulesRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.AppTextField
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.components.SwitchRule
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RulesScreen(
    rulesRepository: RulesRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    var permiteExpress by remember { mutableStateOf(true) }
    var recargoExpressText by remember { mutableStateOf("15") }
    var permiteDescBienvenida by remember { mutableStateOf(true) }
    var descBienvenidaText by remember { mutableStateOf("10") }
    var aceptaYapePlin by remember { mutableStateOf(true) }
    var horarioAtencion by remember { mutableStateOf("09:00 - 20:00") }
    var permiteAcumularDesc by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        when (val res = rulesRepository.getRules()) {
            is ApiResult.Success -> {
                permiteExpress = res.data.politica.permiteExpress
                aceptaYapePlin = res.data.politica.aceptaYapePlin
                horarioAtencion = res.data.politica.horarioAtencion
                permiteAcumularDesc = res.data.politica.permiteAcumularDesc

                val expressRule = res.data.reglas.find { it.tipoRegla == "RECARGO_EXPRESS" }
                val welcomeRule = res.data.reglas.find { it.tipoRegla == "DESC_BIENVENIDA" }

                recargoExpressText = (expressRule?.valorPorcentaje ?: 15.0).toInt().toString()
                permiteDescBienvenida = welcomeRule?.activa ?: true
                descBienvenidaText = (welcomeRule?.valorPorcentaje ?: 10.0).toInt().toString()
            }
            is ApiResult.Error -> snackbarHostState.showSnackbar(res.message)
        }
        loading = false
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Reglas de Precios", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SwitchRule(
                            title = "Recargo por entrega Express",
                            subtitle = "Se aplica a pedidos urgentes del mismo día",
                            checked = permiteExpress,
                            onCheckedChange = { permiteExpress = it }
                        )
                        if (permiteExpress) {
                            Spacer(Modifier.height(8.dp))
                            AppTextField(
                                value = recargoExpressText,
                                onValueChange = { recargoExpressText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = "Porcentaje de recargo (%)",
                                placeholder = "15",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SwitchRule(
                            title = "Descuento de bienvenida",
                            subtitle = "Incentivo automático para clientes nuevos",
                            checked = permiteDescBienvenida,
                            onCheckedChange = { permiteDescBienvenida = it }
                        )
                        if (permiteDescBienvenida) {
                            Spacer(Modifier.height(8.dp))
                            AppTextField(
                                value = descBienvenidaText,
                                onValueChange = { descBienvenidaText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = "Porcentaje de descuento (%)",
                                placeholder = "10",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                // Decisión D8: check Yape/Plin sin tarifas
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = aceptaYapePlin,
                            onCheckedChange = { aceptaYapePlin = it },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Aceptar Yape / Plin", style = MaterialTheme.typography.titleMedium)
                            Text("Se incluirá como medio de pago en tus cotizaciones", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Guardar reglas",
                    loading = saving,
                    onClick = {
                        scope.launch {
                            saving = true
                            val payload = RulesConfigPayload(
                                politica = PolicyPayloadDto(
                                    permiteExpress = permiteExpress,
                                    permiteAcumularDesc = permiteAcumularDesc,
                                    aceptaYapePlin = aceptaYapePlin,
                                    horarioAtencion = horarioAtencion
                                ),
                                reglas = listOf(
                                    RuleItemDto(
                                        tipoRegla = "RECARGO_EXPRESS",
                                        valorPorcentaje = recargoExpressText.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0,
                                        activa = permiteExpress
                                    ),
                                    RuleItemDto(
                                        tipoRegla = "DESC_BIENVENIDA",
                                        valorPorcentaje = descBienvenidaText.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0,
                                        activa = permiteDescBienvenida
                                    )
                                )
                            )
                            when (val res = rulesRepository.updateRules(payload)) {
                                is ApiResult.Success -> snackbarHostState.showSnackbar("Reglas actualizadas")
                                is ApiResult.Error -> snackbarHostState.showSnackbar(res.message)
                            }
                            saving = false
                        }
                    }
                )
            }
        }
    }
}