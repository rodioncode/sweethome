package com.jetbrains.kmpapp.ui

import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────────────────────────
// LIGHT — surfaces, brand, text
// ──────────────────────────────────────────────────────────────────

val PrimaryGreen          = Color(0xFF5B7C5A)
val PrimaryGreenLight     = Color(0xFF8FAE8B)
val PrimaryGreenDark      = Color(0xFF3D5C3C)
val PrimaryContainer      = Color(0xFFEDF5ED)
val OnPrimaryContainer    = Color(0xFF3D5C3C)

val SecondaryPeach        = Color(0xFFE8A87C)
val SecondaryPeachLight   = Color(0xFFF2C9A8)
val OnSecondaryContainer  = Color(0xFF8A5E40)
val AccentTerracotta      = Color(0xFFD4956B)

val BackgroundWarm        = Color(0xFFFBF7F4)
val SurfaceWhite          = Color(0xFFFFFFFF)
val SurfaceVariantCream   = Color(0xFFF5EDE6)

val TextPrimary           = Color(0xFF2C2C2C)
val TextSecondary         = Color(0xFF7A7A7A)
val TextTertiary          = Color(0xFFBDBDBD)

val ErrorRed              = Color(0xFFD4574E)
val DividerColor          = Color(0xFFE8E0D8)
val OutlineVariantLight   = Color(0xFFC9C9C9)

// Backward-compat alias — used by legacy components.
val OnPrimaryWhite        = SurfaceWhite

// Semantic containers (badges, banners, row highlights)
val ErrorContainerLight    = Color(0xFFFDECEA)
val WarningContainerLight  = Color(0xFFFDF3EC)
val SuccessContainerLight  = Color(0xFFE8F5E8)
val InfoContainerLight     = Color(0xFFE3F2FD)
val GoldContainerLight     = Color(0xFFFFFDE7)
val NeutralContainerLight  = Color(0xFFF5F5F5)

// Goal / leaderboard accents
val GoldLight             = Color(0xFFFFA726)

// Info accent (used through SemanticColors.info)
val InfoBlueLight         = Color(0xFF42A5F5)

// OAuth surfaces (light defaults; flipped in dark via SemanticColors)
val OauthAppleBgLight     = Color(0xFF1D1D1F)
val OauthAppleTextLight   = Color(0xFFFFFFFF)
val OauthBorderLight      = Color(0xFFDADCE0)

// ──────────────────────────────────────────────────────────────────
// DARK — calmer, neutral charcoal surfaces + deep sage
// ──────────────────────────────────────────────────────────────────

val DarkPrimary           = Color(0xFF4C6C4B)
val DarkPrimaryLight      = Color(0xFF6B8A6A)
val DarkPrimaryDark       = Color(0xFF3D5C3C)
val DarkPrimaryContainer  = Color(0xFF243024)
val DarkOnPrimaryContainer = Color(0xFFB5D2B3)

val DarkSecondary         = Color(0xFFA87553)
val DarkSecondaryDark     = Color(0xFF8A5E40)

val DarkBackground        = Color(0xFF1C1D1F)
val DarkSurface           = Color(0xFF26282A)
val DarkSurfaceVariant    = Color(0xFF2F3134)
val DarkOutline           = Color(0xFF34373A)
val DarkOutlineVariant    = Color(0xFF42464A)

val DarkOnBackground      = Color(0xFFE6E7E5)
val DarkOnSurfaceVariant  = Color(0xFF9A9C99)
val DarkTextTertiary      = Color(0xFF6A6C6A)

val DarkError             = Color(0xFFA84D44)
val DarkSuccess           = Color(0xFF4C6C4B)
val DarkWarning           = Color(0xFFA87553)
val DarkGold              = Color(0xFFA88A4A)

val DarkErrorContainer    = Color(0xFF3A2422)
val DarkWarningContainer  = Color(0xFF3A2E24)
val DarkSuccessContainer  = Color(0xFF243024)
val DarkInfoContainer     = Color(0xFF1F2A38)
val DarkGoldContainer     = Color(0xFF332C1A)
val DarkNeutralContainer  = Color(0xFF2A2A2C)

