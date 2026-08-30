package com.example.chafund.feature.history.presentation.entries

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.chafund.core.utils.DateTimeFormat
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.ui.theme.AppColors

@Composable
fun MonthEntriesScreenRoot(
    viewModel: MonthEntriesViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MonthEntriesScreen(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthEntriesScreen(
    state: MonthEntriesUiState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entries", fontSize = 15.sp, fontWeight = FontWeight.W500) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.BalanceFillLight,
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Text(
                            text = state.monthLabel,
                            fontSize = 11.sp,
                            color = AppColors.BalanceTextLight,
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
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Total entries",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "+${state.entriesTotal.formatTk()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W500,
                        color = AppColors.EntryDeltaText,
                    )
                }
            }

            if (state.entries.isEmpty() && !state.isLoading) {
                item { EmptyView("No entries this month.") }
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
                            state.entries.forEachIndexed { index, entry ->
                                EntryRow(entry)
                                if (index < state.entries.lastIndex) HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: HistoryEntry) {
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
