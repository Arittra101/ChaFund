package com.example.chafund.feature.history.presentation.daydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.chafund.core.domain.onSuccess
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.HistoryRepository
import com.example.chafund.feature.history.domain.model.ExpenseGrouped
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.feature.history.domain.model.HistoryExpense
import com.example.chafund.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DeleteType { ENTRY, EXPENSE }

data class DayDetailUiState(
    val dateLabel: String = "",
    val dayName: String = "",
    val isReadOnly: Boolean = false,
    val entries: List<HistoryEntry> = emptyList(),
    val groups: List<ExpenseGrouped> = emptyList(),
    val totalSpent: Money = Money.Zero,
    // Edit state
    val editingEntry: HistoryEntry? = null,
    val editingExpense: HistoryExpense? = null,
    val editAmountInput: String = "",
    val editRefInput: String = "",
    val editAmountError: String? = null,
    // Delete state
    val pendingDeleteId: Long? = null,
    val pendingDeleteType: DeleteType? = null,
    val snackbarMessage: String? = null,
)

sealed interface DayDetailEvent {
    data class EditEntry(val entry: HistoryEntry) : DayDetailEvent
    data class EditExpense(val expense: HistoryExpense) : DayDetailEvent
    data class OnEditAmountChange(val value: String) : DayDetailEvent
    data class OnEditRefChange(val value: String) : DayDetailEvent
    data object SaveEdit : DayDetailEvent
    data class RequestDelete(val id: Long, val type: DeleteType) : DayDetailEvent
    data object ConfirmDelete : DayDetailEvent
    data object DismissEdit : DayDetailEvent
    data object DismissDelete : DayDetailEvent
    data object SnackbarDismissed : DayDetailEvent
}

class DayDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HistoryRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.DayDetail>()
    private val monthId = route.monthId
    private val date = route.dateEpoch

    private val _uiState = MutableStateFlow(DayDetailUiState())
    val uiState: StateFlow<DayDetailUiState> = _uiState.asStateFlow()

    init { observe() }

    private fun observe() {
        viewModelScope.launch {
            combine(
                repository.observeEntriesForDay(monthId, date),
                repository.observeExpensesForDay(monthId, date),
            ) { entries, groups ->
                val totalSpent = groups.flatMap { it.expenses }.sumOf { it.amountPaisa }.let { Money(it) }
                val dateLabel  = com.example.chafund.core.utils.DateTimeFormat.formatDate(date)
                val dayName    = com.example.chafund.core.utils.DateTimeFormat.dayName(date)
                _uiState.value.copy(
                    dateLabel  = "$dayName, $dateLabel",
                    dayName    = dayName,
                    entries    = entries,
                    groups     = groups,
                    totalSpent = totalSpent,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onEvent(event: DayDetailEvent) {
        when (event) {
            is DayDetailEvent.EditEntry   -> _uiState.update { it.copy(
                editingEntry    = event.entry,
                editingExpense  = null,
                editAmountInput = Money(event.entry.amountPaisa).paisa.div(100.0).toString(),
                editRefInput    = event.entry.ref ?: "",
            )}
            is DayDetailEvent.EditExpense -> _uiState.update { it.copy(
                editingExpense  = event.expense,
                editingEntry    = null,
                editAmountInput = Money(event.expense.amountPaisa).paisa.div(100.0).toString(),
                editRefInput    = event.expense.ref ?: "",
            )}
            is DayDetailEvent.OnEditAmountChange -> _uiState.update {
                it.copy(editAmountInput = event.value, editAmountError = null)
            }
            is DayDetailEvent.OnEditRefChange -> _uiState.update { it.copy(editRefInput = event.value) }
            DayDetailEvent.SaveEdit    -> saveEdit()
            is DayDetailEvent.RequestDelete -> _uiState.update {
                it.copy(pendingDeleteId = event.id, pendingDeleteType = event.type)
            }
            DayDetailEvent.ConfirmDelete -> confirmDelete()
            DayDetailEvent.DismissEdit   -> _uiState.update { it.copy(editingEntry = null, editingExpense = null) }
            DayDetailEvent.DismissDelete -> _uiState.update { it.copy(pendingDeleteId = null, pendingDeleteType = null) }
            DayDetailEvent.SnackbarDismissed -> _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun saveEdit() {
        val state  = _uiState.value
        val amount = Money.fromTkString(state.editAmountInput)
        if (amount == null || amount.paisa <= 0) {
            _uiState.update { it.copy(editAmountError = "Enter a valid amount") }
            return
        }
        viewModelScope.launch {
            if (state.editingEntry != null) {
                repository.updateEntry(state.editingEntry.id, amount.paisa, state.editRefInput.ifBlank { null })
                    .onSuccess { _uiState.update { it.copy(editingEntry = null, snackbarMessage = "Entry updated") } }
            } else if (state.editingExpense != null) {
                repository.updateExpense(state.editingExpense.id, amount.paisa, state.editingExpense.categoryId, state.editRefInput.ifBlank { null })
                    .onSuccess { _uiState.update { it.copy(editingExpense = null, snackbarMessage = "Expense updated") } }
            }
        }
    }

    private fun confirmDelete() {
        val state = _uiState.value
        val id    = state.pendingDeleteId ?: return
        val type  = state.pendingDeleteType ?: return
        viewModelScope.launch {
            if (type == DeleteType.ENTRY) {
                repository.deleteEntry(id)
                    .onSuccess { _uiState.update { it.copy(pendingDeleteId = null, pendingDeleteType = null, snackbarMessage = "Entry deleted") } }
            } else {
                repository.deleteExpense(id)
                    .onSuccess { _uiState.update { it.copy(pendingDeleteId = null, pendingDeleteType = null, snackbarMessage = "Expense deleted") } }
            }
        }
    }
}
