package com.example.chafund.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Month",
    indices = [
        Index(value = ["year", "month"], unique = true),
        Index(value = ["isCurrent"]),
    ],
)
data class MonthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val year: Int,
    val month: Int,
    val label: String,
    val isCurrent: Boolean,
    val createdAt: Long,
)
