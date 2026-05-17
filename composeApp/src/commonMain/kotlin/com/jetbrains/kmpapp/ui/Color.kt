package com.jetbrains.kmpapp.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────
// Cozy palette — direct mapping from cozy-shared.jsx tokens.
// Light + Dark are NOT mirror images — dark uses lifted sage, warmer
// dark neutrals, container colors that work on dark surfaces.
// ─────────────────────────────────────────────────────────────────────

// ── Light ─────────────────────────────────────────────────────────────
private val LightBg              = Color(0xFFFAF7F2)
private val LightBgWarm          = Color(0xFFF3EDE2)
private val LightSurface         = Color(0xFFFFFFFF)
private val LightSurfaceSoft     = Color(0xFFF8F4ED)
private val LightSurfaceVariant  = Color(0xFFEFE9DD)
private val LightOnBg            = Color(0xFF2A2622)
private val LightOnBgSec         = Color(0xFF6B645A)
private val LightOnBgTer         = Color(0xFFA89F92)
private val LightPrimary         = Color(0xFF4A6B49)
private val LightPrimaryLight    = Color(0xFF7A9A78)
private val LightPrimaryContainer= Color(0xFFDFE8DD)
private val LightOnPrimary       = Color(0xFFFFFFFF)
private val LightCoral           = Color(0xFFD67A5B)
private val LightCoralSoft       = Color(0xFFFCE6DC)
private val LightOchre           = Color(0xFFC49A3F)
private val LightOchreSoft       = Color(0xFFFBF1D8)
private val LightLavender        = Color(0xFF8B7CA8)
private val LightLavenderSoft    = Color(0xFFECE6F4)
private val LightSuccess         = Color(0xFF5B8856)
private val LightError           = Color(0xFFC24F45)
private val LightOutline         = Color(0xFFE8E1D3)
private val LightOutlineStrong   = Color(0xFFD4CCBA)

// ── Dark ──────────────────────────────────────────────────────────────
private val DarkBg               = Color(0xFF1A1C18)
private val DarkBgWarm           = Color(0xFF22241F)
private val DarkSurface          = Color(0xFF23251F)
private val DarkSurfaceSoft      = Color(0xFF1F211C)
private val DarkSurfaceVariant   = Color(0xFF2D2F28)
private val DarkOnBg             = Color(0xFFEDE7DC)
private val DarkOnBgSec          = Color(0xFFA8A294)
private val DarkOnBgTer          = Color(0xFF6B6558)
private val DarkPrimary          = Color(0xFF9CB89A)
private val DarkPrimaryLight     = Color(0xFFB5CDB3)
private val DarkPrimaryContainer = Color(0xFF2F4530)
private val DarkOnPrimary        = Color(0xFF0F1A0F)
private val DarkCoral            = Color(0xFFE89878)
private val DarkCoralSoft        = Color(0xFF3D2A20)
private val DarkOchre            = Color(0xFFD9B566)
private val DarkOchreSoft        = Color(0xFF3A2E14)
private val DarkLavender         = Color(0xFFB0A0CC)
private val DarkLavenderSoft     = Color(0xFF2E2738)
private val DarkSuccess          = Color(0xFF7AAD75)
private val DarkError            = Color(0xFFE6776C)
private val DarkOutline          = Color(0xFF3A3C34)
private val DarkOutlineStrong    = Color(0xFF4D4F44)

// ─────────────────────────────────────────────────────────────────────
// Material 3 ColorScheme mapping.
// ─────────────────────────────────────────────────────────────────────

internal val cozyLightScheme: ColorScheme = lightColorScheme(
    primary             = LightPrimary,
    onPrimary           = LightOnPrimary,
    primaryContainer    = LightPrimaryContainer,
    onPrimaryContainer  = LightPrimary,
    secondary           = LightCoral,
    onSecondary         = LightOnPrimary,
    secondaryContainer  = LightCoralSoft,
    onSecondaryContainer= Color(0xFF6A3520),
    tertiary            = LightLavender,
    onTertiary          = LightOnPrimary,
    tertiaryContainer   = LightLavenderSoft,
    onTertiaryContainer = Color(0xFF3F345A),
    background          = LightBg,
    onBackground        = LightOnBg,
    surface             = LightSurface,
    onSurface           = LightOnBg,
    surfaceVariant      = LightSurfaceVariant,
    onSurfaceVariant    = LightOnBgSec,
    outline             = LightOutlineStrong,
    outlineVariant      = LightOutline,
    error               = LightError,
    onError             = LightOnPrimary,
)

internal val cozyDarkScheme: ColorScheme = darkColorScheme(
    primary             = DarkPrimary,
    onPrimary           = DarkOnPrimary,
    primaryContainer    = DarkPrimaryContainer,
    onPrimaryContainer  = DarkPrimaryLight,
    secondary           = DarkCoral,
    onSecondary         = DarkOnPrimary,
    secondaryContainer  = DarkCoralSoft,
    onSecondaryContainer= DarkCoral,
    tertiary            = DarkLavender,
    onTertiary          = DarkOnPrimary,
    tertiaryContainer   = DarkLavenderSoft,
    onTertiaryContainer = DarkLavender,
    background          = DarkBg,
    onBackground        = DarkOnBg,
    surface             = DarkSurface,
    onSurface           = DarkOnBg,
    surfaceVariant      = DarkSurfaceVariant,
    onSurfaceVariant    = DarkOnBgSec,
    outline             = DarkOutlineStrong,
    outlineVariant      = DarkOutline,
    error               = DarkError,
    onError             = DarkOnPrimary,
)

