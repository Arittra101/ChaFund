package com.example.chafund.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.chafund.core.data.database.entity.ExpenseEntity
import com.example.chafund.core.data.database.relation.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM Expense WHERE monthId = :monthId ORDER BY date DESC, time DESC, id DESC")
    fun observeByMonth(monthId: Long): Flow<List<ExpenseEntity>>

    // Filter by date only: a given epoch-day maps to exactly one calendar month's rows,
    // so this works for normal days and for previous-month "tail" days shown under another month.
    @Transaction
    @Query("""
        SELECT * FROM Expense
        WHERE date = :date
        ORDER BY time DESC, id DESC
    """)
    fun observeByDate(date: Long): Flow<List<ExpenseWithCategory>>

    @Query("SELECT * FROM Expense WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ExpenseEntity?

    @Query("SELECT IFNULL(SUM(amountPaisa), 0) FROM Expense WHERE monthId = :monthId")
    fun sumByMonth(monthId: Long): Flow<Long>

    @Query("""
        SELECT IFNULL(SUM(amountPaisa), 0) FROM Expense
        WHERE monthId = :monthId
           OR (:tailStart IS NOT NULL AND date BETWEEN :tailStart AND :tailEnd)
    """)
    fun sumByMonthWithTail(monthId: Long, tailStart: Long?, tailEnd: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM Expense WHERE timeCategoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity): Int

    @Query("DELETE FROM Expense WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
