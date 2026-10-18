package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BloodRedPrimary,
    onPrimary = SurfaceCardLight,
    primaryContainer = BloodRedContainer,
    onPrimaryContainer = BloodOnRedContainer,
    secondary = EmergencyAmber,
    onSecondary = SurfaceCardLight,
    secondaryContainer = EmergencyAmberLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimary,
    background = SurfaceBgLight,
    onBackground = TextPrimary,
    outline = OutlineBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = BloodRedLight,
    onPrimary = BloodOnRedContainer,
    primaryContainer = BloodRedDark,
    onPrimaryContainer = BloodRedContainer,
    secondary = EmergencyAmber,
    onSecondary = SurfaceCardLight,
    surface = TextPrimary,
    onSurface = SurfaceBgLight,
    background = TextPrimary,
    onBackground = SurfaceBgLight
)

@Composable
fun BloodCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
