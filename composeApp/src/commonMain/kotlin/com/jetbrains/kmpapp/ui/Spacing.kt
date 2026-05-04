package com.jetbrains.kmpapp.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SweetHome spacing scale — strict 4-pt + intermediate 6/10/14/20 dp.
 *
 * Use ONLY these values. Hardcoded `.dp` outside this scale is a bug.
 *
 * Quick guide:
 *  • xxs (4)     — icon ↔ counter, tight metadata
 *  • sm  (8)     — between list cards
 *  • md  (10)    — gap inside chips, icon ↔ label (NEW semantics)
 *  • base(12)   — card padding default
 *  • lg  (14)   — comfortable inner padding
 *  • xl  (16)   — screen horizontal padding, hero card padding
 *  • xxl (20)   — section gap
 *  • xxxl(24)   — between major sections
 *  • xxxxl(32)  — hero header vertical
 *  • huge(48)   — bottom-sheet bottom padding (safe-area)
 *
 * Naming above intentionally matches the new design handoff. Old short
 * mnemonics (xs/sm/md/lg/xl/xxl) were re-mapped during the token migration.
 */
object SweetHomeSpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 6.dp           // NEW (was 8 in legacy scale)
    val sm: Dp = 8.dp           // remapped from old `xs`
    val md: Dp = 10.dp          // NEW intermediate
    val base: Dp = 12.dp        // remapped from old `sm`
    val lg: Dp = 14.dp          // NEW intermediate
    val xl: Dp = 16.dp          // remapped from old `md`
    val xxl: Dp = 20.dp         // NEW intermediate
    val xxxl: Dp = 24.dp        // remapped from old `lg`
    val xxxxl: Dp = 32.dp       // remapped from old `xl`
    val huge: Dp = 48.dp        // remapped from old `xxl`

    // Component-specific aliases (used directly by layouts)
    val screenHorizontal: Dp = 16.dp
    val cardPadding: Dp = 14.dp
    val heroPaddingV: Dp = 28.dp
    val heroPaddingH: Dp = 20.dp
    val bottomSheetPaddingTop: Dp = 20.dp
    val bottomSheetPaddingH: Dp = 24.dp
    val bottomSheetPaddingBottom: Dp = 50.dp
    val bottomNavHeight: Dp = 60.dp

    // Component sizes (named to discourage raw .dp hardcoding)
    val iconButton: Dp = 36.dp
    val sendButton: Dp = 44.dp
    val fabSize: Dp = 56.dp
    val avatarLarge: Dp = 64.dp
    val avatarHero: Dp = 72.dp
    val illustrationSmall: Dp = 80.dp
}

val LocalSpacing = staticCompositionLocalOf { SweetHomeSpacing }
