package com.leadlaunch.ui.screens.s17_profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leadlaunch.data.local.SessionStore
import com.leadlaunch.data.local.UserSession
import com.leadlaunch.data.repository.AuthRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.SwitchRule
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileSettingsScreen(
    sessionStore: SessionStore,
    authRepository: AuthRepository,
    onOpenRadar: () -> Unit,
    onOpenFindings: () -> Unit,
    onOpenRules: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var session by remember { mutableStateOf(UserSession()) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        session = sessionStore.getSession()
    }

    val displayName = session.userName?.takeIf { it.isNotBlank() } ?: "Emprendedor"
    val displayBusiness = session.businessName?.takeIf { it.isNotBlank() } ?: "Mi negocio LeadLaunch"
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "L"

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Mi Perfil") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera de cuenta
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PrimarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(displayBusiness, style = MaterialTheme.typography.bodyMedium, color = PrimaryDark)
                        session.userEmail?.let {
                            Text(it, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        }
                    }
                }
            }

            Text("Herramientas comerciales", style = MaterialTheme.typography.labelMedium, color = TextSecondary)

            SettingsRow(
                icon = Icons.Outlined.Radar,
                title = "Radar de competencia",
                subtitle = "Escanea negocios cercanos y detecta fricciones",
                onClick = onOpenRadar
            )
            SettingsRow(
                icon = Icons.Outlined.Insights,
                title = "Hallazgos del radar",
                subtitle = "Revisa quejas clasificadas de tu último escaneo",
                onClick = onOpenFindings
            )
            SettingsRow(
                icon = Icons.Outlined.Tune,
                title = "Reglas de precios",
                subtitle = "Recargo express, descuento bienvenida y Yape/Plin",
                onClick = onOpenRules
            )
            SettingsRow(
                icon = Icons.Outlined.BarChart,
                title = "Resultados y conversión",
                subtitle = "Ventas ganadas, tasa de cierre y ticket promedio",
                onClick = onOpenAnalytics
            )

            Text("Preferencias", style = MaterialTheme.typography.labelMedium, color = TextSecondary)

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SwitchRule(
                        title = "Alertas y recordatorios",
                        subtitle = "Recibe avisos de oportunidades y seguimiento",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            }

            SettingsRow(
                icon = Icons.Outlined.HelpOutline,
                title = "Ayuda y soporte",
                subtitle = "Información de LeadLaunch MVP v1.0",
                onClick = { showHelpDialog = true }
            )

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = "Cerrar sesión", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("LeadLaunch v1.0") },
            text = {
                Text("Radar comercial y copiloto adaptativo para emprendimientos gastronómicos y de repostería. Los copies generados con IA son sugerencias y no se publican automáticamente.")
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Entendido", color = PrimaryBlue)
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Se limpiará tu sesión en este dispositivo y volverás a la pantalla de inicio de sesión.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            authRepository.logout()
                            onLogout()
                        }
                    }
                ) {
                    Text("Cerrar sesión", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Ir a $title", tint = TextSecondary)
        }
    }
}
