package com.example.chafund.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chafund.ui.theme.ChipColor

@Composable
fun CategoryChip(
    label: String,
    chipColor: ChipColor,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        modifier  = modifier,
        onClick   = onClick ?: {},
        enabled   = onClick != null,
        shape     = shape,
        color     = chipColor.fill,
        border    = if (selected)
            BorderStroke(2.dp, chipColor.text)
        else
            BorderStroke(0.dp, chipColor.fill),
    ) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = FontWeight.W500,
            color      = chipColor.text,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}
