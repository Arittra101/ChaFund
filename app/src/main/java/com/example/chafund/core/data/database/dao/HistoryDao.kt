package com.example.chafund.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.chafund.core.data.database.projection.DailySummaryProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("""
        SELECT x.date,
          IFNULL((SELECT SUM(amountPaisa) FROM Expense WHERE monthId = :monthId AND date = x.date), 0) AS totalSpentForDay,
          IFNULL((SELECT SUM(amountPaisa) FROM Entry   WHERE monthId = :monthId AND date = x.date), 0) AS totalEntriesForDay,
          IFNULL((SELECT SUM(amountPaisa) FROM Entry   WHERE monthId = :monthId AND date <= x.date), 0) -
          IFNULL((SELECT SUM(amountPaisa) FROM Expense WHERE monthId = :monthId AND date <= x.date), 0) AS balanceAtPoint
        FROM (
          SELECT date FROM Entry   WHERE monthId = :monthId
          UNION
          SELECT date FROM Expense WHERE monthId = :monthId
        ) x
        ORDER BY x.date DESC
    """)
    fun observeDailySummaries(monthId: Long): Flow<List<DailySummaryProjection>>

    /**
     * Tail-aware variant: a row belongs to the month when it matches [monthId] OR falls in the
     * previous-month tail [tailStart]..[tailEnd] (inert when [tailStart] is null).
     */
    @Query("""
        SELECT x.date,
          IFNULL((SELECT SUM(amountPaisa) FROM Expense WHERE (monthId = :monthId OR (:tailStart IS NOT NULL AND date BETWEEN :tailStart AND :tailEnd)) AND date = x.date), 0) AS totalSpentForDay,
          IFNULL((SELECT SUM(amountPaisa) FROM Entry   WHERE (monthId = :monthId OR (:tailStart IS NOT NULL AND date BETWEEN :tailStart AND :tailEnd)) AND date = x.date), 0) AS totalEntriesForDay,
          IFNULL((SELECT SUM(amountPaisa) FROM Entry   WHERE (monthId = :monthId OR (:tailStart IS NOT NULL AND date BETWEEN :tailStart AND :tailEnd)) AND date <= x.date), 0) -
          IFNULL((SELECT SUM(amountPaisa) FROM Expense WHERE (monthId = :monthId OR (:tailStart IS NOT NULL AND date BETWEEN :tailStart AND :tailEnd)) AND date <= x.date), 0) AS balanceAtPoint
        FROM (
          SELECT date FROM Entry   WHERE monthId = :monthId OR (:tailStart IS NOT NULL AND date BETWEEN :tailStart AND :tailEnd)
          UNION
          SELECT date FROM Expense WHERE monthId = :monthId OR (:tailStart IS NOT NULL AND date BETWEEN :tailStart AND :tailEnd)
        ) x
        ORDER BY x.date DESC
    """)
    fun observeDailySummariesWithTail(monthId: Long, tailStart: Long?, tailEnd: Long): Flow<List<DailySummaryProjection>>
}
