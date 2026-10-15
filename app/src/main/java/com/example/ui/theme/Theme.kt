package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = NavyDark,
    onPrimaryContainer = BlueSurface,
    secondary = OrangeWarning,
    onSecondary = White,
    tertiary = GreenSuccess,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = White,
    onSurface = White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = NavyDark,
    onPrimaryContainer = White,
    secondary = OrangeWarning,
    onSecondary = White,
    tertiary = GreenSuccess,
    background = GreyLight,
    surface = White,
    onBackground = NavyDark,
    onSurface = NavyDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = GreyText,
    outline = GreyBorder
)

@Composable
fun SPMBTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
