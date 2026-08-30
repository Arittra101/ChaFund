package com.example.chafund.feature.settings.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chafund.core.domain.onError
import com.example.chafund.core.domain.onSuccess
import com.example.chafund.core.utils.MonthWindow
import com.example.chafund.feature.fund.domain.model.Group
import com.example.chafund.feature.fund.domain.model.Month
import com.example.chafund.feature.fund.domain.model.Person
import com.example.chafund.feature.fund.domain.model.TimeCategory
import com.example.chafund.feature.history.domain.model.HistoryMonth
import com.example.chafund.feature.settings.domain.SettingsRepository
import com.example.chafund.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currentMonthLabel: String = "",
    val currentMonth: Month? = null,
    val showCyclePicker: Boolean = false,
    val pastMonths: List<HistoryMonth> = emptyList(),
    val categories: List<TimeCategory> = emptyList(),
    val groups: List<Group> = emptyList(),
    val people: List<Person> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Delete month sheet
    val showDeleteMonthSheet: Boolean = false,
    val pendingDeleteMonthId: Long? = null,
    // Add category sheet
    val showAddCategorySheet: Boolean = false,
    val categoryInput: String = "",
    val categoryError: String? = null,
    // Rename/delete category sheet
    val editingCategoryId: Long? = null,
    val editingCategoryName: String = "",
    val renameInput: String = "",
    val renameError: String? = null,
    // Add group sheet
    val showAddGroupSheet: Boolean = false,
    val groupInput: String = "",
    val groupError: String? = null,
    // Rename/delete group sheet
    val editingGroupId: Long? = null,
    val editingGroupName: String = "",
    val groupRenameInput: String = "",
    val groupRenameError: String? = null,
    // Add/edit person sheet
    val showPersonSheet: Boolean = false,
    val editingPersonId: Long? = null,
    val personNameInput: String = "",
    val personGroupId: Long? = null,
    val personError: String? = null,
    val snackbarMessage: String? = null,
)

sealed interface SettingsEvent {
    data object ShowDeleteMonthSheet : SettingsEvent
    data object HideDeleteMonthSheet : SettingsEvent
    data class RequestDeleteMonth(val id: Long) : SettingsEvent
    data object ConfirmDeleteMonth : SettingsEvent
    data object DismissDeleteMonth : SettingsEvent
    data object ShowAddCategorySheet : SettingsEvent
    data object HideAddCategorySheet : SettingsEvent
    data class OnCategoryInputChange(val value: String) : SettingsEvent
    data object SaveCategory : SettingsEvent
    data class EditCategory(val id: Long, val currentName: String) : SettingsEvent
    data object HideEditCategorySheet : SettingsEvent
    data class OnRenameInputChange(val value: String) : SettingsEvent
    data object SaveRename : SettingsEvent
    data class DeleteCategory(val id: Long) : SettingsEvent

    // Groups
    data object ShowAddGroupSheet : SettingsEvent
    data object HideAddGroupSheet : SettingsEvent
    data class OnGroupInputChange(val value: String) : SettingsEvent
    data object SaveGroup : SettingsEvent
    data class EditGroup(val id: Long, val currentName: String) : SettingsEvent
    data object HideEditGroupSheet : SettingsEvent
    data class OnGroupRenameInputChange(val value: String) : SettingsEvent
    data object SaveGroupRename : SettingsEvent
    data class DeleteGroup(val id: Long) : SettingsEvent

    // Names / people
    data object ShowAddPersonSheet : SettingsEvent
    data class EditPerson(val id: Long, val name: String, val groupId: Long) : SettingsEvent
    data object HidePersonSheet : SettingsEvent
    data class OnPersonNameInputChange(val value: String) : SettingsEvent
    data class OnPersonGroupSelect(val id: Long) : SettingsEvent
    data object SavePerson : SettingsEvent
    data class DeletePerson(val id: Long) : SettingsEvent

    // Cycle start (previous-month tail)
    data object ShowCyclePicker : SettingsEvent
    data object HideCyclePicker : SettingsEvent
    data class SetCycleStart(val epochDay: Long) : SettingsEvent
    data object ClearCycleStart : SettingsEvent

    data class SetTheme(val mode: ThemeMode) : SettingsEvent
    data object SnackbarDismissed : SettingsEvent
}

