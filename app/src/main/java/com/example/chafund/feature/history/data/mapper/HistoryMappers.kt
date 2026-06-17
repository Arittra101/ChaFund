package com.example.chafund.feature.history.data.mapper

import com.example.chafund.core.data.database.entity.EntryEntity
import com.example.chafund.core.data.database.projection.DailySummaryProjection
import com.example.chafund.core.data.database.projection.MonthSummaryProjection
import com.example.chafund.core.data.database.relation.ExpenseWithCategory
import com.example.chafund.core.utils.DateTimeFormat
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.history.domain.model.DailySummary
import com.example.chafund.feature.history.domain.model.HistoryEntry
import com.example.chafund.feature.history.domain.model.HistoryExpense
import com.example.chafund.feature.history.domain.model.HistoryMonth

fun DailySummaryProjection.toDomain() = DailySummary(
    date = date,
    dateLabel = DateTimeFormat.formatDate(date),
    dayName = DateTimeFormat.dayName(date),
    totalSpent = Money(totalSpentForDay),
    totalEntries = Money(totalEntriesForDay),
    balanceAtPoint = Money(balanceAtPoint),
)

fun EntryEntity.toHistoryDomain() = HistoryEntry(
    id          = id,
    amountPaisa = amountPaisa,
    ref         = ref,
    time        = time,
    date        = date,
    monthId     = monthId,
)

fun ExpenseWithCategory.toHistoryDomain() = HistoryExpense(
    id                = expense.id,
    amountPaisa       = expense.amountPaisa,
    ref               = expense.ref,
    time              = expense.time,
    date              = expense.date,
    monthId           = expense.monthId,
    categoryId        = category.id,
    categoryName      = category.name,
    categorySortOrder = category.sortOrder,
)

fun MonthSummaryProjection.toHistoryDomain() = HistoryMonth(
    id           = monthId,
    label        = label,
    isCurrent    = isCurrent,
    totalEntries = Money(totalEntriesPaisa),
    totalSpent   = Money(totalSpentPaisa),
    balance      = Money(totalEntriesPaisa - totalSpentPaisa),
)
