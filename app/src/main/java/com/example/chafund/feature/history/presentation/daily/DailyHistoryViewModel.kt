package com.example.chafund.feature.history.presentation.daily

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.chafund.core.data.session.Session
import com.example.chafund.core.domain.onSuccess
import com.example.chafund.core.utils.DateTimeFormat
import com.example.chafund.core.utils.Money
import com.example.chafund.core.utils.MonthWindow
import com.example.chafund.feature.history.domain.HistoryRepository
import com.example.chafund.feature.history.domain.model.DailySummary
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.navigation.Route
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class DailyHistoryUiState(
    val monthId: Long = 0L,
    val monthLabel: String = "",
    val isReadOnly: Boolean = false,
    val spent: Money = Money.Zero,
    val balance: Money = Money.Zero,
    val days: List<DailySummary> = emptyList(),
    val isLoading: Boolean = true,
    // Cycle (previous-month tail) toggle
    val showCycleToggle: Boolean = false,
    val includePrevTail: Boolean = false,
    val cycleToggleLabel: String = "",
    // Entries list for the month (tail-aware)
    val entries: List<HistoryEntry> = emptyList(),
    val entriesTotal: Money = Money.Zero,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DailyHistoryViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: HistoryRepository,
    private val session: Session,
) : ViewModel() {

    private val routeMonthId: Long? = savedStateHandle.toRoute<Route.DailyHistory>().monthId

    private val _uiState = MutableStateFlow(DailyHistoryUiState())
    val uiState: StateFlow<DailyHistoryUiState> = _uiState.asStateFlow()

    init { observe() }

    private fun observe() {
        viewModelScope.launch {
            // If monthId == 0 (from bottom nav), use session current
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

                    combine(
                        repository.observeDailySummaries(monthId, tailStart, tailEnd),
                        repository.observeEntriesForMonth(monthId, tailStart, tailEnd),
                        session.currentMonthId,
                    ) { days, entries, currentId ->
                        DailyHistoryUiState(
                            monthId = monthId,
                            monthLabel = month?.label ?: "",
                            isReadOnly = monthId != currentId,
                            spent = month?.totalSpent ?: Money.Zero,
                            balance = month?.balance ?: Money.Zero,
                            days = days,
                            isLoading = false,
                            showCycleToggle = month?.cycleStartEpochDay != null,
                            includePrevTail = month?.includePrevTail ?: false,
                            cycleToggleLabel = cycleToggleLabel(month?.cycleStartEpochDay, tailEndOf(month)),
                            entries = entries,
                            entriesTotal = Money(entries.sumOf { it.amountPaisa }),
                        )
                    }
                }
            }.collect { _uiState.value = it }
        }
    }

    fun onToggleIncludePrevTail(include: Boolean) {
        val id = _uiState.value.monthId
        if (id == 0L) return
        viewModelScope.launch { repository.setIncludePrevTail(id, include).onSuccess { } }
    }

    private fun tailEndOf(month: com.example.chafund.feature.history.domain.model.HistoryMonth?): Long =
        month?.let { it.monthFirstEpochDay - 1 } ?: 0L

    private fun cycleToggleLabel(cycleStart: Long?, tailEnd: Long): String {
        if (cycleStart == null) return ""
        return "Include ${DateTimeFormat.formatDateShort(cycleStart)} – ${DateTimeFormat.formatDateShort(tailEnd)}"
    }
}
