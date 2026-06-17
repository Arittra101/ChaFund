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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
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
import com.example.chafund.core.presentation.components.SegmentedToggle
import com.example.chafund.feature.history.domain.model.HistoryMonth
import com.example.chafund.ui.theme.AppColors
import com.example.chafund.ui.theme.ThemeMode

@Composable
fun SettingsScreenRoot(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SettingsEvent.SnackbarDismissed)
        }
    }
    SettingsScreen(state, viewModel::onEvent, snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings", fontSize = 15.sp, fontWeight = FontWeight.W500) }) },
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
                ListItem(
                    headlineContent = { Text("Current month", fontSize = 14.sp, fontWeight = FontWeight.W500) },
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
                ListItem(
                    headlineContent = { Text("Delete month", fontSize = 14.sp, color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                    modifier = Modifier.combinedClickable(onClick = { onEvent(SettingsEvent.ShowDeleteMonthSheet) }),
                )
            }

            // Card 2 — Time categories
            SettingsCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CalendarMonth, null)
                        Text("Time categories", fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(6.dp),
                    ) {
                        state.categories.forEachIndexed { index, cat ->
                            CategoryChip(
                                label     = cat.name,
                                chipColor = AppColors.chipColorFor(index),
                                onClick   = { onEvent(SettingsEvent.EditCategory(cat.id, cat.name)) },
                            )
                        }
                        // "+ Add" chip
                        CategoryChip(
                            label     = "+ Add",
                            chipColor = com.example.chafund.ui.theme.ChipColor(
                                fill = MaterialTheme.colorScheme.surfaceVariant,
                                text = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            onClick = { onEvent(SettingsEvent.ShowAddCategorySheet) },
                        )
                    }
                }
            }

            // Card 3 — Theme
            SettingsCard {
                ListItem(
                    headlineContent = { Text("Theme", fontSize = 14.sp, fontWeight = FontWeight.W500) },
                    leadingContent  = { Icon(Icons.Default.DarkMode, null) },
                    trailingContent = {
                        SegmentedToggle(
                            options       = listOf("Light", "Dark", "System"),
                            selectedIndex = when (state.themeMode) {
                                ThemeMode.LIGHT  -> 0
                                ThemeMode.DARK   -> 1
                                ThemeMode.SYSTEM -> 2
                            },
                            onSelect = { idx ->
                                onEvent(SettingsEvent.SetTheme(when (idx) {
                                    0 -> ThemeMode.LIGHT
                                    1 -> ThemeMode.DARK
                                    else -> ThemeMode.SYSTEM
                                }))
                            },
                            modifier = Modifier.padding(end = 0.dp).fillMaxWidth(0.55f),
                        )
                    },
                )
            }

            // Footnote
            Text(
                text      = "New months are created automatically each month.\nPast months stay in history.",
                fontSize  = 10.sp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    // Delete month bottom sheet
    if (state.showDeleteMonthSheet) {
        DeleteMonthSheet(
            months    = state.pastMonths,
            onDelete  = { onEvent(SettingsEvent.RequestDeleteMonth(it)) },
            onDismiss = { onEvent(SettingsEvent.HideDeleteMonthSheet) },
        )
    }

    // Delete month confirm dialog
    if (state.pendingDeleteMonthId != null) {
        val month = state.pastMonths.find { it.id == state.pendingDeleteMonthId }
        ConfirmationBottomSheet(
            title     = "Delete ${month?.label}?",
            body      = "All entries and expenses in this month will be permanently removed. This can't be undone.",
            onConfirm = { onEvent(SettingsEvent.ConfirmDeleteMonth) },
            onDismiss = { onEvent(SettingsEvent.DismissDeleteMonth) },
        )
    }

    // Rename / delete category sheet
    if (state.editingCategoryId != null) {
        ModalBottomSheet(onDismissRequest = { onEvent(SettingsEvent.HideEditCategorySheet) }) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Edit \"${state.editingCategoryName}\"", fontSize = 17.sp, fontWeight = FontWeight.W500)
                OutlinedTextField(
                    value         = state.renameInput,
                    onValueChange = { onEvent(SettingsEvent.OnRenameInputChange(it)) },
                    label         = { Text("Category name") },
                    isError       = state.renameError != null,
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                )
                if (state.renameError != null) {
                    Text(state.renameError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
                PrimaryButton(text = "Rename", onClick = { onEvent(SettingsEvent.SaveRename) }, enabled = state.renameInput.isNotBlank())
                androidx.compose.material3.OutlinedButton(
                    onClick  = { onEvent(SettingsEvent.DeleteCategory(state.editingCategoryId!!)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete category") }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Add category sheet
    if (state.showAddCategorySheet) {
        ModalBottomSheet(onDismissRequest = { onEvent(SettingsEvent.HideAddCategorySheet) }) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("New time category", fontSize = 17.sp, fontWeight = FontWeight.W500)
                OutlinedTextField(
                    value         = state.categoryInput,
                    onValueChange = { onEvent(SettingsEvent.OnCategoryInputChange(it)) },
                    label         = { Text("Category name") },
                    isError       = state.categoryError != null,
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                )
                if (state.categoryError != null) {
                    Text(state.categoryError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
                PrimaryButton(
                    text    = "Add",
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text     = "Delete a month",
                fontSize = 17.sp,
                fontWeight = FontWeight.W500,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            Text(
                text     = "Past months only. Current month can't be deleted.",
                fontSize = 12.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(8.dp))
            if (months.isEmpty()) {
                Text(
                    text     = "No past months to delete yet.",
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                )
            } else {
                months.forEach { month ->
                    ListItem(
                        headlineContent = { Text(month.label, fontSize = 14.sp) },
                        supportingContent = {
                            Text(
                                "Entry ${month.totalEntries.formatTk()} · Spent ${month.totalSpent.formatTk()}",
                                fontSize = 11.sp,
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { onDelete(month.id) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        },
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
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
