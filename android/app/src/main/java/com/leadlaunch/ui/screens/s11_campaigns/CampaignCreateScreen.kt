package com.leadlaunch.ui.screens.s11_campaigns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.remote.CampaignGenerateRequest
import com.leadlaunch.data.repository.CampaignRepository
import com.leadlaunch.data.repository.RadarRepository
import com.leadlaunch.ui.components.*
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

private val TONES = listOf("Cercano y confiable", "Urgente / Express", "Profesional y claro")
private val CHANNELS = listOf(
    "Redes sociales (Instagram, Facebook, TikTok)",
    "Estados de WhatsApp",
    "Instagram Reels y TikTok"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CampaignCreateScreen(
    radarRepository: RadarRepository,
    campaignRepository: CampaignRepository,
    onCampaignGenerated: () -> Unit,
    onOpenRadar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val lastScan = radarRepository.getLastCachedScan()
    val initialFrictions = remember(lastScan) {
        val fromScan = lastScan?.resumenFricciones?.map { it.tipoNombre }?.distinct().orEmpty()
        if (fromScan.isNotEmpty()) fromScan
        else lastScan?.competidores?.flatMap { it.fricciones }?.map { it.tipoNombre }?.distinct().orEmpty()
    }

    var selectedFrictions by remember(initialFrictions) { mutableStateOf(initialFrictions) }
    var objetivo by remember { mutableStateOf("Atraer más pedidos") }
    var selectedChannel by remember { mutableStateOf(CHANNELS.first()) }
    var selectedTone by remember { mutableStateOf(TONES.first()) }
    var generating by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Campañas con IA") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            SimulatedDataBanner(Modifier.padding(bottom = 12.dp))
            Text(
                text = "Convierte las debilidades de tu competencia en una campaña lista para publicar.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            if (selectedFrictions.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EmptyState(
                        icon = Icons.Outlined.Radar,
                        title = "Sin hallazgos seleccionados",
                        subtitle = "Ejecuta un escaneo en el Radar local para detectar fricciones reales de tu zona.",
                        actionText = "Ir al Radar",
                        onAction = onOpenRadar
                    )
                }
            } else {
                Text("Fricciones detectadas a atacar", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedFrictions.forEach { friction ->
                        RemovableFrictionChip(
                            text = friction,
                            onRemove = { selectedFrictions = selectedFrictions - friction }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                AppTextField(
                    value = objetivo,
                    onValueChange = { objetivo = it },
                    label = "Objetivo de la campaña",
                    placeholder = "Ej. Atraer más pedidos para el fin de semana"
                )
                Spacer(Modifier.height(14.dp))

                Text("Canal principal", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CHANNELS.forEach { channel ->
                        val isSelected = selectedChannel == channel
                        Surface(
                            onClick = { selectedChannel = channel },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimarySoft else SurfaceWhite,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else BorderColor),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedChannel = channel },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(channel, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))

                Text("Tono de comunicación", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TONES.forEach { tone ->
                        FilterChip(
                            selected = selectedTone == tone,
                            onClick = { selectedTone = tone },
                            label = { Text(tone) }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                if (generating) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = IndigoSoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = IndigoAccent,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Generando campaña con IA...",
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoAccent
                                )
                                Text(
                                    "Cruzando tus productos con las quejas de tu zona.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (!generating) {
                            scope.launch {
                                generating = true
                                val req = CampaignGenerateRequest(
                                    escaneoId = lastScan?.escaneoId,
                                    fricciones = selectedFrictions,
                                    objetivo = objetivo.trim().ifBlank { "Atraer más pedidos" },
                                    canal = selectedChannel,
                                    tono = selectedTone
                                )
                                when (val res = campaignRepository.generateCampaign(req)) {
                                    is ApiResult.Success -> onCampaignGenerated()
                                    is ApiResult.Error -> {
                                        val action = snackbarHostState.showSnackbar(
                                            message = res.message,
                                            actionLabel = "Reintentar"
                                        )
                                        if (action == SnackbarResult.ActionPerformed) {
                                            generating = false
                                            return@launch
                                        }
                                    }
                                }
                                generating = false
                            }
                        }
                    },
                    enabled = !generating && selectedFrictions.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = SurfaceWhite)
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (generating) "Generando..." else "Generar campaña con IA",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