private data class LoadedData(
    val month: Month?,
    val past: List<HistoryMonth>,
    val cats: List<TimeCategory>,
    val groups: List<Group>,
    val people: List<Person>,
)

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeCurrentMonth(),
                repository.observePastMonths(),
                repository.observeTimeCategories(),
                repository.observeGroups(),
                repository.observePeople(),
            ) { month, past, cats, groups, people ->
                LoadedData(month, past, cats, groups, people)
            }.combine(repository.themeMode()) { data, theme ->
                _uiState.value.copy(
                    currentMonthLabel = data.month?.label ?: "",
                    currentMonth = data.month,
                    pastMonths = data.past,
                    categories = data.cats,
                    groups = data.groups,
                    people = data.people,
                    themeMode = theme,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ShowDeleteMonthSheet -> _uiState.update { it.copy(showDeleteMonthSheet = true) }
            SettingsEvent.HideDeleteMonthSheet -> _uiState.update {
                it.copy(
                    showDeleteMonthSheet = false,
                    pendingDeleteMonthId = null
                )
            }

            is SettingsEvent.RequestDeleteMonth -> _uiState.update { it.copy(pendingDeleteMonthId = event.id) }
            SettingsEvent.ConfirmDeleteMonth -> confirmDeleteMonth()
            SettingsEvent.DismissDeleteMonth -> _uiState.update { it.copy(pendingDeleteMonthId = null) }
            SettingsEvent.ShowAddCategorySheet -> _uiState.update {
                it.copy(
                    showAddCategorySheet = true,
                    categoryInput = "",
                    categoryError = null
                )
            }

            SettingsEvent.HideAddCategorySheet -> _uiState.update { it.copy(showAddCategorySheet = false) }
            is SettingsEvent.OnCategoryInputChange -> _uiState.update {
                it.copy(
                    categoryInput = event.value,
                    categoryError = null
                )
            }

            SettingsEvent.SaveCategory -> saveCategory()
            is SettingsEvent.EditCategory -> _uiState.update {
                it.copy(
                    editingCategoryId = event.id,
                    editingCategoryName = event.currentName,
                    renameInput = event.currentName,
                    renameError = null
                )
            }

            SettingsEvent.HideEditCategorySheet -> _uiState.update { it.copy(editingCategoryId = null) }
            is SettingsEvent.OnRenameInputChange -> _uiState.update {
                it.copy(
                    renameInput = event.value,
                    renameError = null
                )
            }

            SettingsEvent.SaveRename -> saveRename()
            is SettingsEvent.DeleteCategory -> deleteCategory(event.id)

            SettingsEvent.ShowAddGroupSheet -> _uiState.update {
                it.copy(
                    showAddGroupSheet = true,
                    groupInput = "",
                    groupError = null
                )
            }

            SettingsEvent.HideAddGroupSheet -> _uiState.update { it.copy(showAddGroupSheet = false) }
            is SettingsEvent.OnGroupInputChange -> _uiState.update {
                it.copy(
                    groupInput = event.value,
                    groupError = null
                )
            }

            SettingsEvent.SaveGroup -> saveGroup()
            is SettingsEvent.EditGroup -> _uiState.update {
                it.copy(
                    editingGroupId = event.id,
                    editingGroupName = event.currentName,
                    groupRenameInput = event.currentName,
                    groupRenameError = null
                )
            }

            SettingsEvent.HideEditGroupSheet -> _uiState.update { it.copy(editingGroupId = null) }
            is SettingsEvent.OnGroupRenameInputChange -> _uiState.update {
                it.copy(
                    groupRenameInput = event.value,
                    groupRenameError = null
                )
            }

            SettingsEvent.SaveGroupRename -> saveGroupRename()
            is SettingsEvent.DeleteGroup -> deleteGroup(event.id)

            SettingsEvent.ShowAddPersonSheet -> _uiState.update {
                it.copy(
                    showPersonSheet = true,
                    editingPersonId = null,
                    personNameInput = "",
                    personGroupId = it.groups.firstOrNull()?.id,
                    personError = null
                )
            }

            is SettingsEvent.EditPerson -> _uiState.update {
                it.copy(
                    showPersonSheet = true,
                    editingPersonId = event.id,
                    personNameInput = event.name,
                    personGroupId = event.groupId,
                    personError = null
                )
            }

            SettingsEvent.HidePersonSheet -> _uiState.update { it.copy(showPersonSheet = false) }
            is SettingsEvent.OnPersonNameInputChange -> _uiState.update {
                it.copy(
                    personNameInput = event.value,
                    personError = null
                )
            }

            is SettingsEvent.OnPersonGroupSelect -> _uiState.update {
                it.copy(
                    personGroupId = event.id,
                    personError = null
                )
            }

            SettingsEvent.SavePerson -> savePerson()
            is SettingsEvent.DeletePerson -> deletePerson(event.id)

            SettingsEvent.ShowCyclePicker -> _uiState.update { it.copy(showCyclePicker = true) }
            SettingsEvent.HideCyclePicker -> _uiState.update { it.copy(showCyclePicker = false) }
            is SettingsEvent.SetCycleStart -> setCycleStart(event.epochDay)
            SettingsEvent.ClearCycleStart -> clearCycleStart()

            is SettingsEvent.SetTheme -> viewModelScope.launch { repository.setTheme(event.mode) }
            SettingsEvent.SnackbarDismissed -> _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun setCycleStart(epochDay: Long) {
        val monthId = _uiState.value.currentMonth?.id ?: return
        viewModelScope.launch {
            repository.setCycleStart(monthId, epochDay)
                .onSuccess { _uiState.update { it.copy(showCyclePicker = false, snackbarMessage = "Cycle start set") } }
                .onError   { _uiState.update { it.copy(showCyclePicker = false, snackbarMessage = "Could not set cycle start") } }
        }
    }

    private fun clearCycleStart() {
        val monthId = _uiState.value.currentMonth?.id ?: return
        viewModelScope.launch {
            repository.clearCycleStart(monthId)
                .onSuccess { _uiState.update { it.copy(showCyclePicker = false, snackbarMessage = "Cycle start cleared") } }
                .onError   { _uiState.update { it.copy(showCyclePicker = false, snackbarMessage = "Could not clear cycle start") } }
        }
    }

    private fun confirmDeleteMonth() {
        val id = _uiState.value.pendingDeleteMonthId ?: return
        viewModelScope.launch {
            repository.deletePastMonth(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            pendingDeleteMonthId = null,
                            snackbarMessage = "Month deleted"
                        )
                    }
                }
                .onError { _uiState.update { it.copy(snackbarMessage = "Cannot delete current month") } }
        }
    }

    private fun saveRename() {
        val id = _uiState.value.editingCategoryId ?: return
        val name = _uiState.value.renameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(renameError = "Name cannot be empty") }; return
        }
        viewModelScope.launch {
            repository.renameCategory(id, name)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            editingCategoryId = null,
                            snackbarMessage = "Category renamed"
                        )
                    }
                }
                .onError { _uiState.update { it.copy(renameError = "Name already exists") } }
        }
    }

    private fun saveCategory() {
        val name = _uiState.value.categoryInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(categoryError = "Name cannot be empty") }; return
        }
        viewModelScope.launch {
            repository.addCategory(name)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showAddCategorySheet = false,
                            snackbarMessage = "Category added"
                        )
                    }
                }
                .onError { _uiState.update { it.copy(categoryError = "Name already exists") } }
        }
    }

    private fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            editingCategoryId = null,
                            snackbarMessage = "Category deleted"
                        )
                    }
                }
                .onError { _uiState.update { it.copy(snackbarMessage = "Category is in use and cannot be deleted") } }
        }
    }

    private fun saveGroup() {
        val name = _uiState.value.groupInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(groupError = "Name cannot be empty") }; return
        }
        viewModelScope.launch {
            repository.addGroup(name)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showAddGroupSheet = false,
                            snackbarMessage = "Group added"
                        )
                    }
                }
                .onError { _uiState.update { it.copy(groupError = "Group already exists") } }
        }
    }

    private fun saveGroupRename() {
        val id = _uiState.value.editingGroupId ?: return
        val name = _uiState.value.groupRenameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(groupRenameError = "Name cannot be empty") }; return
        }
        viewModelScope.launch {
            repository.renameGroup(id, name)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            editingGroupId = null,
                            snackbarMessage = "Group renamed"
                        )
                    }
                }
                .onError { _uiState.update { it.copy(groupRenameError = "Name already exists") } }
        }
    }

    private fun deleteGroup(id: Long) {
        viewModelScope.launch {
            repository.deleteGroup(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            editingGroupId = null,
                            snackbarMessage = "Group deleted"
                        )
                    }
                }
                .onError { _uiState.update { it.copy(snackbarMessage = "Group has names and cannot be deleted") } }
        }
    }

    private fun savePerson() {
        val state = _uiState.value
        val name = state.personNameInput.trim()
        val groupId = state.personGroupId
        if (name.isBlank()) {
            _uiState.update { it.copy(personError = "Name cannot be empty") }; return
        }
        if (groupId == null) {
            _uiState.update { it.copy(personError = "Select a group") }; return
        }
        viewModelScope.launch {
            val result = if (state.editingPersonId == null) {
                repository.addPerson(name, groupId)
            } else {
                repository.updatePerson(state.editingPersonId, name, groupId)
            }
            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showPersonSheet = false,
                            snackbarMessage = "Name saved"
                        )
                    }
                }
                .onError { _uiState.update { it.copy(personError = "Name already exists in this group") } }
        }
    }

    private fun deletePerson(id: Long) {
        viewModelScope.launch {
            repository.deletePerson(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showPersonSheet = false,
                            snackbarMessage = "Name deleted"
                        )
                    }
                }
                .onError { _uiState.update { it.copy(snackbarMessage = "Could not delete name") } }
        }
    }
}
