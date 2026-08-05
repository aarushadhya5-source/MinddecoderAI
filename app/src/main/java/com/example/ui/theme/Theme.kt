package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonMint,
    onPrimary = OledBlack,
    primaryContainer = Color(0xFF00382B),
    onPrimaryContainer = NeonMint,
    secondary = ElectricCyan,
    onSecondary = OledBlack,
    secondaryContainer = Color(0xFF00334E),
    onSecondaryContainer = ElectricCyan,
    tertiary = GlowingViolet,
    onTertiary = TextWhite,
    background = OledBlack,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMuted,
    outline = BorderGlass,
    error = AlertRed
)

@Composable
fun MindDecoderTheme(
    darkTheme: Boolean = true, // MindDecoder defaults to dark OLED theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

