package com.leadlaunch.ui.screens.s05_home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.local.SessionStore
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.remote.DashboardResponse
import com.leadlaunch.data.repository.BusinessRepository
import com.leadlaunch.ui.components.CardKPI
import com.leadlaunch.ui.components.EmptyState
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.components.formatSoles
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    sessionStore: SessionStore,
    businessRepository: BusinessRepository,
    onOpenRadar: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenCatalog: () -> Unit,
    onOpenAnalytics: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("Emprendedor") }
    var dashboard by remember { mutableStateOf<DashboardResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            errorMsg = null
            userName = sessionStore.getSession().userName?.split(" ")?.firstOrNull() ?: "Emprendedor"
            when (val res = businessRepository.getDashboard()) {
                is ApiResult.Success -> dashboard = res.data
                is ApiResult.Error -> errorMsg = res.message
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Hola, $userName", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = dashboard?.let { "${it.razonComercial} · ${it.distrito}" } ?: "Resumen de tu negocio",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(20.dp))

        if (loading) {
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.weight(1f).height(88.dp).clip(RoundedCornerShape(14.dp))
                            .background(BorderColor.copy(alpha = 0.6f))
                    )
                    Box(
                        Modifier.weight(1f).height(88.dp).clip(RoundedCornerShape(14.dp))
                            .background(BorderColor.copy(alpha = 0.6f))
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        } else if (errorMsg != null) {
            EmptyState(
                icon = Icons.Outlined.CloudOff,
                title = "No pudimos cargar el resumen",
                subtitle = errorMsg!!,
                actionText = "Reintentar",
                onAction = { load() }
            )
        } else {
            val d = dashboard!!
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CardKPI("Productos activos", "${d.productosActivos}", PrimaryBlue, Modifier.weight(1f))
                CardKPI("Fricciones locales", "${d.friccionesDetectadas}", ErrorRed, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CardKPI("Conversión", "${d.tasaConversionPct}%", IndigoAccent, Modifier.weight(1f))
                CardKPI("Ventas ganadas", formatSoles(d.montoVentasGanadas), SuccessGreen, Modifier.weight(1f))
            }

            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = onOpenAnalytics,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("Ver detalle de resultados y conversión →", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))
            Text("Última recomendación adaptativa", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (d.ultimaAlertaAdaptativa.isNullOrBlank()) {
                EmptyState(
                    icon = Icons.Outlined.Radar,
                    title = "Aún no tienes alertas",
                    subtitle = "Escanea tu zona para detectar oportunidades frente a tu competencia.",
                    actionText = "Abrir Radar",
                    onAction = onOpenRadar
                )
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Oportunidad detectada", color = IndigoAccent, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(d.ultimaAlertaAdaptativa, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            PrimaryButton(text = "Escanear competencia (Radar)", onClick = onOpenRadar)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onOpenRules,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) {
                    Text("Reglas de precios")
                }
                OutlinedButton(
                    onClick = onOpenCatalog,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) {
                    Text("Ver catálogo")
                }
            }
        }
    }
}
