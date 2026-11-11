package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = JihadGreenAccent,
    onPrimary = JihadBackgroundDark,
    primaryContainer = JihadGreenDark,
    onPrimaryContainer = Color.White,
    secondary = JihadGreenLight,
    onSecondary = Color.White,
    background = JihadBackgroundDark,
    onBackground = JihadTextPrimaryDark,
    surface = JihadSurfaceDark,
    onSurface = JihadTextPrimaryDark,
    surfaceVariant = JihadSurfaceVariantDark,
    onSurfaceVariant = JihadTextSecondaryDark,
    outline = Color(0xFF2C3E36),
    error = JihadErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = JihadGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = JihadGreenMint,
    onPrimaryContainer = JihadGreenDark,
    secondary = JihadGreenLight,
    onSecondary = Color.White,
    background = JihadBackgroundLight,
    onBackground = JihadTextPrimary,
    surface = JihadSurfaceLight,
    onSurface = JihadTextPrimary,
    surfaceVariant = JihadSurfaceVariant,
    onSurfaceVariant = JihadTextSecondary,
    outline = JihadDivider,
    error = JihadErrorRed
)

@Composable
fun JihadChatTheme(
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
