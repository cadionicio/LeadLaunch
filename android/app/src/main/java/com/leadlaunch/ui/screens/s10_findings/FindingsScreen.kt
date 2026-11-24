package com.leadlaunch.ui.screens.s10_findings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.repository.RadarRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.EmptyState
import com.leadlaunch.ui.components.FrictionChip
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.theme.*

@Composable
fun FindingsScreen(
    radarRepository: RadarRepository,
    onCreateCampaign: () -> Unit,
    onBack: () -> Unit
) {
    val scan = radarRepository.getLastCachedScan()
    var selectedTab by remember { mutableIntStateOf(0) }

    val allFindings = remember(scan) {
        scan?.competidores?.flatMap { comp ->
            comp.fricciones.map { friction ->
                Pair(comp.nombre, friction)
            }
        } ?: emptyList()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Hallazgos del Radar", onBack = onBack) },
        bottomBar = {
            if (scan != null && allFindings.isNotEmpty()) {
                Surface(color = SurfaceWhite, shadowElevation = 8.dp) {
                    Box(Modifier.fillMaxWidth().padding(16.dp)) {
                        PrimaryButton(
                            text = "Crear campaña IA con estos hallazgos",
                            onClick = onCreateCampaign
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
        ) {
            if (scan == null) {
                EmptyState(
                    icon = Icons.Outlined.Radar,
                    title = "Sin escaneos recientes",
                    subtitle = "Vuelve al Radar y ejecuta un escaneo para ver hallazgos."
                )
            } else {
                if (scan.fuenteDatos == "DATASET_FALLBACK") {
                    Surface(
                        color = WarningSoft,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Datos de referencia (demo)",
                                color = WarningAmber,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceWhite,
                    contentColor = PrimaryBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Fricciones (${allFindings.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Competidores (${scan.competidores.size})") }
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == 0) {
                        items(allFindings) { (competitorName, friction) ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(competitorName, style = MaterialTheme.typography.titleMedium)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = WarningAmber,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = " ${friction.calificacion}.0",
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    FrictionChip(friction.tipoNombre)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Oportunidad: ${friction.resumenDolor}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PrimaryDark
                                    )
                                }
                            }
                        }
                    } else {
                        items(scan.competidores, key = { it.competidorId }) { c ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(c.nombre, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${c.distrito} · ${c.fricciones.size} fricción(es)",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = WarningAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(" ${c.calificacionProm}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
