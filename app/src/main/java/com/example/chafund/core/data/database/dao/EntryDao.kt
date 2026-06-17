package com.example.chafund.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.chafund.core.data.database.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query("SELECT * FROM Entry WHERE monthId = :monthId ORDER BY date DESC, time DESC, id DESC")
    fun observeByMonth(monthId: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM Entry WHERE monthId = :monthId AND date = :date ORDER BY time DESC, id DESC")
    fun observeByDate(monthId: Long, date: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM Entry WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): EntryEntity?

    @Query("SELECT IFNULL(SUM(amountPaisa), 0) FROM Entry WHERE monthId = :monthId")
    fun sumByMonth(monthId: Long): Flow<Long>

    @Insert
    suspend fun insert(entry: EntryEntity): Long

    @Update
    suspend fun update(entry: EntryEntity): Int

    @Query("DELETE FROM Entry WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
