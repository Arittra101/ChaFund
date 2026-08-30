package com.example.chafund.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chafund.core.data.database.entity.PersonEntity
import com.example.chafund.core.data.database.projection.PersonWithGroupProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query(
        """
        SELECT p.id AS id, p.name AS name, p.groupId AS groupId, g.name AS groupName
        FROM Person p
        JOIN PersonGroup g ON g.id = p.groupId
        ORDER BY g.name ASC, p.name ASC
        """
    )
    fun observeAllWithGroup(): Flow<List<PersonWithGroupProjection>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(person: PersonEntity): Long

    @Query("UPDATE Person SET name = :name, groupId = :groupId WHERE id = :id")
    suspend fun update(id: Long, name: String, groupId: Long): Int

    @Query("UPDATE Entry SET personId = NULL WHERE personId = :personId")
    suspend fun clearPersonRefsForEntries(personId: Long): Int

    @Query("DELETE FROM Person WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
