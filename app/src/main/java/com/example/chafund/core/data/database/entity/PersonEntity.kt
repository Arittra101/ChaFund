package com.example.chafund.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Person",
    foreignKeys = [
        ForeignKey(
            entity = PersonGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("groupId"),
        Index(value = ["groupId", "name"], unique = true),
    ],
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val groupId: Long,
    val createdAt: Long,
)
