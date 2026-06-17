package com.example.chafund.feature.history.presentation.daydetail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.chafund.core.presentation.components.AmountField
import com.example.chafund.core.presentation.components.CategoryChip
import com.example.chafund.core.presentation.components.ConfirmationBottomSheet
import com.example.chafund.core.presentation.components.EmptyView
import com.example.chafund.core.presentation.components.MetricCard
import com.example.chafund.core.presentation.components.PrimaryButton
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.model.ExpenseGrouped
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.feature.history.domain.model.HistoryExpense
import com.example.chafund.ui.theme.AppColors

@Composable
fun DayDetailScreenRoot(viewModel: DayDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(DayDetailEvent.SnackbarDismissed)
        }
    }
    DayDetailScreen(state, viewModel::onEvent, onBack, snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    state: DayDetailUiState,
    onEvent: (DayDetailEvent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val entriesTotal = remember(state.entries) { Money(state.entries.sumOf { it.amountPaisa }) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(state.dateLabel, fontSize = 15.sp, fontWeight = FontWeight.W500) },
                actions = {
                    if (state.isReadOnly) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppColors.SpentFill,
                            modifier = Modifier.padding(end = 12.dp),
                        ) {
                            Text(
                                text     = "Read-only",
                                fontSize = 11.sp,
                                color    = AppColors.SpentText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Summary row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricCard(
                        label      = "Entries",
                        value      = entriesTotal,
                        fillColor  = AppColors.BalanceFillLight,
                        labelColor = AppColors.BalanceTextLight,
                        valueColor = AppColors.BalanceTextLight,
                        modifier   = Modifier.weight(1f),
                    )
                    MetricCard(
                        label      = "Spent",
                        value      = state.totalSpent,
                        fillColor  = AppColors.SpentFill,
                        labelColor = AppColors.SpentText,
                        valueColor = AppColors.SpentText,
                        modifier   = Modifier.weight(1f),
                    )
                }
            }

            // Entries card
            item {
                SectionLabel("Entries")
                if (state.entries.isEmpty()) {
                    EmptyView("No entries today.")
                } else {
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
                                EntryRow(
                                    entry      = entry,
                                    isReadOnly = state.isReadOnly,
                                    onEdit     = { onEvent(DayDetailEvent.EditEntry(entry)) },
                                    onDelete   = { onEvent(DayDetailEvent.RequestDelete(entry.id, DeleteType.ENTRY)) },
                                )
                                if (index < state.entries.lastIndex) {
                                    HorizontalDivider(thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            // Expenses card
            item {
                SectionLabel("Expenses")
                if (state.groups.isEmpty()) {
                    EmptyView("No expenses today.")
                } else {
                    val shape = RoundedCornerShape(14.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, AppColors.BorderLight, shape),
                        shape = shape,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column {
                            state.groups.forEachIndexed { groupIndex, group ->
                                ExpenseGroupSection(
                                    group      = group,
                                    isReadOnly = state.isReadOnly,
                                    onEdit     = { exp -> onEvent(DayDetailEvent.EditExpense(exp)) },
                                    onDelete   = { id -> onEvent(DayDetailEvent.RequestDelete(id, DeleteType.EXPENSE)) },
                                )
                                if (groupIndex < state.groups.lastIndex) {
                                    HorizontalDivider(thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (state.editingEntry != null || state.editingExpense != null) {
        ModalBottomSheet(onDismissRequest = { onEvent(DayDetailEvent.DismissEdit) }) {
            EditSheet(state = state, onEvent = onEvent)
        }
    }

    if (state.pendingDeleteId != null) {
        ConfirmationBottomSheet(
            title     = if (state.pendingDeleteType == DeleteType.ENTRY) "Delete entry?" else "Delete expense?",
            body      = "This record will be permanently removed. Totals will update immediately.",
            onConfirm = { onEvent(DayDetailEvent.ConfirmDelete) },
            onDismiss = { onEvent(DayDetailEvent.DismissDelete) },
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text     = title,
        fontSize = 11.sp,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun EntryRow(
    entry: HistoryEntry,
    isReadOnly: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = "+${Money(entry.amountPaisa).formatTk()}",
                fontSize   = 14.sp,
                fontWeight = FontWeight.W500,
                color      = AppColors.EntryDeltaText,
            )
            val meta = if (!entry.ref.isNullOrBlank()) "${entry.ref} · ${entry.time}" else entry.time
            Text(text = meta, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!isReadOnly) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AppColors.BalanceTextLight)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ExpenseGroupSection(
    group: ExpenseGrouped,
    isReadOnly: Boolean,
    onEdit: (HistoryExpense) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val chipColor = AppColors.chipColorFor(group.sortOrder - 1)
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        CategoryChip(label = group.categoryName, chipColor = chipColor)
        Spacer(Modifier.height(6.dp))
        group.expenses.forEachIndexed { index, exp ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val meta = if (!exp.ref.isNullOrBlank()) "${exp.ref} · ${exp.time}" else exp.time
                    Text(text = meta, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text       = Money(exp.amountPaisa).formatTk(),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.W500,
                    color      = AppColors.SpentText,
                )
                if (!isReadOnly) {
                    IconButton(onClick = { onEdit(exp) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AppColors.BalanceTextLight)
                    }
                    IconButton(onClick = { onDelete(exp.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (index < group.expenses.lastIndex) {
                HorizontalDivider(thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun EditSheet(state: DayDetailUiState, onEvent: (DayDetailEvent) -> Unit) {
    val label = if (state.editingEntry != null) "Edit entry" else "Edit expense"
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(label, fontSize = 17.sp, fontWeight = FontWeight.W500)
        AmountField(
            value         = state.editAmountInput,
            onValueChange = { onEvent(DayDetailEvent.OnEditAmountChange(it)) },
            error         = state.editAmountError,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Ref note · optional", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value         = state.editRefInput,
                onValueChange = { if (it.length <= 80) onEvent(DayDetailEvent.OnEditRefChange(it)) },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("e.g. office collection", fontSize = 13.sp) },
                singleLine    = true,
                shape         = RoundedCornerShape(8.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                ),
            )
        }
        PrimaryButton(text = "Save changes", onClick = { onEvent(DayDetailEvent.SaveEdit) })
    }
}
