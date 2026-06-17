package com.example.chafund.feature.fund.domain

import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.fund.domain.model.Month
import com.example.chafund.feature.fund.domain.model.MonthSummary
import com.example.chafund.feature.fund.domain.model.TimeCategory
import kotlinx.coroutines.flow.Flow

interface FundRepository {
    fun observeCurrentMonth(): Flow<Month?>
    fun observeCurrentMonthSummary(): Flow<MonthSummary>
    fun observeTimeCategories(): Flow<List<TimeCategory>>
    suspend fun addEntry(amount: Money, ref: String?): Result<Unit, DataError.Local>
    suspend fun addExpense(amount: Money, categoryId: Long, ref: String?): Result<Unit, DataError.Local>
}
