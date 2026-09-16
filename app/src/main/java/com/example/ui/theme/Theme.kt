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
    primary = ClayCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = ClayCyanContainer,
    secondary = ClayLavender,
    onSecondary = Color.White,
    background = ClayBackgroundDark,
    surface = ClayCardDark,
    surfaceVariant = Color(0xFF242C42),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = ClayCoral
)

private val LightColorScheme = lightColorScheme(
    primary = ClayBlue,
    onPrimary = Color.White,
    primaryContainer = ClayBlueContainer,
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = ClayCyan,
    onSecondary = Color.White,
    background = ClayBackgroundLight,
    surface = ClayCardLight,
    surfaceVariant = Color(0xFFE2E8F0),
    onBackground = ClayTextPrimary,
    onSurface = ClayTextPrimary,
    onSurfaceVariant = ClayTextSecondary,
    outline = Color(0xFFCBD5E1),
    error = ClayCoral
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional newsroom palette by default
    content: @Composable () -> Unit,
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
