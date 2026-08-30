package com.example.chafund.feature.history.domain.model

import com.example.chafund.core.utils.Money

data class DailySummary(
    val date: Long,
    val dateLabel: String,
    val dayName: String,
    val totalSpent: Money,
    val totalEntries: Money,
    val balanceAtPoint: Money,
)

data class HistoryEntry(
    val id: Long,
    val amountPaisa: Long,
    val ref: String?,
    val personId: Long?,
    val personName: String?,
    val groupName: String?,
    val time: String,
    val date: Long,
    val monthId: Long,
) {
    /** Preferred display label: "Name ~ Group" when linked, else the legacy ref. */
    val displayLabel: String?
        get() = if (personName != null && groupName != null) "$personName ~ $groupName" else ref
}

data class HistoryExpense(
    val id: Long,
    val amountPaisa: Long,
    val ref: String?,
    val time: String,
    val date: Long,
    val monthId: Long,
    val categoryId: Long,
    val categoryName: String,
    val categorySortOrder: Int,
)

data class ExpenseGrouped(
    val categoryId: Long,
    val categoryName: String,
    val sortOrder: Int,
    val expenses: List<HistoryExpense>,
)

data class HistoryMonth(
    val id: Long,
    val label: String,
    val isCurrent: Boolean,
    val totalEntries: Money,
    val totalSpent: Money,
    val balance: Money,
    val cycleStartEpochDay: Long? = null,
    val includePrevTail: Boolean = false,
    val monthFirstEpochDay: Long = 0L,
)
