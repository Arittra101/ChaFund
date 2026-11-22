package com.example.chafund.feature.settings.presentation.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chafund.core.presentation.components.CategoryChip
import com.example.chafund.core.presentation.components.ConfirmationBottomSheet
import com.example.chafund.core.presentation.components.LockIndicator
import com.example.chafund.core.presentation.components.PrimaryButton
import com.example.chafund.feature.history.domain.model.HistoryMonth
import com.example.chafund.ui.theme.AppColors

@Composable
fun SettingsScreenRoot(
    viewModel: SettingsViewModel,
    onOpenNamesGroups: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SettingsEvent.SnackbarDismissed)
        }
    }
    SettingsScreen(state, viewModel::onEvent, snackbarHostState, onOpenNamesGroups)
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onOpenNamesGroups: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Settings",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W500
                )
            })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Card 1 — Month
            SettingsCard {
              Column {
                ListItem(
                    headlineContent = {
                        Text(
                            "Current month",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )
                    },
                    supportingContent = {
                        Text(
                            "Auto-set from calendar · ${state.currentMonthLabel}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingContent = { Icon(Icons.Default.CalendarMonth, null) },
                    trailingContent = { LockIndicator() },
                )
                HorizontalDivider(thickness = 0.5.dp)
                val carry = state.carry
                ListItem(
                    headlineContent = {
                        Text("Bring last month's balance", fontSize = 14.sp, fontWeight = FontWeight.W500)
                    },
                    supportingContent = {
                        Text(
                            text = when {
                                carry.alreadyCarried -> "Added to this month"
                                !carry.hasPreviousMonth -> "No previous month yet"
                                carry.canCarry ->
                                    "Add ${carry.previousMonthName}'s balance · +${carry.previousBalance.formatTk()}"
                                else -> "${carry.previousMonthName}'s balance is not positive"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingContent = { Icon(Icons.Default.ArrowDownward, null) },
                    trailingContent = {
                        if (carry.alreadyCarried) {
                            Text("Done ✓", fontSize = 12.sp, color = AppColors.BalanceTextLight)
                        } else {
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    },
                    modifier = if (carry.canCarry) {
                        Modifier.combinedClickable(onClick = { onEvent(SettingsEvent.CarryLastMonthBalance) })
                    } else {
                        Modifier
                    },
                )
                HorizontalDivider(thickness = 0.5.dp)
                ListItem(
                    headlineContent = {
                        Text(
                            "Delete month",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                    modifier = Modifier.combinedClickable(onClick = { onEvent(SettingsEvent.ShowDeleteMonthSheet) }),
                )
              }
            }

            // Card 2 — Time categories
            SettingsCard {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null)
                        Text("Time categories", fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.categories.forEachIndexed { index, cat ->
                            CategoryChip(
                                label = cat.name,
                                chipColor = AppColors.chipColorFor(index),
                                onClick = { onEvent(SettingsEvent.EditCategory(cat.id, cat.name)) },
                            )
                        }
                        // "+ Add" chip
                        CategoryChip(
                            label = "+ Add",
                            chipColor = com.example.chafund.ui.theme.ChipColor(
                                fill = MaterialTheme.colorScheme.surfaceVariant,
                                text = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            onClick = { onEvent(SettingsEvent.ShowAddCategorySheet) },
                        )
                    }
                }
            }

            // Card — Payment methods
            SettingsCard {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, null)
                        Text("Payment methods", fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                    Text(
                        "Picked on every entry and expense. Cash is the default.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.paymentMethods.forEachIndexed { index, method ->
                            CategoryChip(
                                label = method.name,
                                chipColor = AppColors.chipColorFor(index),
                                onClick = { onEvent(SettingsEvent.EditMethod(method.id, method.name)) },
                            )
                        }
                        CategoryChip(
                            label = "+ Add",
                            chipColor = com.example.chafund.ui.theme.ChipColor(
                                fill = MaterialTheme.colorScheme.surfaceVariant,
                                text = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            onClick = { onEvent(SettingsEvent.ShowAddMethodSheet) },
                        )
                    }
                }
            }

            // Card — Groups (preview → opens Names & Groups screen)
            SettingsCard {
                Column(
                    modifier = Modifier
                        .combinedClickable(onClick = onOpenNamesGroups)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Group, null)
                        Text("Groups", fontSize = 14.sp, fontWeight = FontWeight.W500, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (state.groups.isEmpty()) {
                        Text("No groups yet.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            state.groups.take(3).forEachIndexed { index, group ->
                                CategoryChip(label = group.name, chipColor = AppColors.chipColorFor(index))
                            }
                            if (state.groups.size > 3) {
                                Text(
                                    "+${state.groups.size - 3} more",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 5.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Card — Names (preview → opens Names & Groups screen)
            SettingsCard {
                Column(
                    modifier = Modifier
                        .combinedClickable(onClick = onOpenNamesGroups)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Person, null)
                        Text("Names", fontSize = 14.sp, fontWeight = FontWeight.W500, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (state.people.isEmpty()) {
                        Text(
                            text = if (state.groups.isEmpty()) "Add a group first, then add names." else "No names yet.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.people.take(3).forEach { person ->
                            Text(person.label, fontSize = 14.sp)
                        }
                        if (state.people.size > 3) {
                            Text(
                                "+${state.people.size - 3} more",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Footnote
            Text(
                text = "New months are created automatically each month.\nPast months stay in history.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    // Delete month bottom sheet
    if (state.showDeleteMonthSheet) {
        DeleteMonthSheet(
            months = state.pastMonths,
            onDelete = { onEvent(SettingsEvent.RequestDeleteMonth(it)) },
            onDismiss = { onEvent(SettingsEvent.HideDeleteMonthSheet) },
        )
    }

    // Delete month confirm dialog
    if (state.pendingDeleteMonthId != null) {
        val month = state.pastMonths.find { it.id == state.pendingDeleteMonthId }
        ConfirmationBottomSheet(
            title = "Delete ${month?.label}?",
            body = "All entries and expenses in this month will be permanently removed. This can't be undone.",
            onConfirm = { onEvent(SettingsEvent.ConfirmDeleteMonth) },
            onDismiss = { onEvent(SettingsEvent.DismissDeleteMonth) },
        )
    }

    // Rename / delete category sheet
    if (state.editingCategoryId != null) {
        ModalBottomSheet(onDismissRequest = { onEvent(SettingsEvent.HideEditCategorySheet) }) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Edit \"${state.editingCategoryName}\"",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W500
                )
                OutlinedTextField(
                    value = state.renameInput,
                    onValueChange = { onEvent(SettingsEvent.OnRenameInputChange(it)) },
                    label = { Text("Category name") },
                    isError = state.renameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (state.renameError != null) {
                    Text(
                        state.renameError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }
                PrimaryButton(
                    text = "Rename",
                    onClick = { onEvent(SettingsEvent.SaveRename) },
                    enabled = state.renameInput.isNotBlank()
                )
                androidx.compose.material3.OutlinedButton(
                    onClick = { onEvent(SettingsEvent.DeleteCategory(state.editingCategoryId!!)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                ) { Text("Delete category") }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Rename / delete payment method sheet
    if (state.editingMethodId != null) {
        ModalBottomSheet(onDismissRequest = { onEvent(SettingsEvent.HideEditMethodSheet) }) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Edit \"${state.editingMethodName}\"",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W500
                )
                OutlinedTextField(
                    value = state.methodRenameInput,
                    onValueChange = { onEvent(SettingsEvent.OnMethodRenameInputChange(it)) },
                    label = { Text("Payment method") },
                    isError = state.methodRenameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (state.methodRenameError != null) {
                    Text(
                        state.methodRenameError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }
                PrimaryButton(
                    text = "Rename",
                    onClick = { onEvent(SettingsEvent.SaveMethodRename) },
                    enabled = state.methodRenameInput.isNotBlank(),
                )
                androidx.compose.material3.OutlinedButton(
                    onClick = { onEvent(SettingsEvent.DeleteMethod(state.editingMethodId!!)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                ) { Text("Delete payment method") }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Add payment method sheet
    if (state.showAddMethodSheet) {
        ModalBottomSheet(onDismissRequest = { onEvent(SettingsEvent.HideAddMethodSheet) }) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("New payment method", fontSize = 17.sp, fontWeight = FontWeight.W500)
                OutlinedTextField(
                    value = state.methodInput,
                    onValueChange = { onEvent(SettingsEvent.OnMethodInputChange(it)) },
                    label = { Text("Payment method") },
                    isError = state.methodError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (state.methodError != null) {
                    Text(
                        state.methodError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }
                PrimaryButton(
                    text = "Add",
                    onClick = { onEvent(SettingsEvent.SaveMethod) },
                    enabled = state.methodInput.isNotBlank(),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Add category sheet
    if (state.showAddCategorySheet) {
        ModalBottomSheet(onDismissRequest = { onEvent(SettingsEvent.HideAddCategorySheet) }) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("New time category", fontSize = 17.sp, fontWeight = FontWeight.W500)
                OutlinedTextField(
                    value = state.categoryInput,
                    onValueChange = { onEvent(SettingsEvent.OnCategoryInputChange(it)) },
                    label = { Text("Category name") },
                    isError = state.categoryError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (state.categoryError != null) {
                    Text(
                        state.categoryError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }
                PrimaryButton(
                    text = "Add",
                    onClick = { onEvent(SettingsEvent.SaveCategory) },
                    enabled = state.categoryInput.isNotBlank(),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteMonthSheet(
    months: List<HistoryMonth>,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Delete a month",
                fontSize = 17.sp,
                fontWeight = FontWeight.W500,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Past months only — the current month can't be deleted.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (months.isEmpty()) {
                Text(
                    text = "No past months to delete yet.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp),
                )
            } else {
                val shape = RoundedCornerShape(14.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(0.5.dp, AppColors.BorderLight, shape)
                        // Cap the list so a long history still leaves the sheet usable.
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    shape = shape,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column {
                        months.forEachIndexed { index, month ->
                            MonthDeleteRow(month = month, onDelete = { onDelete(month.id) })
                            if (index < months.lastIndex) HorizontalDivider(thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDeleteRow(month: HistoryMonth, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 4.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(text = month.label, fontSize = 14.sp, fontWeight = FontWeight.W500)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Entry ${month.totalEntries.formatTk()}",
                    fontSize = 11.sp,
                    color = AppColors.BalanceTextLight,
                )
                Text(
                    text = "Spent ${month.totalSpent.formatTk()}",
                    fontSize = 11.sp,
                    color = AppColors.SpentText,
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete ${month.label}",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, AppColors.BorderLight, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) { content() }
}
