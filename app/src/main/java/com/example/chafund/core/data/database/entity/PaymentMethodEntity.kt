package com.example.chafund.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A way money moves — Cash, bKash, Nagad, bank… User-managed from Settings. */
@Entity(
    tableName = "PaymentMethod",
    indices = [Index(value = ["name"], unique = true)],
)
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val sortOrder: Int,
    val createdAt: Long,
)
