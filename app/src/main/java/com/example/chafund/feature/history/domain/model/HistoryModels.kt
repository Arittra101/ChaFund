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
)

/** Collected / spent / remaining for a single payment method. */
data class MethodBreakdown(
    val methodId: Long?,
    val methodName: String,
    val collected: Money,
    val spent: Money,
) {
    val remaining: Money get() = collected - spent
}

/**
 * Money split by payment method for a day or a month. [rows] keeps the order the methods are
 * defined in; records saved before payment methods existed land in an "Unspecified" row.
 */
data class AmountBreakdown(val rows: List<MethodBreakdown> = emptyList()) {
    val totalCollected: Money get() = Money(rows.sumOf { it.collected.paisa })
    val totalSpent: Money get() = Money(rows.sumOf { it.spent.paisa })
    val totalRemaining: Money get() = Money(rows.sumOf { it.remaining.paisa })
    val isEmpty: Boolean get() = rows.isEmpty()

    companion object {
        val Empty = AmountBreakdown()
    }
}
