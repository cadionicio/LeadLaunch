package com.leadlaunch.ui.screens.s16_analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.remote.DashboardResponse
import com.leadlaunch.data.remote.QuoteResponse
import com.leadlaunch.data.repository.BusinessRepository
import com.leadlaunch.data.repository.QuoteRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.CardKPI
import com.leadlaunch.ui.components.EmptyState
import com.leadlaunch.ui.components.formatSoles
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AnalyticsScreen(
    businessRepository: BusinessRepository,
    quoteRepository: QuoteRepository,
    onNewQuote: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var dashboard by remember { mutableStateOf<DashboardResponse?>(null) }
    var quotes by remember { mutableStateOf<List<QuoteResponse>>(emptyList()) }
    var selectedPeriod by remember { mutableStateOf("Últimos 30 días") }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            errorMsg = null
            when (val d = businessRepository.getDashboard()) {
                is ApiResult.Success -> dashboard = d.data
                is ApiResult.Error -> errorMsg = d.message
            }
            when (val q = quoteRepository.getQuotes()) {
                is ApiResult.Success -> quotes = q.data.items
                is ApiResult.Error -> Unit
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Resultados y Conversión", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Últimos 7 días", "Últimos 30 días", "Últimos 90 días").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            when {
                loading -> {
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
                }
                errorMsg != null -> {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "Error al cargar analítica",
                        subtitle = errorMsg!!,
                        actionText = "Reintentar",
                        onAction = { load() }
                    )
                }
                else -> {
                    val totalQuotes = maxOf(dashboard?.totalCotizaciones ?: 0, quotes.size)
                    val wonCount = maxOf(dashboard?.totalVentasGanadas ?: 0, quotes.count { it.estado == "GANADA" })
                    val wonAmount = maxOf(
                        dashboard?.montoVentasGanadas ?: 0.0,
                        quotes.filter { it.estado == "GANADA" }.sumOf { it.totalNeto }
                    )
                    val conversionPct = if (totalQuotes > 0) (wonCount * 100.0 / totalQuotes) else 0.0
                    val avgTicket = if (wonCount > 0) (wonAmount / wonCount) else 0.0

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        CardKPI("Cotizaciones", "$totalQuotes", PrimaryBlue, Modifier.weight(1f))
                        CardKPI("Conversión", "${String.format("%.1f", conversionPct)}%", IndigoAccent, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        CardKPI("Ventas ganadas", formatSoles(wonAmount), SuccessGreen, Modifier.weight(1f))
                        CardKPI("Ticket promedio", formatSoles(avgTicket), PrimaryDark, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Embudo de cotizaciones ($selectedPeriod)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))

                    if (quotes.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            EmptyState(
                                icon = Icons.Outlined.BarChart,
                                title = "Sin ventas registradas aún",
                                subtitle = "Emite cotizaciones y actualiza su estado para visualizar tu tasa de cierre real.",
                                actionText = "Nueva cotización",
                                onAction = onNewQuote
                            )
                        }
                    } else {
                        val emitidas = quotes.count { it.estado == "EMITIDA" }
                        val enviadas = quotes.count { it.estado == "ENVIADA_WA" }
                        val ganadas = quotes.count { it.estado == "GANADA" }
                        val perdidas = quotes.count { it.estado == "PERDIDA" }
                        val maxCount = maxOf(quotes.size, 1)

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            border = BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                FunnelBar("Emitidas", emitidas, maxCount, PrimaryBlue)
                                FunnelBar("Enviadas por WhatsApp", enviadas, maxCount, IndigoAccent)
                                FunnelBar("Ventas Ganadas", ganadas, maxCount, SuccessGreen)
                                FunnelBar("Perdidas", perdidas, maxCount, ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FunnelBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val progress = (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text("$count", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = BorderColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}
