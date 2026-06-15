package com.example.aonime.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AonimeDarkColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = OnViolet,
    primaryContainer = VioletDark,
    onPrimaryContainer = TextPrimary,
    secondary = VioletLight,
    onSecondary = OnViolet,
    secondaryContainer = CardSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = VioletGlow,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = ErrorColor,
)

@Composable
fun AonimeTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AonimeDarkColorScheme,
        typography = Typography,
        content = content,
    )
}
