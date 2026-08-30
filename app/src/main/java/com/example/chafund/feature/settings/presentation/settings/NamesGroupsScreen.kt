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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import com.example.chafund.core.presentation.components.PrimaryButton
import com.example.chafund.ui.theme.AppColors
import com.example.chafund.ui.theme.ChipColor

@Composable
fun NamesGroupsScreenRoot(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SettingsEvent.SnackbarDismissed)
        }
    }
    NamesGroupsScreen(state, viewModel::onEvent, onBack, snackbarHostState)
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun NamesGroupsScreen(
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Names & Groups", fontSize = 15.sp, fontWeight = FontWeight.W500) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Groups
            Card {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Group, null)
                        Text("Groups", fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.groups.forEachIndexed { index, group ->
                            CategoryChip(
                                label = group.name,
                                chipColor = AppColors.chipColorFor(index),
                                onClick = { onEvent(SettingsEvent.EditGroup(group.id, group.name)) },
                            )
                        }
                        CategoryChip(
                            label = "+ Add",
                            chipColor = ChipColor(
                                fill = MaterialTheme.colorScheme.surfaceVariant,
                                text = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            onClick = { onEvent(SettingsEvent.ShowAddGroupSheet) },
                        )
                    }
                }
            }

            // Names
            Card {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Person, null)
                        Text("Names", fontSize = 14.sp, fontWeight = FontWeight.W500)
                    }
                    if (state.people.isEmpty()) {
                        Text(
                            text = if (state.groups.isEmpty()) "Add a group first, then add names." else "No names yet.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.people.forEach { person ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(onClick = {
                                        onEvent(SettingsEvent.EditPerson(person.id, person.name, person.groupId))
                                    })
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(person.label, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    PrimaryButton(
                        text = "+ Add name",
                        onClick = { onEvent(SettingsEvent.ShowAddPersonSheet) },
                        enabled = state.groups.isNotEmpty(),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Add group sheet
    if (state.showAddGroupSheet) {
        ModalBottomSheet(onDismissRequest = { onEvent(SettingsEvent.HideAddGroupSheet) }) {
            SheetColumn {
                Text("New group", fontSize = 17.sp, fontWeight = FontWeight.W500)
                OutlinedTextField(
                    value = state.groupInput,
                    onValueChange = { onEvent(SettingsEvent.OnGroupInputChange(it)) },
                    label = { Text("Group name") },
                    isError = state.groupError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (state.groupError != null) {
                    Text(state.groupError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
                PrimaryButton(
                    text = "Add",
                    onClick = { onEvent(SettingsEvent.SaveGroup) },
                    enabled = state.groupInput.isNotBlank(),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Rename / delete group sheet
    if (state.editingGroupId != null) {
        ModalBottomSheet(onDismissRequest = { onEvent(SettingsEvent.HideEditGroupSheet) }) {
            SheetColumn {
                Text("Edit \"${state.editingGroupName}\"", fontSize = 17.sp, fontWeight = FontWeight.W500)
                OutlinedTextField(
                    value = state.groupRenameInput,
                    onValueChange = { onEvent(SettingsEvent.OnGroupRenameInputChange(it)) },
                    label = { Text("Group name") },
                    isError = state.groupRenameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (state.groupRenameError != null) {
                    Text(state.groupRenameError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
                PrimaryButton(
                    text = "Rename",
                    onClick = { onEvent(SettingsEvent.SaveGroupRename) },
                    enabled = state.groupRenameInput.isNotBlank(),
                )
                OutlinedButton(
                    onClick = { onEvent(SettingsEvent.DeleteGroup(state.editingGroupId!!)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete group") }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Add / edit person sheet
    if (state.showPersonSheet) {
        ModalBottomSheet(onDismissRequest = { onEvent(SettingsEvent.HidePersonSheet) }) {
            SheetColumn {
                Text(
                    text = if (state.editingPersonId == null) "New name" else "Edit name",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W500,
                )
                OutlinedTextField(
                    value = state.personNameInput,
                    onValueChange = { onEvent(SettingsEvent.OnPersonNameInputChange(it)) },
                    label = { Text("Name") },
                    isError = state.personError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text("Group", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    state.groups.forEachIndexed { index, group ->
                        CategoryChip(
                            label = group.name,
                            chipColor = AppColors.chipColorFor(index),
                            selected = group.id == state.personGroupId,
                            onClick = { onEvent(SettingsEvent.OnPersonGroupSelect(group.id)) },
                        )
                    }
                }
                if (state.personError != null) {
                    Text(state.personError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
                PrimaryButton(
                    text = "Save",
                    onClick = { onEvent(SettingsEvent.SavePerson) },
                    enabled = state.personNameInput.isNotBlank() && state.personGroupId != null,
                )
                if (state.editingPersonId != null) {
                    OutlinedButton(
                        onClick = { onEvent(SettingsEvent.DeletePerson(state.editingPersonId!!)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text("Delete name") }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, AppColors.BorderLight, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) { content() }
}

@Composable
private fun SheetColumn(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}
