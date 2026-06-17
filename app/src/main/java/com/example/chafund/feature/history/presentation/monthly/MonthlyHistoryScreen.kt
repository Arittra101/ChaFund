package com.example.chafund.feature.history.presentation.monthly

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.chafund.feature.history.domain.model.HistoryMonth
import com.example.chafund.ui.theme.AppColors

@Composable
fun MonthlyHistoryScreenRoot(
    viewModel: MonthlyHistoryViewModel,
    onMonthClick: (monthId: Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MonthlyHistoryScreen(state = state, onMonthClick = onMonthClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyHistoryScreen(
    state: MonthlyHistoryUiState,
    onMonthClick: (monthId: Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Monthly history", fontSize = 15.sp, fontWeight = FontWeight.W500) })
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.months.isEmpty() && !state.isLoading) {
                item { EmptyView("No months yet.") }
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
                            state.months.forEachIndexed { index, month ->
                                MonthRow(month = month, onClick = { onMonthClick(month.id) })
                                if (index < state.months.lastIndex) HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthRow(month: HistoryMonth, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = month.label, fontSize = 14.sp, fontWeight = FontWeight.W500)
                if (month.isCurrent) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.BalanceFillLight,
                    ) {
                        Text(
                            text     = "current",
                            fontSize = 10.sp,
                            color    = AppColors.BalanceTextLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            Text(
                text     = "Entry ${month.totalEntries.formatTk()} · Spent ${month.totalSpent.formatTk()}",
                fontSize = 11.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text       = month.balance.formatTk(),
            fontSize   = 13.sp,
            color      = if (month.balance.isNegative) AppColors.NegativeText else AppColors.BalanceTextLight,
            fontWeight = FontWeight.W500,
        )
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
