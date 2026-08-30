package com.example.chafund.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.chafund.core.data.database.entity.MonthEntity
import com.example.chafund.core.data.database.projection.MonthSummaryProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthDao {

    @Query("SELECT * FROM Month WHERE isCurrent = 1 LIMIT 1")
    fun observeCurrent(): Flow<MonthEntity?>

    @Query("SELECT * FROM Month WHERE year = :year AND month = :month LIMIT 1")
    suspend fun findByYearMonth(year: Int, month: Int): MonthEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(month: MonthEntity): Long

    @Transaction
    suspend fun upsertByYearMonth(month: MonthEntity): Long {
        val existing = findByYearMonth(month.year, month.month)
        if (existing != null) return existing.id
        val inserted = insertOrIgnore(month)
        if (inserted != -1L) return inserted
        return findByYearMonth(month.year, month.month)!!.id
    }

    @Query("UPDATE Month SET isCurrent = 0 WHERE isCurrent = 1")
    suspend fun unflagAllCurrent(): Int

    @Query("UPDATE Month SET isCurrent = 1 WHERE id = :id")
    suspend fun flagCurrent(id: Long): Int

    @Query("UPDATE Month SET cycleStartEpochDay = :start, includePrevTail = 1 WHERE id = :id")
    suspend fun setCycleStart(id: Long, start: Long): Int

    @Query("UPDATE Month SET cycleStartEpochDay = NULL, includePrevTail = 0 WHERE id = :id")
    suspend fun clearCycleStart(id: Long): Int

    @Query("UPDATE Month SET includePrevTail = :include WHERE id = :id")
    suspend fun setIncludePrevTail(id: Long, include: Boolean): Int

    @Transaction
    suspend fun promoteToCurrent(id: Long) {
        unflagAllCurrent()
        flagCurrent(id)
    }

    @Query("SELECT * FROM Month ORDER BY year DESC, month DESC")
    fun observeAll(): Flow<List<MonthEntity>>

    @Query("SELECT * FROM Month WHERE isCurrent = 0 ORDER BY year DESC, month DESC")
    fun observePast(): Flow<List<MonthEntity>>

    @Query("DELETE FROM Month WHERE id = :id AND isCurrent = 0")
    suspend fun deletePastById(id: Long): Int

    // Per-month totals are tail-aware: when a month has an active cycle
    // (cycleStartEpochDay set AND includePrevTail = 1) its totals also count previous-month rows
    // dated between the cycle start and the last day before this month begins. That tail-end
    // epoch-day is derived from the month's year/month via SQLite's julianday().
    @Query("""
        SELECT
            m.id           AS monthId,
            m.year         AS year,
            m.month        AS month,
            m.label        AS label,
            m.isCurrent    AS isCurrent,
            m.cycleStartEpochDay AS cycleStartEpochDay,
            m.includePrevTail    AS includePrevTail,
            IFNULL((
                SELECT SUM(amountPaisa) FROM Entry e
                WHERE e.monthId = m.id
                   OR (m.cycleStartEpochDay IS NOT NULL AND m.includePrevTail = 1
                       AND e.date BETWEEN m.cycleStartEpochDay
                       AND CAST(julianday(m.year || '-' || substr('0' || m.month, -2, 2) || '-01') - 2440587.5 AS INTEGER) - 1)
            ), 0) AS totalEntriesPaisa,
            IFNULL((
                SELECT SUM(amountPaisa) FROM Expense x
                WHERE x.monthId = m.id
                   OR (m.cycleStartEpochDay IS NOT NULL AND m.includePrevTail = 1
                       AND x.date BETWEEN m.cycleStartEpochDay
                       AND CAST(julianday(m.year || '-' || substr('0' || m.month, -2, 2) || '-01') - 2440587.5 AS INTEGER) - 1)
            ), 0) AS totalSpentPaisa
        FROM Month m
        ORDER BY m.year DESC, m.month DESC
    """)
    fun observeMonthSummaries(): Flow<List<MonthSummaryProjection>>
}
