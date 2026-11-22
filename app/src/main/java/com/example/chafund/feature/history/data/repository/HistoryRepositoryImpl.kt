package com.example.chafund.feature.history.data.repository

import com.example.chafund.core.data.database.dao.EntryDao
import com.example.chafund.core.data.database.dao.ExpenseDao
import com.example.chafund.core.data.database.dao.HistoryDao
import com.example.chafund.core.data.database.dao.MonthDao
import com.example.chafund.core.data.database.dao.PaymentMethodDao
import com.example.chafund.core.data.database.dao.PersonDao
import com.example.chafund.core.domain.DataError
import com.example.chafund.core.domain.DispatcherProvider
import com.example.chafund.core.domain.Result
import com.example.chafund.feature.fund.data.mapper.toDomain as paymentMethodToDomain
import com.example.chafund.feature.fund.data.mapper.toDomain as personToDomain
import com.example.chafund.feature.fund.domain.model.PaymentMethod
import com.example.chafund.feature.fund.domain.model.Person
import com.example.chafund.feature.history.data.mapper.toDomain
import com.example.chafund.feature.history.data.mapper.toHistoryDomain
import com.example.chafund.feature.history.domain.HistoryRepository
import com.example.chafund.core.data.database.projection.MethodTotalProjection
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.model.AmountBreakdown
import com.example.chafund.feature.history.domain.model.DailySummary
import com.example.chafund.feature.history.domain.model.MethodBreakdown
import com.example.chafund.feature.history.domain.model.ExpenseGrouped
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.feature.history.domain.model.HistoryMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HistoryRepositoryImpl(
    private val monthDao: MonthDao,
    private val entryDao: EntryDao,
    private val expenseDao: ExpenseDao,
    private val historyDao: HistoryDao,
    private val personDao: PersonDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val dispatchers: DispatcherProvider,
) : HistoryRepository {

    override fun observeMonthSummaries(): Flow<List<HistoryMonth>> =
        monthDao.observeMonthSummaries().map { list -> list.map { it.toHistoryDomain() } }

    override fun observeDailySummaries(monthId: Long): Flow<List<DailySummary>> =
        historyDao.observeDailySummaries(monthId).map { list -> list.map { it.toDomain() } }

    override fun observeBreakdownForMonth(monthId: Long): Flow<AmountBreakdown> =
        combine(
            paymentMethodDao.observeAll(),
            entryDao.sumByMethodForMonth(monthId),
            expenseDao.sumByMethodForMonth(monthId),
        ) { methods, collected, spent -> buildBreakdown(methods, collected, spent) }

    override fun observeBreakdownForDay(date: Long): Flow<AmountBreakdown> =
        combine(
            paymentMethodDao.observeAll(),
            entryDao.sumByMethodForDay(date),
            expenseDao.sumByMethodForDay(date),
        ) { methods, collected, spent -> buildBreakdown(methods, collected, spent) }

    /**
     * One row per defined payment method, in the order they are defined, plus a trailing
     * "Unspecified" row for pre-payment-method records — only when those actually carry money.
     */
    private fun buildBreakdown(
        methods: List<com.example.chafund.core.data.database.entity.PaymentMethodEntity>,
        collected: List<MethodTotalProjection>,
        spent: List<MethodTotalProjection>,
    ): AmountBreakdown {
        val collectedById = collected.associate { it.paymentMethodId to it.totalPaisa }
        val spentById = spent.associate { it.paymentMethodId to it.totalPaisa }

        val rows = methods.map { method ->
            MethodBreakdown(
                methodId = method.id,
                methodName = method.name,
                collected = Money(collectedById[method.id] ?: 0L),
                spent = Money(spentById[method.id] ?: 0L),
            )
        }

        val unassignedCollected = collectedById[null] ?: 0L
        val unassignedSpent = spentById[null] ?: 0L
        val unassigned = if (unassignedCollected != 0L || unassignedSpent != 0L) {
            listOf(
                MethodBreakdown(
                    methodId = null,
                    methodName = "Unspecified",
                    collected = Money(unassignedCollected),
                    spent = Money(unassignedSpent),
                )
            )
        } else {
            emptyList()
        }

        return AmountBreakdown(rows + unassigned)
    }

    override fun observeEntriesForMonth(monthId: Long): Flow<List<HistoryEntry>> =
        entryDao.observeByMonthWithPerson(monthId).map { list -> list.map { it.toHistoryDomain() } }

    override fun observeEntriesForDay(date: Long): Flow<List<HistoryEntry>> =
        entryDao.observeByDateWithPerson(date).map { list -> list.map { it.toHistoryDomain() } }

    override fun observePeople(): Flow<List<Person>> =
        personDao.observeAllWithGroup().map { list -> list.map { it.personToDomain() } }

    override fun observePaymentMethods(): Flow<List<PaymentMethod>> =
        paymentMethodDao.observeAll().map { list -> list.map { it.paymentMethodToDomain() } }

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

    override suspend fun updateEntry(
        id: Long,
        amountPaisa: Long,
        personId: Long?,
        paymentMethodId: Long?,
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
                        paymentMethodId = paymentMethodId,
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
        paymentMethodId: Long?,
        ref: String?,
    ): Result<Unit, DataError.Local> =
        withContext(dispatchers.io) {
            runCatching {
                val entity = expenseDao.findById(id)
                    ?: return@withContext Result.Error(DataError.Local.NOT_FOUND)
                expenseDao.update(
                    entity.copy(
                        amountPaisa = amountPaisa,
                        timeCategoryId = categoryId,
                        paymentMethodId = paymentMethodId,
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
