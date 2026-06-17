package com.example.chafund.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chafund.core.data.database.entity.TimeCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeCategoryDao {

    @Query("SELECT * FROM TimeCategory ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<TimeCategoryEntity>>

    @Query("SELECT * FROM TimeCategory WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): TimeCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: TimeCategoryEntity): Long

    @Query("UPDATE TimeCategory SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String): Int

    @Query("DELETE FROM TimeCategory WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
