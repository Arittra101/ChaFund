package com.example.chafund.core.data.database.projection

data class DailySummaryProjection(
    val date: Long,
    val totalSpentForDay: Long,
    val totalEntriesForDay: Long,
    val balanceAtPoint: Long,
)
