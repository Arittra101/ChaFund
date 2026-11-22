package com.example.chafund.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.model.AmountBreakdown
import com.example.chafund.ui.theme.AppColors

/**
 * Money split by payment method: what came in, what went out and what is left for each,
 * then the combined remaining total.
 */
@Composable
fun BreakdownCard(
    breakdown: AmountBreakdown,
    title: String = "Breakdown",
    modifier: Modifier = Modifier,
) {
    if (breakdown.isEmpty) return
    val shape = RoundedCornerShape(14.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, AppColors.BorderLight, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            breakdown.rows.forEach { row ->
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = row.methodName, fontSize = 13.sp, fontWeight = FontWeight.W500)
                    AmountLine("Collected", row.collected, AppColors.BalanceTextLight)
                    AmountLine("Expense", row.spent, AppColors.SpentText)
                    AmountLine(
                        label = "Remaining",
                        amount = row.remaining,
                        color = if (row.remaining.isNegative)
                            AppColors.NegativeText
                        else
                            MaterialTheme.colorScheme.onSurface,
                        emphasised = true,
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Total remaining",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = breakdown.totalRemaining.formatTk(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W500,
                    color = if (breakdown.totalRemaining.isNegative)
                        AppColors.NegativeText
                    else
                        AppColors.BalanceTextLight,
                )
            }
        }
    }
}

@Composable
private fun AmountLine(
    label: String,
    amount: Money,
    color: Color,
    emphasised: Boolean = false,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = amount.formatTk(),
            fontSize = if (emphasised) 14.sp else 13.sp,
            fontWeight = if (emphasised) FontWeight.W500 else FontWeight.W400,
            color = color,
        )
    }
}
