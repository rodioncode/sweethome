package com.jetbrains.kmpapp.ui

import androidx.compose.ui.graphics.Color

// Primary
val PrimaryGreen = Color(0xFF5B7C5A)
val PrimaryGreenLight = Color(0xFF8FAE8B)
val PrimaryGreenDark = Color(0xFF3D5C3C)
val PrimaryContainer = Color(0xFFEDF5ED)

// Secondary
val SecondaryPeach = Color(0xFFE8A87C)
val SecondaryPeachLight = Color(0xFFF2C9A8)

// Accent
val AccentTerracotta = Color(0xFFD4956B)

// Background & Surface
val BackgroundWarm = Color(0xFFFBF7F4)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceVariantCream = Color(0xFFF5EDE6)

// Text
val TextPrimary = Color(0xFF2C2C2C)
val TextSecondary = Color(0xFF7A7A7A)
val TextTertiary = Color(0xFFBDBDBD)

// Utility
val ErrorRed = Color(0xFFD4574E)
val DividerColor = Color(0xFFE8E0D8)
val OnPrimaryWhite = Color(0xFFFFFFFF)

// List colors — 7 presets (light)
val ListColorCoral = Color(0xFFFF7043)
val ListColorSky = Color(0xFF42A5F5)
val ListColorMint = Color(0xFF66BB6A)
val ListColorLavender = Color(0xFFAB47BC)
val ListColorAmber = Color(0xFFFFA726)
val ListColorRose = Color(0xFFEC407A)
val ListColorSlate = Color(0xFF78909C)

// List colors — dark variants (brightened for dark backgrounds)
val ListColorCoralDark = Color(0xFFFF8A65)
val ListColorSkyDark = Color(0xFF64B5F6)
val ListColorMintDark = Color(0xFF81C784)
val ListColorLavenderDark = Color(0xFFBA68C8)
val ListColorAmberDark = Color(0xFFFFB74D)
val ListColorRoseDark = Color(0xFFF06292)
val ListColorSlateDark = Color(0xFF90A4AE)

// Dark theme tokens (from dark.js design handoff)
val DarkPrimary = Color(0xFF9CC09A)
val DarkPrimaryLight = Color(0xFFB5D2B3)
val DarkPrimaryContainer = Color(0xFF243024)
val DarkBackground = Color(0xFF0F1310)
val DarkSurface = Color(0xFF1A1F1B)
val DarkSurfaceVariant = Color(0xFF242A25)
val DarkOutline = Color(0xFF2A302B)
val DarkOutlineVariant = Color(0xFF3A413B)
val DarkOnBackground = Color(0xFFECEDE8)
val DarkOnSurfaceVariant = Color(0xFF9DA39C)
val DarkTextTertiary = Color(0xFF6B716C)
val DarkError = Color(0xFFE57368)
val DarkSuccess = Color(0xFF9CC09A)

// Priority colors
val PriorityHigh = Color(0xFFEF5350)
val PriorityMedium = Color(0xFFFFA726)
val PriorityLow = Color(0xFF66BB6A)

fun listColorForType(type: String, isDark: Boolean = false): Color = when (type) {
    "shopping"      -> if (isDark) ListColorCoralDark    else ListColorCoral
    "home_chores"   -> if (isDark) ListColorMintDark     else ListColorMint
    "general_todos" -> if (isDark) ListColorSkyDark      else ListColorSky
    "study"         -> if (isDark) ListColorAmberDark    else ListColorAmber
    "travel"        -> if (isDark) ListColorSkyDark      else ListColorSky
    "wishlist"      -> if (isDark) ListColorRoseDark     else ListColorRose
    "media"         -> if (isDark) ListColorLavenderDark else ListColorLavender
    else            -> if (isDark) ListColorSlateDark    else ListColorSlate
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
