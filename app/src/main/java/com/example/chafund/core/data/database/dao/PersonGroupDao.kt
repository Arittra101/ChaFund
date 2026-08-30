package com.example.chafund.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chafund.core.data.database.entity.PersonGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonGroupDao {

    @Query("SELECT * FROM PersonGroup ORDER BY name ASC, id ASC")
    fun observeAll(): Flow<List<PersonGroupEntity>>

    @Query("SELECT * FROM PersonGroup WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): PersonGroupEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(group: PersonGroupEntity): Long

    @Query("UPDATE PersonGroup SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String): Int

    @Query("SELECT COUNT(*) FROM Person WHERE groupId = :groupId")
    suspend fun countPersonsInGroup(groupId: Long): Int

    @Query("DELETE FROM PersonGroup WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
