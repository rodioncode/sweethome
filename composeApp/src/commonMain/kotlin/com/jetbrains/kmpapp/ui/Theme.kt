package com.jetbrains.kmpapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryGreenLight,
    onPrimaryContainer = PrimaryGreenDark,
    secondary = SecondaryPeach,
    onSecondary = OnPrimaryWhite,
    secondaryContainer = SecondaryPeachLight,
    onSecondaryContainer = Color(0xFF5C3A1E),
    tertiary = AccentTerracotta,
    onTertiary = OnPrimaryWhite,
    background = BackgroundWarm,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantCream,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = OnPrimaryWhite,
    outline = DividerColor,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = PrimaryGreenDark,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = PrimaryGreenLight,
    secondary = SecondaryPeach,
    onSecondary = Color(0xFF5C3A1E),
    secondaryContainer = Color(0xFF5C3A1E),
    onSecondaryContainer = SecondaryPeachLight,
    tertiary = AccentTerracotta,
    onTertiary = Color(0xFF1A1A1A),
    background = Color(0xFF1A1F1A),
    onBackground = Color(0xFFE8F0E8),
    surface = Color(0xFF1F261F),
    onSurface = Color(0xFFE8F0E8),
    surfaceVariant = Color(0xFF2C352C),
    onSurfaceVariant = Color(0xFFA8B8A8),
    error = Color(0xFFFF6B63),
    onError = OnPrimaryWhite,
    outline = Color(0xFF4A4A4A),
)

@Composable
fun SweetHomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
