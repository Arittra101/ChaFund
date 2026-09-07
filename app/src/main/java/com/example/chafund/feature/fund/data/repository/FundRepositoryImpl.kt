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
import com.example.chafund.core.data.database.entity.MonthEntity
import com.example.chafund.core.data.session.Session
import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.Result
import com.example.chafund.core.domain.DispatcherProvider
import com.example.chafund.core.utils.DateTimeFormat
import com.example.chafund.core.utils.Money
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
import java.time.LocalDate

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
                combine(
                    entryDao.sumByMonth(month.id),
                    expenseDao.sumByMonth(month.id),
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
        dateEpochDay: Long,
    ): Result<Unit, DataError.Local> = withContext(dispatchers.io) {
        try {
            val now = System.currentTimeMillis()
            entryDao.insert(
                EntryEntity(
                    monthId = resolveMonthId(dateEpochDay),
                    amountPaisa = amount.paisa,
                    ref = null,
                    personId = personId,
                    date = dateEpochDay,
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
        dateEpochDay: Long,
    ): Result<Unit, DataError.Local> = withContext(dispatchers.io) {
        try {
            val now = System.currentTimeMillis()
            expenseDao.insert(
                ExpenseEntity(
                    monthId = resolveMonthId(dateEpochDay),
                    timeCategoryId = categoryId,
                    amountPaisa = amount.paisa,
                    ref = ref?.takeIf { it.isNotBlank() },
                    date = dateEpochDay,
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

    /**
     * Resolves the month a record dated [dateEpochDay] belongs to. Dates within the current
     * calendar month map to the established current month; a date in a later calendar month
     * (e.g. pre-dating into next month) upserts that month as a non-current month so its
     * activity is tracked separately without disturbing the current month.
     */
    private suspend fun resolveMonthId(dateEpochDay: Long): Long {
        val date = LocalDate.ofEpochDay(dateEpochDay)
        val today = LocalDate.now()
        if (date.year == today.year && date.monthValue == today.monthValue) {
            return session.currentMonthId.value
        }
        return monthDao.upsertByYearMonth(
            MonthEntity(
                year = date.year,
                month = date.monthValue,
                label = DateTimeFormat.monthLabel(date.year, date.monthValue),
                isCurrent = false,
                createdAt = System.currentTimeMillis(),
            )
        )
    }
}
