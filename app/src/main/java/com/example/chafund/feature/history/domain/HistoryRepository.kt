package com.example.chafund.feature.history.domain

import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.fund.domain.model.Person
import com.example.chafund.feature.history.domain.model.DailySummary
import com.example.chafund.feature.history.domain.model.ExpenseGrouped
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.feature.history.domain.model.HistoryMonth
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observeMonthSummaries(): Flow<List<HistoryMonth>>
    fun observeDailySummaries(monthId: Long, tailStart: Long?, tailEnd: Long): Flow<List<DailySummary>>
    fun observeEntriesForMonth(monthId: Long, tailStart: Long?, tailEnd: Long): Flow<List<HistoryEntry>>
    fun observeEntriesForDay(date: Long): Flow<List<HistoryEntry>>
    fun observeExpensesForDay(date: Long): Flow<List<ExpenseGrouped>>
    fun observePeople(): Flow<List<Person>>

    suspend fun setIncludePrevTail(monthId: Long, include: Boolean): Result<Unit, DataError.Local>
    suspend fun updateEntry(id: Long, amountPaisa: Long, personId: Long?): Result<Unit, DataError.Local>
    suspend fun deleteEntry(id: Long): Result<Unit, DataError.Local>
    suspend fun updateExpense(id: Long, amountPaisa: Long, categoryId: Long, ref: String?): Result<Unit, DataError.Local>
    suspend fun deleteExpense(id: Long): Result<Unit, DataError.Local>
}
