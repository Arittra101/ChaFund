package com.example.chafund.feature.history.presentation.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import com.example.chafund.core.presentation.components.CategoryChip
import com.example.chafund.core.presentation.components.ConfirmationBottomSheet
import com.example.chafund.core.presentation.components.EmptyView
import com.example.chafund.core.presentation.components.PrimaryButton
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.model.ExpenseGrouped
import com.example.chafund.feature.history.domain.model.HistoryEntry
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
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                title = { Text(state.dateLabel, fontSize = 15.sp, fontWeight = FontWeight.W500) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            val shape = RoundedCornerShape(14.dp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, AppColors.BorderLight, shape),
                shape = shape,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    // Entries section
                    SectionHeader("Entries added")
                    if (state.entries.isEmpty()) {
                        EmptyView("No entries", modifier = Modifier.padding(horizontal = 14.dp))
                    } else {
                        state.entries.forEach { entry ->
                            EntryRow(
                                entry      = entry,
                                isReadOnly = state.isReadOnly,
                                onEdit     = { onEvent(DayDetailEvent.EditEntry(entry)) },
                                onDelete   = { onEvent(DayDetailEvent.RequestDelete(entry.id, DeleteType.ENTRY)) },
                            )
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    // Expenses section
                    SectionHeader("Expenses by time")
                    if (state.groups.isEmpty()) {
                        EmptyView("No expenses", modifier = Modifier.padding(horizontal = 14.dp))
                    } else {
                        state.groups.forEach { group ->
                            ExpenseGroupSection(
                                group      = group,
                                isReadOnly = state.isReadOnly,
                                onEdit     = { exp -> onEvent(DayDetailEvent.EditExpense(exp)) },
                                onDelete   = { id -> onEvent(DayDetailEvent.RequestDelete(id, DeleteType.EXPENSE)) },
                            )
                        }
                    }

                    // Day total footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Day spent", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text       = state.totalSpent.formatTk(),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.W500,
                            color      = AppColors.SpentText,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    // Edit sheet
    if (state.editingEntry != null || state.editingExpense != null) {
        ModalBottomSheet(onDismissRequest = { onEvent(DayDetailEvent.DismissEdit) }) {
            EditSheet(state = state, onEvent = onEvent)
        }
    }

    // Delete confirmation
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
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        fontSize = 11.sp,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun EntryRow(entry: HistoryEntry, isReadOnly: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = Money(entry.amountPaisa).formatTk(), fontSize = 13.sp)
            if (!entry.ref.isNullOrBlank()) {
                Text(text = "${entry.ref} · ${entry.time}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(text = entry.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(text = "+${Money(entry.amountPaisa).formatTk()}", fontSize = 13.sp, color = AppColors.EntryDeltaText)
        if (!isReadOnly) {
            IconButton(onClick = onEdit)   { Icon(Icons.Default.Edit,   contentDescription = "Edit",   tint = AppColors.BalanceTextLight) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ExpenseGroupSection(
    group: ExpenseGrouped,
    isReadOnly: Boolean,
    onEdit: (com.example.chafund.feature.history.domain.model.HistoryExpense) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val chipColor = AppColors.chipColorFor(group.sortOrder - 1)
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
        CategoryChip(label = group.categoryName, chipColor = chipColor)
        group.expenses.forEach { exp ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val note = if (!exp.ref.isNullOrBlank()) "${exp.ref} · ${exp.time}" else exp.time
                    Text(text = note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = Money(exp.amountPaisa).formatTk(), fontSize = 13.sp, color = AppColors.SpentText)
                if (!isReadOnly) {
                    IconButton(onClick = { onEdit(exp) })      { Icon(Icons.Default.Edit,   contentDescription = "Edit",   tint = AppColors.BalanceTextLight) }
                    IconButton(onClick = { onDelete(exp.id) }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

@Composable
private fun EditSheet(state: DayDetailUiState, onEvent: (DayDetailEvent) -> Unit) {
    val label = if (state.editingEntry != null) "Edit entry" else "Edit expense"
    Column(
        modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, fontSize = 17.sp, fontWeight = FontWeight.W500)
        OutlinedTextField(
            value         = state.editAmountInput,
            onValueChange = { onEvent(DayDetailEvent.OnEditAmountChange(it)) },
            label         = { Text("Amount (Tk)") },
            isError       = state.editAmountError != null,
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
        )
        if (state.editAmountError != null) {
            Text(state.editAmountError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
        }
        OutlinedTextField(
            value         = state.editRefInput,
            onValueChange = { onEvent(DayDetailEvent.OnEditRefChange(it)) },
            label         = { Text("Ref note · optional") },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
        )
        PrimaryButton(text = "Save", onClick = { onEvent(DayDetailEvent.SaveEdit) })
        Spacer(Modifier.height(8.dp))
    }
}
