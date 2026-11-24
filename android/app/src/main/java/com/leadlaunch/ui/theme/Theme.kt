package com.leadlaunch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LeadLaunchColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimarySoft,
    onPrimaryContainer = PrimaryDark,
    secondary = IndigoAccent,
    onSecondary = SurfaceWhite,
    secondaryContainer = IndigoSoft,
    onSecondaryContainer = IndigoAccent,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = ErrorRed,
    onError = SurfaceWhite,
    errorContainer = ErrorSoft,
    onErrorContainer = ErrorRed
)

@Composable
fun LeadLaunchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LeadLaunchColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
