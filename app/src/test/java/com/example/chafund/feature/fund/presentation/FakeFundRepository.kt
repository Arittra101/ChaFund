package com.example.chafund.feature.fund.presentation

import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.fund.domain.FundRepository
import com.example.chafund.feature.fund.domain.model.Month
import com.example.chafund.feature.fund.domain.model.MonthSummary
import com.example.chafund.feature.fund.domain.model.Person
import com.example.chafund.feature.fund.domain.model.TimeCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFundRepository : FundRepository {
    private val _month      = MutableStateFlow<Month?>(null)
    private val _summary    = MutableStateFlow(MonthSummary.empty())
    private val _categories = MutableStateFlow<List<TimeCategory>>(emptyList())
    private val _people     = MutableStateFlow<List<Person>>(emptyList())

    var addEntryResult: Result<Unit, DataError.Local>   = Result.Success(Unit)
    var addExpenseResult: Result<Unit, DataError.Local> = Result.Success(Unit)

    override fun observeCurrentMonth()        = _month      as Flow<Month?>
    override fun observeCurrentMonthSummary() = _summary    as Flow<MonthSummary>
    override fun observeTimeCategories()      = _categories as Flow<List<TimeCategory>>
    override fun observePeople()              = _people     as Flow<List<Person>>

    override suspend fun addEntry(amount: Money, personId: Long, dateEpochDay: Long) = addEntryResult
    override suspend fun addExpense(amount: Money, categoryId: Long, ref: String?, dateEpochDay: Long) =
        addExpenseResult

    fun setMonth(m: Month?)                  { _month.value      = m }
    fun setSummary(s: MonthSummary)          { _summary.value    = s }
    fun setCategories(c: List<TimeCategory>) { _categories.value = c }
    fun setPeople(p: List<Person>)           { _people.value     = p }
}
