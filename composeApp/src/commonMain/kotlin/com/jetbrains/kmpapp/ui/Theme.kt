package com.jetbrains.kmpapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary             = PrimaryGreen,
    onPrimary           = SurfaceWhite,
    primaryContainer    = PrimaryContainer,
    onPrimaryContainer  = OnPrimaryContainer,

    secondary           = SecondaryPeach,
    onSecondary         = SurfaceWhite,
    secondaryContainer  = SecondaryPeachLight,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary            = AccentTerracotta,
    onTertiary          = SurfaceWhite,

    background          = BackgroundWarm,
    onBackground        = TextPrimary,

    surface             = SurfaceWhite,
    onSurface           = TextPrimary,
    surfaceVariant      = SurfaceVariantCream,
    onSurfaceVariant    = TextSecondary,

    outline             = DividerColor,
    outlineVariant      = OutlineVariantLight,

    error               = ErrorRed,
    onError             = SurfaceWhite,
    errorContainer      = ErrorContainerLight,
    onErrorContainer    = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary             = DarkPrimary,
    onPrimary           = SurfaceWhite,
    primaryContainer    = DarkPrimaryContainer,
    onPrimaryContainer  = DarkOnPrimaryContainer,

    secondary           = DarkSecondary,
    onSecondary         = SurfaceWhite,
    secondaryContainer  = DarkSecondaryDark,
    onSecondaryContainer = SurfaceWhite,

    tertiary            = DarkSecondary,
    onTertiary          = SurfaceWhite,

    background          = DarkBackground,
    onBackground        = DarkOnBackground,

    surface             = DarkSurface,
    onSurface           = DarkOnBackground,
    surfaceVariant      = DarkSurfaceVariant,
    onSurfaceVariant    = DarkOnSurfaceVariant,

    outline             = DarkOutline,
    outlineVariant      = DarkOutlineVariant,

    error               = DarkError,
    onError             = SurfaceWhite,
    errorContainer      = DarkErrorContainer,
    onErrorContainer    = DarkError,
)

/**
 * Extra semantic colors that don't fit M3 ColorScheme directly.
 * Resolve at call site via [LocalSemanticColors.current].
 */
data class SemanticColors(
    val warning: Color,
    val warningContainer: Color,
    val success: Color,
    val successContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val gold: Color,
    val goldContainer: Color,
    val neutralContainer: Color,
    val textTertiary: Color,
    val priorityHigh: Color = PriorityHigh,
    val priorityMedium: Color = PriorityMedium,
    val priorityLow: Color = PriorityLow,
    val oauthAppleBg: Color,
    val oauthAppleText: Color,
    val oauthBorder: Color,
)

private val SemanticLight = SemanticColors(
    warning = SecondaryPeach,
    warningContainer = WarningContainerLight,
    success = PrimaryGreen,
    successContainer = SuccessContainerLight,
    info = InfoBlueLight,
    infoContainer = InfoContainerLight,
    gold = GoldLight,
    goldContainer = GoldContainerLight,
    neutralContainer = NeutralContainerLight,
    textTertiary = TextTertiary,
    oauthAppleBg = OauthAppleBgLight,
    oauthAppleText = OauthAppleTextLight,
    oauthBorder = OauthBorderLight,
)

private val SemanticDark = SemanticColors(
    warning = DarkWarning,
    warningContainer = DarkWarningContainer,
    success = DarkSuccess,
    successContainer = DarkSuccessContainer,
    info = InfoBlueDark,
    infoContainer = DarkInfoContainer,
    gold = DarkGold,
    goldContainer = DarkGoldContainer,
    neutralContainer = DarkNeutralContainer,
    textTertiary = DarkTextTertiary,
    oauthAppleBg = OauthAppleBgDark,
    oauthAppleText = OauthAppleTextDark,
    oauthBorder = OauthBorderDark,
)

val LocalSemanticColors = staticCompositionLocalOf { SemanticLight }

@Composable
fun SweetHomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val semantic = if (darkTheme) SemanticDark else SemanticLight

    CompositionLocalProvider(
        LocalSemanticColors provides semantic,
        LocalSpacing provides SweetHomeSpacing,
        LocalShapes provides SweetHomeShapes,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = SweetHomeM3Typography,
            shapes = SweetHomeM3Shapes,
            content = content,
        )
    }
}