// ─────────────────────────────────────────────────────────────────────
// Cozy-specific accents that don't fit Material 3.
// Access via LocalCozyExtraColors.current.<token>.
// ─────────────────────────────────────────────────────────────────────

@Immutable
data class CozyExtraColors(
    val bgWarm: Color,
    val surfaceSoft: Color,
    val textTer: Color,
    val ochre: Color,
    val ochreSoft: Color,
    val coral: Color,
    val coralSoft: Color,
    val lavender: Color,
    val lavenderSoft: Color,
    val primaryLight: Color,
    val success: Color,
    val outlineSoft: Color,
)

internal val cozyLightExtras = CozyExtraColors(
    bgWarm        = LightBgWarm,
    surfaceSoft   = LightSurfaceSoft,
    textTer       = LightOnBgTer,
    ochre         = LightOchre,
    ochreSoft     = LightOchreSoft,
    coral         = LightCoral,
    coralSoft     = LightCoralSoft,
    lavender      = LightLavender,
    lavenderSoft  = LightLavenderSoft,
    primaryLight  = LightPrimaryLight,
    success       = LightSuccess,
    outlineSoft   = LightOutline,
)

internal val cozyDarkExtras = CozyExtraColors(
    bgWarm        = DarkBgWarm,
    surfaceSoft   = DarkSurfaceSoft,
    textTer       = DarkOnBgTer,
    ochre         = DarkOchre,
    ochreSoft     = DarkOchreSoft,
    coral         = DarkCoral,
    coralSoft     = DarkCoralSoft,
    lavender      = DarkLavender,
    lavenderSoft  = DarkLavenderSoft,
    primaryLight  = DarkPrimaryLight,
    success       = DarkSuccess,
    outlineSoft   = DarkOutline,
)

val LocalCozyExtraColors = staticCompositionLocalOf { cozyLightExtras }

// ─────────────────────────────────────────────────────────────────────
// Kid Mode palette — separate from Material 3.
// ─────────────────────────────────────────────────────────────────────

@Immutable
data class KidColors(
    val sky: Color, val skyDeep: Color,
    val sun: Color, val sunDeep: Color,
    val apple: Color, val appleDeep: Color,
    val grass: Color, val grassDeep: Color,
    val candy: Color, val candyDeep: Color,
    val ink: Color,
    val inkSec: Color,
    val inkTer: Color,
    val cream: Color,
    val paper: Color,
    val warm: Color,
    val line: Color,
)

val kidColors = KidColors(
    sky       = Color(0xFFA6CEE8), skyDeep   = Color(0xFF7BB0D4),
    sun       = Color(0xFFFFCB47), sunDeep   = Color(0xFFE5AB2A),
    apple     = Color(0xFFF58E73), appleDeep = Color(0xFFD67057),
    grass     = Color(0xFF8EC07D), grassDeep = Color(0xFF6FA15F),
    candy     = Color(0xFFF5A8C7), candyDeep = Color(0xFFD78AAA),
    ink       = Color(0xFF3A322A),
    inkSec    = Color(0xFF6B6055),
    inkTer    = Color(0xFFA89D90),
    cream     = Color(0xFFFFFBF2),
    paper     = Color(0xFFFFFFFF),
    warm      = Color(0xFFFBF4E8),
    line      = Color(0xFFE5DDC9),
)

val LocalKidColors = staticCompositionLocalOf { kidColors }

val LocalIsKidMode = staticCompositionLocalOf { false }

// ─────────────────────────────────────────────────────────────────────
// Backward-compat: list type → color mapping (uses new Cozy accents)
// ─────────────────────────────────────────────────────────────────────

fun listColorForType(type: String, isDark: Boolean = false): Color = when (type) {
    "shopping"      -> if (isDark) DarkCoral       else LightCoral
    "home_chores"   -> if (isDark) DarkSuccess     else LightSuccess
    "general_todos" -> if (isDark) DarkPrimary     else LightPrimary
    "study"         -> if (isDark) DarkOchre       else LightOchre
    "travel"        -> if (isDark) DarkPrimaryLight else LightPrimaryLight
    "wishlist"      -> if (isDark) DarkLavender    else LightLavender
    "media"         -> if (isDark) DarkLavender    else LightLavender
    else            -> if (isDark) DarkOnBgSec     else LightOnBgSec
}

fun listEmojiForType(type: String): String = when (type) {
    "shopping"      -> "🛒"
    "home_chores"   -> "🏠"
    "general_todos" -> "✅"
    "study"         -> "📚"
    "travel"        -> "✈️"
    "wishlist"      -> "🎁"
    "media"         -> "🎬"
    else            -> "📋"
}

fun String.toComposeColor(fallback: Color = LightPrimary): Color {
    val hex = removePrefix("#")
    if (hex.length != 6) return fallback
    val r = hex.substring(0, 2).toIntOrNull(16) ?: return fallback
    val g = hex.substring(2, 4).toIntOrNull(16) ?: return fallback
    val b = hex.substring(4, 6).toIntOrNull(16) ?: return fallback
    return Color(red = r / 255f, green = g / 255f, blue = b / 255f)
}
