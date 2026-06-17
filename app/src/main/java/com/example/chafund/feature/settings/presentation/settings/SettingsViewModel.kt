package com.example.chafund.feature.settings.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chafund.core.domain.onError
import com.example.chafund.core.domain.onSuccess
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
    val pastMonths: List<HistoryMonth> = emptyList(),
    val categories: List<TimeCategory> = emptyList(),
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
    data class SetTheme(val mode: ThemeMode) : SettingsEvent
    data object SnackbarDismissed : SettingsEvent
}

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeCurrentMonthLabel(),
                repository.observePastMonths(),
                repository.observeTimeCategories(),
                repository.themeMode(),
            ) { label, past, cats, theme ->
                _uiState.value.copy(
                    currentMonthLabel = label,
                    pastMonths        = past,
                    categories        = cats,
                    themeMode         = theme,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ShowDeleteMonthSheet -> _uiState.update { it.copy(showDeleteMonthSheet = true) }
            SettingsEvent.HideDeleteMonthSheet -> _uiState.update { it.copy(showDeleteMonthSheet = false, pendingDeleteMonthId = null) }
            is SettingsEvent.RequestDeleteMonth -> _uiState.update { it.copy(pendingDeleteMonthId = event.id) }
            SettingsEvent.ConfirmDeleteMonth   -> confirmDeleteMonth()
            SettingsEvent.DismissDeleteMonth   -> _uiState.update { it.copy(pendingDeleteMonthId = null) }
            SettingsEvent.ShowAddCategorySheet -> _uiState.update { it.copy(showAddCategorySheet = true, categoryInput = "", categoryError = null) }
            SettingsEvent.HideAddCategorySheet -> _uiState.update { it.copy(showAddCategorySheet = false) }
            is SettingsEvent.OnCategoryInputChange -> _uiState.update { it.copy(categoryInput = event.value, categoryError = null) }
            SettingsEvent.SaveCategory         -> saveCategory()
            is SettingsEvent.EditCategory      -> _uiState.update { it.copy(editingCategoryId = event.id, editingCategoryName = event.currentName, renameInput = event.currentName, renameError = null) }
            SettingsEvent.HideEditCategorySheet -> _uiState.update { it.copy(editingCategoryId = null) }
            is SettingsEvent.OnRenameInputChange -> _uiState.update { it.copy(renameInput = event.value, renameError = null) }
            SettingsEvent.SaveRename           -> saveRename()
            is SettingsEvent.DeleteCategory    -> deleteCategory(event.id)
            is SettingsEvent.SetTheme          -> viewModelScope.launch { repository.setTheme(event.mode) }
            SettingsEvent.SnackbarDismissed    -> _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun confirmDeleteMonth() {
        val id = _uiState.value.pendingDeleteMonthId ?: return
        viewModelScope.launch {
            repository.deletePastMonth(id)
                .onSuccess { _uiState.update { it.copy(pendingDeleteMonthId = null, snackbarMessage = "Month deleted") } }
                .onError   { _uiState.update { it.copy(snackbarMessage = "Cannot delete current month") } }
        }
    }

    private fun saveRename() {
        val id   = _uiState.value.editingCategoryId ?: return
        val name = _uiState.value.renameInput.trim()
        if (name.isBlank()) { _uiState.update { it.copy(renameError = "Name cannot be empty") }; return }
        viewModelScope.launch {
            repository.renameCategory(id, name)
                .onSuccess { _uiState.update { it.copy(editingCategoryId = null, snackbarMessage = "Category renamed") } }
                .onError   { _uiState.update { it.copy(renameError = "Name already exists") } }
        }
    }

    private fun saveCategory() {
        val name = _uiState.value.categoryInput.trim()
        if (name.isBlank()) { _uiState.update { it.copy(categoryError = "Name cannot be empty") }; return }
        viewModelScope.launch {
            repository.addCategory(name)
                .onSuccess { _uiState.update { it.copy(showAddCategorySheet = false, snackbarMessage = "Category added") } }
                .onError   { _uiState.update { it.copy(categoryError = "Name already exists") } }
        }
    }

    private fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
                .onSuccess { _uiState.update { it.copy(snackbarMessage = "Category deleted") } }
                .onError   { _uiState.update { it.copy(snackbarMessage = "Category is in use and cannot be deleted") } }
        }
    }
}
