package com.example.chafund.feature.history.domain

import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.model.DailySummary
import com.example.chafund.feature.history.domain.model.ExpenseGrouped
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.feature.history.domain.model.HistoryMonth
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observeMonthSummaries(): Flow<List<HistoryMonth>>
    fun observeDailySummaries(monthId: Long): Flow<List<DailySummary>>
    fun observeEntriesForDay(monthId: Long, date: Long): Flow<List<HistoryEntry>>
    fun observeExpensesForDay(monthId: Long, date: Long): Flow<List<ExpenseGrouped>>

    suspend fun updateEntry(id: Long, amountPaisa: Long, ref: String?): Result<Unit, DataError.Local>
    suspend fun deleteEntry(id: Long): Result<Unit, DataError.Local>
    suspend fun updateExpense(id: Long, amountPaisa: Long, categoryId: Long, ref: String?): Result<Unit, DataError.Local>
    suspend fun deleteExpense(id: Long): Result<Unit, DataError.Local>
}
