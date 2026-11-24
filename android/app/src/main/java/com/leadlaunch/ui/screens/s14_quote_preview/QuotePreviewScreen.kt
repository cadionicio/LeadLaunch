package com.leadlaunch.ui.screens.s14_quote_preview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.repository.QuoteRepository
import com.leadlaunch.ui.components.*
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun QuotePreviewScreen(
    quoteRepository: QuoteRepository,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var quote by remember { mutableStateOf(quoteRepository.getLastQuote()) }
    var showConfirmSentDialog by remember { mutableStateOf(false) }
    var updatingStatus by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            AppBar(
                title = quote?.let { "Cotización ${it.codigo ?: it.cotizacionId.take(8).uppercase()}" } ?: "Vista previa",
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val current = quote
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No hay cotización activa",
                    subtitle = "Crea una nueva cotización desde tu catálogo.",
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
                // Estado y cliente
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(current.nombreProspecto, style = MaterialTheme.typography.titleMedium)
                            QuoteStatusBadge(current.estado)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = current.telefonoProspecto?.let { "WhatsApp: +$it" } ?: "Sin número registrado",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (current.esEntregaExpress) {
                                StatusBadge("Entrega Express", IndigoSoft, IndigoAccent)
                            }
                            if (current.esClienteNuevo) {
                                StatusBadge("Cliente nuevo", SuccessSoft, SuccessGreen)
                            }
                        }
                    }
                }

                // Desglose de productos
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Detalle del pedido", style = MaterialTheme.typography.titleMedium)
                        current.detalles.forEach { d ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${d.cantidad}x ${d.nombreProducto}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = formatSoles(d.importeLinea),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 4.dp))
                        SummaryRow("Subtotal", formatSoles(current.subtotal))
                        if (current.montoRecargo > 0) {
                            SummaryRow("Recargo Express", "+${formatSoles(current.montoRecargo)}", IndigoAccent)
                        }
                        if (current.montoDescuento > 0) {
                            SummaryRow("Descuento Bienvenida", "-${formatSoles(current.montoDescuento)}", SuccessGreen)
                        }
                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total neto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = formatSoles(current.totalNeto),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark
                            )
                        }
                    }
                }

                // Vista previa del mensaje
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimarySoft.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Mensaje listo para enviar", style = MaterialTheme.typography.labelMedium, color = PrimaryDark)
                        Spacer(Modifier.height(6.dp))
                        Text(current.mensajeWhatsapp, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }

                // CTA WhatsApp (Decisión CP4: https://wa.me/... + manejo de WhatsApp no instalado)
                WhatsAppButton(
                    text = "Enviar por WhatsApp",
                    onClick = {
                        val url = current.whatsappUrl ?: buildFallbackWaUrl(current.telefonoProspecto, current.mensajeWhatsapp)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        try {
                            context.startActivity(intent)
                            if (current.estado == "EMITIDA") {
                                showConfirmSentDialog = true
                            }
                        } catch (_: ActivityNotFoundException) {
                            scope.launch {
                                snackbarHostState.showSnackbar("WhatsApp no está instalado. Puedes copiar el mensaje abajo.")
                            }
                        }
                    }
                )

                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(current.mensajeWhatsapp))
                        scope.launch { snackbarHostState.showSnackbar("Mensaje copiado al portapapeles") }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar mensaje", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Copiar mensaje")
                }

                TextButton(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text("Volver al historial de cotizaciones", color = PrimaryBlue)
                }
            }
        }
    }

    // Confirmación explícita para pasar a ENVIADA_WA (P08)
    if (showConfirmSentDialog && quote != null) {
        AlertDialog(
            onDismissRequest = { if (!updatingStatus) showConfirmSentDialog = false },
            title = { Text("¿Enviaste la cotización?") },
            text = { Text("Confirma si lograste enviar el mensaje por WhatsApp para actualizar su estado a Enviada.") },
            confirmButton = {
                TextButton(
                    enabled = !updatingStatus,
                    onClick = {
                        scope.launch {
                            updatingStatus = true
                            when (val res = quoteRepository.updateQuoteStatus(quote!!.cotizacionId, "ENVIADA_WA")) {
                                is ApiResult.Success -> {
                                    quote = res.data
                                    snackbarHostState.showSnackbar("Estado actualizado a Enviada")
                                }
                                is ApiResult.Error -> snackbarHostState.showSnackbar(res.message)
                            }
                            updatingStatus = false
                            showConfirmSentDialog = false
                        }
                    }
                ) {
                    Text("Sí, marcar como Enviada", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !updatingStatus,
                    onClick = { showConfirmSentDialog = false }
                ) {
                    Text("Aún no")
                }
            }
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

private fun buildFallbackWaUrl(phone: String?, message: String): String {
    val cleanPhone = phone?.filter { it.isDigit() }.orEmpty()
    val encoded = Uri.encode(message)
    return if (cleanPhone.isNotBlank()) "https://wa.me/$cleanPhone?text=$encoded"
    else "https://wa.me/?text=$encoded"
}
