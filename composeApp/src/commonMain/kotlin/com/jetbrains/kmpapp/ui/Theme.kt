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
    outlineVariant = Color(0xFFC9C9C9),
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = PrimaryGreenDark,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkPrimaryLight,
    secondary = SecondaryPeach,
    onSecondary = Color(0xFF5C3A1E),
    secondaryContainer = Color(0xFF5C3A1E),
    onSecondaryContainer = SecondaryPeachLight,
    tertiary = AccentTerracotta,
    onTertiary = Color(0xFF1A1A1A),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = OnPrimaryWhite,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
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
