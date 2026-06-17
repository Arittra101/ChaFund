package com.example.chafund.feature.fund.domain.model

import com.example.chafund.core.utils.Money

data class Month(
    val id: Long,
    val year: Int,
    val month: Int,
    val label: String,
    val isCurrent: Boolean,
)

data class TimeCategory(
    val id: Long,
    val name: String,
    val sortOrder: Int,
)

data class MonthSummary(
    val monthId: Long,
    val totalEntries: Money,
    val totalSpent: Money,
    val balance: Money,
) {
    companion object {
        fun empty(monthId: Long = 0L) = MonthSummary(
            monthId      = monthId,
            totalEntries = Money.Zero,
            totalSpent   = Money.Zero,
            balance      = Money.Zero,
        )
    }
}
