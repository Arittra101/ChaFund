package com.example.chafund.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Semantic money colors per UI spec §3
object AppColors {
    // Balance – blue
    val BalanceFillLight  = Color(0xFFE6F1FB)
    val BalanceFillDark   = Color(0xFF0C447C)
    val BalanceTextLight  = Color(0xFF185FA5)
    val BalanceTextDark   = Color(0xFF85B7EB)

    // Spent – coral
    val SpentFill         = Color(0xFFFAECE7)
    val SpentText         = Color(0xFF993C1D)

    // Negative balance
    val NegativeText      = Color(0xFFE24B4A)

    // Entry delta (positive green)
    val EntryDeltaText    = Color(0xFF1B7A50)

    // Card border (0.5dp)
    val BorderLight       = Color(0x26000000)   // ~0.15α black
    val BorderDark        = Color(0x24FFFFFF)   // ~0.14α white

    // Time-category chip palette (fill to text)
    val Morning  = ChipColor(Color(0xFFE1F5EE), Color(0xFF0F6E56))
    val Noon     = ChipColor(Color(0xFFE6F1FB), Color(0xFF185FA5))
    val Afternoon= ChipColor(Color(0xFFFAEEDA), Color(0xFF854F0B))
    val Evening  = ChipColor(Color(0xFFEEEDFE), Color(0xFF3C3489))

    val chipPalette = listOf(Morning, Noon, Afternoon, Evening)

    fun chipColorFor(index: Int): ChipColor = chipPalette[index % chipPalette.size]

    // Resolve balance colors based on dark-mode
    @Composable
    fun balanceFill(dark: Boolean) = if (dark) BalanceFillDark else BalanceFillLight

    @Composable
    fun balanceText(dark: Boolean) = if (dark) BalanceTextDark else BalanceTextLight
}

@Immutable
data class ChipColor(val fill: Color, val text: Color)
