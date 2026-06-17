package com.example.chafund.feature.history.presentation.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chafund.feature.history.domain.HistoryRepository
import com.example.chafund.feature.history.domain.model.HistoryMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MonthlyHistoryUiState(
    val months: List<HistoryMonth> = emptyList(),
    val isLoading: Boolean = true,
)

class MonthlyHistoryViewModel(
    private val repository: HistoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlyHistoryUiState())
    val uiState: StateFlow<MonthlyHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeMonthSummaries().collect { months ->
                _uiState.value = MonthlyHistoryUiState(months = months, isLoading = false)
            }
        }
    }
}