val InfoBlueDark          = Color(0xFF5894C2)

val OauthAppleBgDark      = Color(0xFFFFFFFF)
val OauthAppleTextDark    = Color(0xFF1D1D1F)
val OauthBorderDark       = Color(0xFF3A413B)

// ──────────────────────────────────────────────────────────────────
// LIST COLORS — applied to list icon/avatar only, never to UI surfaces.
// ──────────────────────────────────────────────────────────────────

object ListColors {
    val CoralLight    = Color(0xFFFF7043)
    val SkyLight      = Color(0xFF42A5F5)
    val MintLight     = Color(0xFF66BB6A)
    val LavenderLight = Color(0xFFAB47BC)
    val AmberLight    = Color(0xFFFFA726)
    val RoseLight     = Color(0xFFEC407A)
    val SlateLight    = Color(0xFF78909C)

    val CoralDark     = Color(0xFFD27155)
    val SkyDark       = Color(0xFF5894C2)
    val MintDark      = Color(0xFF6BA46E)
    val LavenderDark  = Color(0xFF9758A4)
    val AmberDark     = Color(0xFFD49642)
    val RoseDark      = Color(0xFFC25578)
    val SlateDark     = Color(0xFF778896)
}

// Backward-compat aliases for callers that still reference the old flat names.
val ListColorCoral        = ListColors.CoralLight
val ListColorSky          = ListColors.SkyLight
val ListColorMint         = ListColors.MintLight
val ListColorLavender     = ListColors.LavenderLight
val ListColorAmber        = ListColors.AmberLight
val ListColorRose         = ListColors.RoseLight
val ListColorSlate        = ListColors.SlateLight
val ListColorCoralDark    = ListColors.CoralDark
val ListColorSkyDark      = ListColors.SkyDark
val ListColorMintDark     = ListColors.MintDark
val ListColorLavenderDark = ListColors.LavenderDark
val ListColorAmberDark    = ListColors.AmberDark
val ListColorRoseDark     = ListColors.RoseDark
val ListColorSlateDark    = ListColors.SlateDark

// ──────────────────────────────────────────────────────────────────
// PRIORITY — semantic, not in M3 ColorScheme
// ──────────────────────────────────────────────────────────────────

val PriorityHigh   = Color(0xFFEF5350)
val PriorityMedium = Color(0xFFFFA726)
val PriorityLow    = Color(0xFF66BB6A)

// ──────────────────────────────────────────────────────────────────
// Helpers (kept from prior API — used widely)
// ──────────────────────────────────────────────────────────────────

fun listColorForType(type: String, isDark: Boolean = false): Color = when (type) {
    "shopping"      -> if (isDark) ListColors.CoralDark    else ListColors.CoralLight
    "home_chores"   -> if (isDark) ListColors.MintDark     else ListColors.MintLight
    "general_todos" -> if (isDark) ListColors.SkyDark      else ListColors.SkyLight
    "study"         -> if (isDark) ListColors.AmberDark    else ListColors.AmberLight
    "travel"        -> if (isDark) ListColors.SkyDark      else ListColors.SkyLight
    "wishlist"      -> if (isDark) ListColors.RoseDark     else ListColors.RoseLight
    "media"         -> if (isDark) ListColors.LavenderDark else ListColors.LavenderLight
    else            -> if (isDark) ListColors.SlateDark    else ListColors.SlateLight
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

fun String.toComposeColor(fallback: Color = PrimaryGreen): Color {
    val hex = removePrefix("#")
    if (hex.length != 6) return fallback
    val r = hex.substring(0, 2).toIntOrNull(16) ?: return fallback
    val g = hex.substring(2, 4).toIntOrNull(16) ?: return fallback
    val b = hex.substring(4, 6).toIntOrNull(16) ?: return fallback
    return Color(red = r / 255f, green = g / 255f, blue = b / 255f)
}
