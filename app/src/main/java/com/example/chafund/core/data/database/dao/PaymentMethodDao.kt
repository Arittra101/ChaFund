package com.example.chafund.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chafund.core.data.database.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Query("SELECT * FROM PaymentMethod ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM PaymentMethod WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): PaymentMethodEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(method: PaymentMethodEntity): Long

    @Query("UPDATE PaymentMethod SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String): Int

    @Query("DELETE FROM PaymentMethod WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
