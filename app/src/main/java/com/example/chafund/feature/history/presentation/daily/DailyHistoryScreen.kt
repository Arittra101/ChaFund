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
import androidx.compose.material3.Checkbox
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
import com.example.chafund.core.utils.DateTimeFormat
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.model.DailySummary
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.ui.theme.AppColors

@Composable
fun DailyHistoryScreenRoot(
    viewModel: DailyHistoryViewModel,
    onDayClick: (monthId: Long?, dateEpoch: Long) -> Unit,
    monthId: Long? = null,
    onOpenEntries: (Long) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DailyHistoryScreen(
        state = state,
        monthId = monthId,
        onDayClick = onDayClick,
        onToggleCycle = viewModel::onToggleIncludePrevTail,
        onOpenEntries = onOpenEntries,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHistoryScreen(
    state: DailyHistoryUiState,
    monthId: Long? = null,
    onDayClick: (monthId: Long?, dateEpoch: Long) -> Unit,
    onToggleCycle: (Boolean) -> Unit = {},
    onOpenEntries: (Long) -> Unit = {},
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricCard(
                        label = "Spent",
                        value = state.spent,
                        fillColor = AppColors.SpentFill,
                        labelColor = AppColors.SpentText,
                        valueColor = AppColors.SpentText,
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        label = "Balance",
                        value = state.balance,
                        fillColor = AppColors.BalanceFillLight,
                        labelColor = AppColors.BalanceTextLight,
                        valueColor = AppColors.BalanceTextLight,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Cycle (previous-month tail) toggle
            if (state.showCycleToggle) {
                item {
                    val shape = RoundedCornerShape(14.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, AppColors.BorderLight, shape),
                        shape = shape,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !state.isReadOnly) { onToggleCycle(!state.includePrevTail) }
                                .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = state.cycleToggleLabel,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                            )
                            Checkbox(
                                checked = state.includePrevTail,
                                onCheckedChange = { onToggleCycle(it) },
                                enabled = !state.isReadOnly,
                            )
                        }
                    }
                }
            }

            // Entries card (person name · date · time · amount) with tail-aware total — preview, opens full list
            item { EntriesCard(state = state, onOpen = { onOpenEntries(state.monthId) }) }

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
private fun EntriesCard(state: DailyHistoryUiState, onOpen: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, AppColors.BorderLight, shape)
            .clickable(onClick = onOpen),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Entries",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Total +${state.entriesTotal.formatTk()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = AppColors.EntryDeltaText,
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            HorizontalDivider(thickness = 0.5.dp)
            if (state.entries.isEmpty()) {
                Text(
                    text = "No entries this month.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                )
            } else {
                val preview = state.entries.take(3)
                preview.forEachIndexed { index, entry ->
                    EntryListRow(entry)
                    if (index < preview.lastIndex) HorizontalDivider(thickness = 0.5.dp)
                }
                if (state.entries.size > 3) {
                    HorizontalDivider(thickness = 0.5.dp)
                    Text(
                        text = "+${state.entries.size - 3} more · View all",
                        fontSize = 12.sp,
                        color = AppColors.BalanceTextLight,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryListRow(entry: HistoryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.displayLabel ?: "Unnamed",
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
            )
            Text(
                text = "${DateTimeFormat.formatDate(entry.date)} · ${entry.time}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "+${Money(entry.amountPaisa).formatTk()}",
            fontSize = 13.sp,
            fontWeight = FontWeight.W500,
            color = AppColors.EntryDeltaText,
        )
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
