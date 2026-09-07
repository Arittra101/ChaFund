package com.example.chafund.core.data.database.projection

data class MonthSummaryProjection(
    val monthId: Long,
    val year: Int,
    val month: Int,
    val label: String,
    val isCurrent: Boolean,
    val totalEntriesPaisa: Long,
    val totalSpentPaisa: Long,
)
