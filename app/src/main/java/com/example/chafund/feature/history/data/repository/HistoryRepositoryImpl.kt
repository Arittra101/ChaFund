package com.example.chafund.feature.history.data.repository

import com.example.chafund.core.data.database.dao.EntryDao
import com.example.chafund.core.data.database.dao.ExpenseDao
import com.example.chafund.core.data.database.dao.HistoryDao
import com.example.chafund.core.data.database.dao.MonthDao
import com.example.chafund.core.data.database.dao.PersonDao
import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.DispatcherProvider
import com.example.chafund.core.domain.Result
import com.example.chafund.feature.fund.data.mapper.toDomain as personToDomain
import com.example.chafund.feature.fund.domain.model.Person
import com.example.chafund.feature.history.data.mapper.toDomain
import com.example.chafund.feature.history.data.mapper.toHistoryDomain
import com.example.chafund.feature.history.domain.HistoryRepository
import com.example.chafund.feature.history.domain.model.DailySummary
import com.example.chafund.feature.history.domain.model.ExpenseGrouped
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.feature.history.domain.model.HistoryMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HistoryRepositoryImpl(
    private val monthDao: MonthDao,
    private val entryDao: EntryDao,
    private val expenseDao: ExpenseDao,
    private val historyDao: HistoryDao,
    private val personDao: PersonDao,
    private val dispatchers: DispatcherProvider,
) : HistoryRepository {

    override fun observeMonthSummaries(): Flow<List<HistoryMonth>> =
        monthDao.observeMonthSummaries().map { list -> list.map { it.toHistoryDomain() } }

    override fun observeDailySummaries(monthId: Long, tailStart: Long?, tailEnd: Long): Flow<List<DailySummary>> =
        historyDao.observeDailySummariesWithTail(monthId, tailStart, tailEnd).map { list -> list.map { it.toDomain() } }

    override fun observeEntriesForMonth(monthId: Long, tailStart: Long?, tailEnd: Long): Flow<List<HistoryEntry>> =
        entryDao.observeByMonthWithPerson(monthId, tailStart, tailEnd).map { list -> list.map { it.toHistoryDomain() } }

    override fun observeEntriesForDay(date: Long): Flow<List<HistoryEntry>> =
        entryDao.observeByDateWithPerson(date).map { list -> list.map { it.toHistoryDomain() } }

    override fun observePeople(): Flow<List<Person>> =
        personDao.observeAllWithGroup().map { list -> list.map { it.personToDomain() } }

    override fun observeExpensesForDay(date: Long): Flow<List<ExpenseGrouped>> =
        expenseDao.observeByDate(date).map { list ->
            list.map { it.toHistoryDomain() }
                .groupBy { it.categoryId }
                .map { (catId, expenses) ->
                    ExpenseGrouped(
                        categoryId = catId,
                        categoryName = expenses.first().categoryName,
                        sortOrder = expenses.first().categorySortOrder,
                        expenses = expenses.sortedByDescending { it.time },
                    )
                }
                .sortedBy { it.sortOrder }
        }

    override suspend fun setIncludePrevTail(monthId: Long, include: Boolean): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                monthDao.setIncludePrevTail(monthId, include)
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun updateEntry(
        id: Long,
        amountPaisa: Long,
        personId: Long?
    ): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                val entity = entryDao.findById(id)
                    ?: return@withContext Result.Error(DataError.Local.NOT_FOUND)
                // When a name is chosen the legacy ref is superseded; keep ref only for
                // unnamed (legacy) entries that stay unnamed.
                entryDao.update(
                    entity.copy(
                        amountPaisa = amountPaisa,
                        personId = personId,
                        ref = if (personId != null) null else entity.ref,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun deleteEntry(id: Long): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                entryDao.deleteById(id)
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun updateExpense(
        id: Long,
        amountPaisa: Long,
        categoryId: Long,
        ref: String?
    ): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                val entity = expenseDao.findById(id)
                    ?: return@withContext Result.Error(DataError.Local.NOT_FOUND)
                expenseDao.update(
                    entity.copy(
                        amountPaisa = amountPaisa,
                        timeCategoryId = categoryId,
                        ref = ref,
                        updatedAt = System.currentTimeMillis(),
                    )
                )
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }

    override suspend fun deleteExpense(id: Long): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                expenseDao.deleteById(id)
                Result.Success(Unit)
            }.getOrElse { Result.Error(DataError.Local.UNKNOWN) }
        }
}
