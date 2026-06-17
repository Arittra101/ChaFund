package com.example.chafund.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Semantic color scheme ────────────────────────────────────────────────────

@Immutable
data class AppColorScheme(
    val balanceFill: Color,
    val balanceText: Color,
    val spentFill: Color,
    val spentText: Color,
    val entryDeltaText: Color,
    val negativeText: Color,
    val border: Color,
    val chipPalette: List<ChipColor>,
)

@Immutable
data class ChipColor(val fill: Color, val text: Color)

val LightAppColorScheme = AppColorScheme(
    balanceFill    = Color(0xFFE6F1FB),
    balanceText    = Color(0xFF185FA5),
    spentFill      = Color(0xFFFAECE7),
    spentText      = Color(0xFF993C1D),
    entryDeltaText = Color(0xFF1B7A50),
    negativeText   = Color(0xFFE24B4A),
    border         = Color(0x26000000),   // 15% black — visible on light surfaces
    chipPalette    = listOf(
        ChipColor(Color(0xFFE1F5EE), Color(0xFF0F6E56)),  // Morning
        ChipColor(Color(0xFFE6F1FB), Color(0xFF185FA5)),  // Noon
        ChipColor(Color(0xFFFAEEDA), Color(0xFF854F0B)),  // Afternoon
        ChipColor(Color(0xFFEEEDFE), Color(0xFF3C3489)),  // Evening
    ),
)

val DarkAppColorScheme = AppColorScheme(
    balanceFill    = Color(0xFF0C447C),   // deep blue — contrast on dark surface
    balanceText    = Color(0xFF85B7EB),   // light blue
    spentFill      = Color(0xFF2D1208),   // deep coral-brown
    spentText      = Color(0xFFFFAB91),   // light salmon
    entryDeltaText = Color(0xFF5BBF88),   // light green — visible on dark
    negativeText   = Color(0xFFFF897D),   // soft red — WCAG AA on dark
    border         = Color(0x24FFFFFF),   // 14% white — visible on dark surfaces
    chipPalette    = listOf(
        ChipColor(Color(0xFF0F2E1E), Color(0xFF4DB87A)),  // Morning dark
        ChipColor(Color(0xFF0A1F3A), Color(0xFF85B7EB)),  // Noon dark
        ChipColor(Color(0xFF2E1F08), Color(0xFFFFCC80)),  // Afternoon dark
        ChipColor(Color(0xFF1A1835), Color(0xFFB0A9F5)),  // Evening dark
    ),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColorScheme }

// ── AppColors object ─────────────────────────────────────────────────────────
// All properties are @Composable getters — they automatically return the right
// color for the current theme. All existing call sites work without changes.

object AppColors {
    val BalanceFillLight: Color  @Composable get() = LocalAppColors.current.balanceFill
    val BalanceFillDark: Color   @Composable get() = LocalAppColors.current.balanceFill
    val BalanceTextLight: Color  @Composable get() = LocalAppColors.current.balanceText
    val BalanceTextDark: Color   @Composable get() = LocalAppColors.current.balanceText
    val SpentFill: Color         @Composable get() = LocalAppColors.current.spentFill
    val SpentText: Color         @Composable get() = LocalAppColors.current.spentText
    val EntryDeltaText: Color    @Composable get() = LocalAppColors.current.entryDeltaText
    val NegativeText: Color      @Composable get() = LocalAppColors.current.negativeText
    val BorderLight: Color       @Composable get() = LocalAppColors.current.border
    val BorderDark: Color        @Composable get() = LocalAppColors.current.border

    @Composable
    fun chipColorFor(index: Int): ChipColor =
        LocalAppColors.current.chipPalette[index % LocalAppColors.current.chipPalette.size]

    // Kept for backward compat — dark param is now ignored (theme handles it)
    @Composable fun balanceFill(dark: Boolean) = LocalAppColors.current.balanceFill
    @Composable fun balanceText(dark: Boolean) = LocalAppColors.current.balanceText
}
