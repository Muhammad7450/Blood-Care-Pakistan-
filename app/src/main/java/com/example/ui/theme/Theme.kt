package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF8C1D18),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = DarkSecondary,
    onSecondary = Color.Black,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color(0xFFF0E0E0),
    onSurface = Color(0xFFF0E0E0)
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonRedPrimary,
    onPrimary = Color.White,
    primaryContainer = SoftRedContainer,
    onPrimaryContainer = OnSoftRedContainer,
    secondary = MedicalRedSecondary,
    onSecondary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun BloodCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our medical red theme explicitly
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
