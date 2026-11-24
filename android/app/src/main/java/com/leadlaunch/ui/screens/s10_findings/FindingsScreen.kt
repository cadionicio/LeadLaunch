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
import com.leadlaunch.ui.theme.*

@Composable
fun FindingsScreen(
    radarRepository: RadarRepository,
    onBack: () -> Unit
) {
    val scan = radarRepository.getLastCachedScan()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Hallazgos del Radar", onBack = onBack) }
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
                // Decisión D12: aviso si es DATASET_FALLBACK
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
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
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
                        text = { Text("Fricciones (${scan.hallazgos.size})") }
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
                        items(scan.hallazgos, key = { it.hallazgoId }) { h ->
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
                                        Text(h.competidorNombre, style = MaterialTheme.typography.titleMedium)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                                            Text(" ${h.calificacion}.0", fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    FrictionChip(h.tipoFriccion)
                                    Spacer(Modifier.height(8.dp))
                                    Text(""${h.textoResena}"", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Oportunidad: ${h.resumenDolor}",
                                        style = MaterialTheme.typography.labelMedium,
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
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(c.nombre, style = MaterialTheme.typography.titleMedium)
                                        Text(c.distrito, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
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
