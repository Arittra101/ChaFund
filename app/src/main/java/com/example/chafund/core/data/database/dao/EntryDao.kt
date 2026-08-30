package com.example.chafund.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chafund.core.data.database.entity.EntryEntity
import com.example.chafund.core.data.database.projection.EntryWithPersonProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM Entry WHERE monthId = :monthId ORDER BY date DESC, time DESC, id DESC")
    fun observeByMonth(monthId: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM Entry WHERE monthId = :monthId AND date = :date ORDER BY time DESC, id DESC")
    fun observeByDate(monthId: Long, date: Long): Flow<List<EntryEntity>>

    // Filter by date only: a given epoch-day maps to exactly one calendar month's rows,
    // so this works for normal days and for previous-month "tail" days shown under another month.
    @Query(
        """
        SELECT e.*, p.name AS personName, g.name AS groupName
        FROM Entry e
        LEFT JOIN Person p ON p.id = e.personId
        LEFT JOIN PersonGroup g ON g.id = p.groupId
        WHERE e.date = :date
        ORDER BY e.time DESC, e.id DESC
        """
    )
    fun observeByDateWithPerson(date: Long): Flow<List<EntryWithPersonProjection>>

    // All entries for a month, tail-aware, with person + group for display.
    @Query(
        """
        SELECT e.*, p.name AS personName, g.name AS groupName
        FROM Entry e
        LEFT JOIN Person p ON p.id = e.personId
        LEFT JOIN PersonGroup g ON g.id = p.groupId
        WHERE e.monthId = :monthId
           OR (:tailStart IS NOT NULL AND e.date BETWEEN :tailStart AND :tailEnd)
        ORDER BY e.date DESC, e.time DESC, e.id DESC
        """
    )
    fun observeByMonthWithPerson(monthId: Long, tailStart: Long?, tailEnd: Long): Flow<List<EntryWithPersonProjection>>

    @Query("SELECT * FROM Entry WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): EntryEntity?

    @Query("SELECT IFNULL(SUM(amountPaisa), 0) FROM Entry WHERE monthId = :monthId")
    fun sumByMonth(monthId: Long): Flow<Long>

    @Query("""
        SELECT IFNULL(SUM(amountPaisa), 0) FROM Entry
        WHERE monthId = :monthId
           OR (:tailStart IS NOT NULL AND date BETWEEN :tailStart AND :tailEnd)
    """)
    fun sumByMonthWithTail(monthId: Long, tailStart: Long?, tailEnd: Long): Flow<Long>

    @Insert
    suspend fun insert(entry: EntryEntity): Long

    @Update
    suspend fun update(entry: EntryEntity): Int

    @Query("DELETE FROM Entry WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
