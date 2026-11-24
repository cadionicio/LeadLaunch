package com.leadlaunch.ui.screens.s01_splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leadlaunch.data.local.SessionStore
import com.leadlaunch.ui.theme.PrimaryBlue
import com.leadlaunch.ui.theme.SurfaceWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    sessionStore: SessionStore,
    onGoHome: () -> Unit,
    onGoBusiness: () -> Unit,
    onGoLogin: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(800)
        val session = sessionStore.getSession()
        when {
            !session.token.isNullOrBlank() && !session.businessId.isNullOrBlank() -> onGoHome()
            !session.token.isNullOrBlank() -> onGoBusiness()
            else -> onGoLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "LeadLaunch",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SurfaceWhite
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Radar comercial y copiloto de ventas",
                fontSize = 14.sp,
                color = SurfaceWhite.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(color = SurfaceWhite, strokeWidth = 2.5.dp)
        }
    }
}
