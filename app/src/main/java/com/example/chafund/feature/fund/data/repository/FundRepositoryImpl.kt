package com.example.chafund.feature.fund.data.repository

import android.database.sqlite.SQLiteException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.chafund.core.data.database.dao.EntryDao
import com.example.chafund.core.data.database.dao.ExpenseDao
import com.example.chafund.core.data.database.dao.MonthDao
import com.example.chafund.core.data.database.dao.TimeCategoryDao
import com.example.chafund.core.data.database.entity.EntryEntity
import com.example.chafund.core.data.database.entity.ExpenseEntity
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
    private val session: Session,
    private val dispatchers: DispatcherProvider,
) : FundRepository {

    override fun observeCurrentMonth(): Flow<Month?> =
        monthDao.observeCurrent().map { it?.toDomain() }

    override fun observeCurrentMonthSummary(): Flow<MonthSummary> =
        session.currentMonthId.flatMapLatest { monthId ->
            if (monthId == 0L) {
                flowOf(MonthSummary.empty())
            } else {
                combine(
                    entryDao.sumByMonth(monthId),
                    expenseDao.sumByMonth(monthId),
                ) { entrySum, expenseSum ->
                    MonthSummary(
                        monthId      = monthId,
                        totalEntries = Money(entrySum),
                        totalSpent   = Money(expenseSum),
                        balance      = Money(entrySum - expenseSum),
                    )
                }
            }
        }

    override fun observeTimeCategories(): Flow<List<TimeCategory>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addEntry(
        amount: Money,
        ref: String?,
    ): Result<Unit, DataError.Local> = withContext(dispatchers.io) {
        try {
            val now     = System.currentTimeMillis()
            val monthId = session.currentMonthId.value
            entryDao.insert(
                EntryEntity(
                    monthId     = monthId,
                    amountPaisa = amount.paisa,
                    ref         = ref?.takeIf { it.isNotBlank() },
                    date        = DateTimeFormat.todayEpochDay(),
                    time        = DateTimeFormat.nowTime(),
                    createdAt   = now,
                    updatedAt   = now,
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
            val now     = System.currentTimeMillis()
            val monthId = session.currentMonthId.value
            expenseDao.insert(
                ExpenseEntity(
                    monthId        = monthId,
                    timeCategoryId = categoryId,
                    amountPaisa    = amount.paisa,
                    ref            = ref?.takeIf { it.isNotBlank() },
                    date           = DateTimeFormat.todayEpochDay(),
                    time           = DateTimeFormat.nowTime(),
                    createdAt      = now,
                    updatedAt      = now,
                )
            )
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}
