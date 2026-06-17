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
}
