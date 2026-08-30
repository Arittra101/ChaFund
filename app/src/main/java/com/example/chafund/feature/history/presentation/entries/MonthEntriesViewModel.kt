package com.example.chafund.feature.history.presentation.entries

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.chafund.core.data.session.Session
import com.example.chafund.core.utils.Money
import com.example.chafund.core.utils.MonthWindow
import com.example.chafund.feature.history.domain.HistoryRepository
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.navigation.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class MonthEntriesUiState(
    val monthLabel: String = "",
    val entries: List<HistoryEntry> = emptyList(),
    val entriesTotal: Money = Money.Zero,
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MonthEntriesViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HistoryRepository,
    private val session: Session,
) : ViewModel() {

    private val routeMonthId: Long? = savedStateHandle.toRoute<Route.MonthEntries>().monthId

    private val _uiState = MutableStateFlow(MonthEntriesUiState())
    val uiState: StateFlow<MonthEntriesUiState> = _uiState.asStateFlow()

    init { observe() }

    private fun observe() {
        viewModelScope.launch {
            val effectiveIdFlow = if (routeMonthId != 0L) flowOf(routeMonthId)
            else session.currentMonthId

            effectiveIdFlow.flatMapLatest { rawId ->
                repository.observeMonthSummaries().flatMapLatest { months ->
                    val monthId = rawId ?: session.currentMonthId.value
                    val month = months.find { it.id == monthId }
                    val tailStart = MonthWindow.tailStart(
                        month?.cycleStartEpochDay,
                        month?.includePrevTail ?: false,
                    )
                    val tailEnd = month?.let { it.monthFirstEpochDay - 1 } ?: 0L

                    repository.observeEntriesForMonth(monthId, tailStart, tailEnd).map { entries ->
                        MonthEntriesUiState(
                            monthLabel = month?.label ?: "",
                            entries = entries,
                            entriesTotal = Money(entries.sumOf { it.amountPaisa }),
                            isLoading = false,
                        )
                    }
                }
            }.collect { _uiState.value = it }
        }
    }
}
