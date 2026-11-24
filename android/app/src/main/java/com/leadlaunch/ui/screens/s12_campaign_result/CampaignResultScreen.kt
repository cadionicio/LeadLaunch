package com.leadlaunch.ui.screens.s12_campaign_result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.repository.CampaignRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.SimulatedDataBanner
import com.leadlaunch.ui.components.EmptyState
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CampaignResultScreen(
    campaignRepository: CampaignRepository,
    onCreateQuote: () -> Unit,
    onBack: () -> Unit
) {
    val campaign = campaignRepository.getLastCampaign()
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Campaña generada", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (campaign == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Outlined.Campaign,
                    title = "Sin campaña generada",
                    subtitle = "Vuelve atrás y genera una campaña basada en tus hallazgos.",
                    actionText = "Volver",
                    onAction = onBack
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SimulatedDataBanner(Modifier.padding(bottom = 12.dp))
                Surface(
                    color = SuccessSoft,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Campaña lista",
                            tint = SuccessGreen
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Campaña adaptativa lista · Revisa antes de publicar",
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Bloque 1: Alerta adaptativa
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Alerta de ajuste de oferta",
                            color = IndigoAccent,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(campaign.alertaAdaptativa, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // Bloque 2: Post para redes sociales
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "2. Post para redes sociales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(campaign.postRedes, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(campaign.postRedes))
                                scope.launch { snackbarHostState.showSnackbar("Post copiado al portapapeles") }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar post", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Copiar texto del post")
                        }
                    }
                }

                // Bloque 3: Guion de 15s
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "3. Guion para Reel / TikTok (15 s)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(campaign.guionReel15s, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(campaign.guionReel15s))
                                scope.launch { snackbarHostState.showSnackbar("Guion copiado al portapapeles") }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar guion", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Copiar guion")
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                PrimaryButton(
                    text = "Crear cotización para un interesado",
                    onClick = onCreateQuote
                )
            }
        }
    }
}
