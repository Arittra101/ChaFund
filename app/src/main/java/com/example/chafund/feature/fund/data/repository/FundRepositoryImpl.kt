package com.example.chafund.feature.fund.data.repository

import android.database.sqlite.SQLiteException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.chafund.core.data.database.dao.EntryDao
import com.example.chafund.core.data.database.dao.ExpenseDao
import com.example.chafund.core.data.database.dao.MonthDao
import com.example.chafund.core.data.database.dao.PersonDao
import com.example.chafund.core.data.database.dao.TimeCategoryDao
import com.example.chafund.core.data.database.entity.EntryEntity
import com.example.chafund.core.data.database.entity.ExpenseEntity
import com.example.chafund.core.data.session.Session
import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.core.domain.DispatcherProvider
import com.example.chafund.core.utils.DateTimeFormat
import com.example.chafund.core.utils.Money
import com.example.chafund.core.utils.MonthWindow
import com.example.chafund.feature.fund.data.mapper.toDomain
import com.example.chafund.feature.fund.domain.FundRepository
import com.example.chafund.feature.fund.domain.model.Month
import com.example.chafund.feature.fund.domain.model.MonthSummary
import com.example.chafund.feature.fund.domain.model.Person
import com.example.chafund.feature.fund.domain.model.TimeCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class FundRepositoryImpl(
    private val monthDao: MonthDao,
    private val entryDao: EntryDao,
    private val expenseDao: ExpenseDao,
    private val categoryDao: TimeCategoryDao,
    private val personDao: PersonDao,
    private val session: Session,
    private val dispatchers: DispatcherProvider,
) : FundRepository {

    override fun observeCurrentMonth(): Flow<Month?> =
        monthDao.observeCurrent().map { it?.toDomain() }

    override fun observeCurrentMonthSummary(): Flow<MonthSummary> =
        monthDao.observeCurrent().flatMapLatest { month ->
            if (month == null) {
                flowOf(MonthSummary.empty())
            } else {
                val tailStart = MonthWindow.tailStart(month.cycleStartEpochDay, month.includePrevTail)
                val tailEnd = MonthWindow.tailEndFor(month.year, month.month)
                combine(
                    entryDao.sumByMonthWithTail(month.id, tailStart, tailEnd),
                    expenseDao.sumByMonthWithTail(month.id, tailStart, tailEnd),
                ) { entrySum, expenseSum ->
                    MonthSummary(
                        monthId = month.id,
                        totalEntries = Money(entrySum),
                        totalSpent = Money(expenseSum),
                        balance = Money(entrySum - expenseSum),
                    )
                }
            }
        }

    override fun observeTimeCategories(): Flow<List<TimeCategory>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observePeople(): Flow<List<Person>> =
        personDao.observeAllWithGroup().map { list -> list.map { it.toDomain() } }

    override suspend fun addEntry(
        amount: Money,
        personId: Long,
    ): Result<Unit, DataError.Local> = withContext(dispatchers.io) {
        try {
            val now = System.currentTimeMillis()
            val monthId = session.currentMonthId.value
            entryDao.insert(
                EntryEntity(
                    monthId = monthId,
                    amountPaisa = amount.paisa,
                    ref = null,
                    personId = personId,
                    date = DateTimeFormat.todayEpochDay(),
                    time = DateTimeFormat.nowTime(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun addExpense(
        amount: Money,
        categoryId: Long,
        ref: String?,
    ): Result<Unit, DataError.Local> = withContext(dispatchers.io) {
        try {
            val now = System.currentTimeMillis()
            val monthId = session.currentMonthId.value
            expenseDao.insert(
                ExpenseEntity(
                    monthId = monthId,
                    timeCategoryId = categoryId,
                    amountPaisa = amount.paisa,
                    ref = ref?.takeIf { it.isNotBlank() },
                    date = DateTimeFormat.todayEpochDay(),
                    time = DateTimeFormat.nowTime(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}
