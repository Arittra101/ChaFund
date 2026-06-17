package com.example.chafund.feature.fund.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chafund.core.domain.onError
import com.example.chafund.core.domain.onSuccess
import com.example.chafund.core.utils.DateTimeFormat
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.fund.domain.FundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(
    private val repository: FundRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        updateTodayHint()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.observeCurrentMonth(),
                repository.observeCurrentMonthSummary(),
                repository.observeTimeCategories(),
            ) { month, summary, categories ->
                val firstCatId = if (categories.isNotEmpty()) categories.first().id else null
                _uiState.value.copy(
                    monthLabel         = month?.label ?: "",
                    balance            = summary.balance,
                    spent              = summary.totalSpent,
                    categories         = categories,
                    selectedCategoryId = _uiState.value.selectedCategoryId
                        ?: if (_uiState.value.addMode == AddMode.EXPENSE) firstCatId else null,
                )
            }.collect { state ->
                _uiState.value = state.copy(saveEnabled = isSaveEnabled(state))
            }
        }
    }

    private fun updateTodayHint() {
        val today = LocalDate.now()
        val hint = "${DateTimeFormat.formatDateShort(today)} · ${DateTimeFormat.dayNameShort(today)} · auto-tracked"
        _uiState.update { it.copy(todayHint = hint) }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnModeChange -> onModeChange(event.mode)
            is HomeUiEvent.OnAmountChange -> onAmountChange(event.value)
            is HomeUiEvent.OnRefChange -> _uiState.update {
                it.copy(refInput = event.value)
            }
            is HomeUiEvent.OnCategorySelect -> _uiState.update {
                it.copy(selectedCategoryId = event.id, categoryError = null,
                    saveEnabled = isSaveEnabled(it.copy(selectedCategoryId = event.id)))
            }
            HomeUiEvent.OnSave -> onSave()
            HomeUiEvent.OnSnackbarDismissed -> _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun onModeChange(mode: AddMode) {
        _uiState.update { state ->
            val firstCatId = if (state.categories.isNotEmpty()) state.categories.first().id else null
            val updated = state.copy(
                addMode            = mode,
                amountError        = null,
                categoryError      = null,
                selectedCategoryId = if (mode == AddMode.EXPENSE) firstCatId else null,
            )
            updated.copy(saveEnabled = isSaveEnabled(updated))
        }
    }

    private fun onAmountChange(value: String) {
        _uiState.update { state ->
            val updated = state.copy(amountInput = value, amountError = null)
            updated.copy(saveEnabled = isSaveEnabled(updated))
        }
    }

    private fun onSave() {
        val state = _uiState.value
        val amount = Money.fromTkString(state.amountInput)

        if (amount == null || amount.paisa <= 0) {
            _uiState.update { it.copy(amountError = "Enter a valid amount") }
            return
        }
        if (state.addMode == AddMode.EXPENSE && state.selectedCategoryId == null) {
            _uiState.update { it.copy(categoryError = "Select a time category") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val result = if (state.addMode == AddMode.ENTRY) {
                repository.addEntry(amount, state.refInput)
            } else {
                repository.addExpense(amount, state.selectedCategoryId!!, state.refInput)
            }

            result
                .onSuccess {
                    val msg = if (state.addMode == AddMode.ENTRY)
                        "Entry saved · +${amount.formatTk()}"
                    else
                        "Expense saved · ${amount.formatTk()}"
                    _uiState.update {
                        it.copy(
                            isSaving      = false,
                            amountInput   = "",
                            refInput      = "",
                            amountError   = null,
                            categoryError = null,
                            saveEnabled   = false,
                            snackbarMessage = msg,
                        )
                    }
                }
                .onError {
                    _uiState.update {
                        it.copy(isSaving = false, snackbarMessage = "Something went wrong")
                    }
                }
        }
    }

    private fun isSaveEnabled(state: HomeUiState): Boolean {
        val amountOk = state.amountInput.toDoubleOrNull()?.let { it > 0 } == true
        return when (state.addMode) {
            AddMode.ENTRY   -> amountOk
            AddMode.EXPENSE -> amountOk && state.selectedCategoryId != null
        }
    }
}
