package com.example.chafund.feature.history.presentation.daily

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chafund.core.presentation.components.EmptyView
import com.example.chafund.core.presentation.components.MetricCard
import com.example.chafund.feature.history.domain.model.DailySummary
import com.example.chafund.ui.theme.AppColors

@Composable
fun DailyHistoryScreenRoot(
    viewModel: DailyHistoryViewModel,
    onDayClick: (monthId: Long, dateEpoch: Long) -> Unit,
    monthId: Long,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DailyHistoryScreen(state = state, monthId = monthId, onDayClick = onDayClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHistoryScreen(
    state: DailyHistoryUiState,
    monthId: Long,
    onDayClick: (monthId: Long, dateEpoch: Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily history", fontSize = 15.sp, fontWeight = FontWeight.W500) },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.BalanceFillLight,
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Text(
                            text     = state.monthLabel,
                            fontSize = 11.sp,
                            color    = AppColors.BalanceTextLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Sticky summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricCard(
                        label      = "Spent",
                        value      = state.spent,
                        fillColor  = AppColors.SpentFill,
                        labelColor = AppColors.SpentText,
                        valueColor = AppColors.SpentText,
                        modifier   = Modifier.weight(1f),
                    )
                    MetricCard(
                        label      = "Balance",
                        value      = state.balance,
                        fillColor  = AppColors.BalanceFillLight,
                        labelColor = AppColors.BalanceTextLight,
                        valueColor = AppColors.BalanceTextLight,
                        modifier   = Modifier.weight(1f),
                    )
                }
            }

            // Date list
            if (state.days.isEmpty() && !state.isLoading) {
                item { EmptyView("No activity yet this month.") }
            } else {
                item {
                    val shape = RoundedCornerShape(14.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, AppColors.BorderLight, shape),
                        shape = shape,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column {
                            state.days.forEachIndexed { index, day ->
                                DayRow(day = day, onClick = { onDayClick(monthId, day.date) })
                                if (index < state.days.lastIndex) HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayRow(day: DailySummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = day.dateLabel, fontSize = 14.sp, fontWeight = FontWeight.W500)
            Text(
                text     = "Spent ${day.totalSpent.formatTk()}",
                fontSize = 11.sp,
                color    = AppColors.SpentText,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "today's entry", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "+${day.totalEntries.formatTk()}", fontSize = 13.sp, color = AppColors.EntryDeltaText)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
