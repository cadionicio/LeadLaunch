package com.leadlaunch.ui.screens.phase2

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.repository.AuthRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.EmptyState
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.theme.BackgroundLight
import com.leadlaunch.ui.theme.ErrorRed
import kotlinx.coroutines.launch

@Composable
fun QuotesPlaceholderScreen() {
    Scaffold(containerColor = BackgroundLight, topBar = { AppBar(title = "Cotizaciones (S15)") }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            EmptyState(
                icon = Icons.Outlined.ReceiptLong,
                title = "Cotizador y WhatsApp (Fase 2)",
                subtitle = "Aquí se listará el historial de cotizaciones (S15) y el envío rápido por WhatsApp (S13–S14)."
            )
        }
    }
}

@Composable
fun CampaignsPlaceholderScreen() {
    Scaffold(containerColor = BackgroundLight, topBar = { AppBar(title = "Campañas IA (S11)") }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            EmptyState(
                icon = Icons.Outlined.Campaign,
                title = "Campañas Reactivas IA (Fase 2)",
                subtitle = "Aquí generarás posts publicitarios y guiones de video corto basados en tus hallazgos del Radar (S11–S12)."
            )
        }
    }
}

@Composable
fun MoreScreen(
    authRepository: AuthRepository,
    onOpenRadar: () -> Unit,
    onOpenRules: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Scaffold(containerColor = BackgroundLight, topBar = { AppBar(title = "Más opciones") }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(text = "Radar de competencia (S09)", onClick = onOpenRadar)
            PrimaryButton(text = "Reglas de precios (S08)", onClick = onOpenRules)
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        authRepository.logout()
                        onLogout()
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
