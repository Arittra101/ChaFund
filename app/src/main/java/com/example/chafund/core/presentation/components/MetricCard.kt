package com.example.chafund.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chafund.core.utils.Money
import com.example.chafund.ui.theme.AppColors

@Composable
fun MetricCard(
    label: String,
    value: Money,
    fillColor: Color,
    labelColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    Card(
        modifier = modifier
            .border(0.5.dp, AppColors.BorderLight, shape),
        shape  = shape,
        colors = CardDefaults.cardColors(containerColor = fillColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text       = label,
                fontSize   = 11.sp,
                fontWeight = FontWeight.W400,
                color      = labelColor,
            )
            Text(
                text       = value.formatTk(),
                fontSize   = 21.sp,
                fontWeight = FontWeight.W500,
                color      = if (value.isNegative) AppColors.NegativeText else valueColor,
            )
        }
    }
}
