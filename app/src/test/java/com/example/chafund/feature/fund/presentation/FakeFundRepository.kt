package com.example.chafund.feature.fund.presentation

import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.fund.domain.FundRepository
import com.example.chafund.feature.fund.domain.model.Month
import com.example.chafund.feature.fund.domain.model.MonthSummary
import com.example.chafund.feature.fund.domain.model.TimeCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFundRepository : FundRepository {
    private val _month      = MutableStateFlow<Month?>(null)
    private val _summary    = MutableStateFlow(MonthSummary.empty())
    private val _categories = MutableStateFlow<List<TimeCategory>>(emptyList())

    var addEntryResult: Result<Unit, DataError.Local>   = Result.Success(Unit)
    var addExpenseResult: Result<Unit, DataError.Local> = Result.Success(Unit)

    override fun observeCurrentMonth()        = _month      as Flow<Month?>
    override fun observeCurrentMonthSummary() = _summary    as Flow<MonthSummary>
    override fun observeTimeCategories()      = _categories as Flow<List<TimeCategory>>

    override suspend fun addEntry(amount: Money, ref: String?)                        = addEntryResult
    override suspend fun addExpense(amount: Money, categoryId: Long, ref: String?)    = addExpenseResult

    fun setMonth(m: Month?)                  { _month.value      = m }
    fun setSummary(s: MonthSummary)          { _summary.value    = s }
    fun setCategories(c: List<TimeCategory>) { _categories.value = c }
}
