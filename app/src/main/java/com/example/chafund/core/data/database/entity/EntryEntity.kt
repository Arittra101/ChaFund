package com.example.chafund.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Entry",
    foreignKeys = [
        ForeignKey(
            entity = MonthEntity::class,
            parentColumns = ["id"],
            childColumns = ["monthId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("monthId"),
        Index("date"),
        Index("personId"),
        Index("paymentMethodId"),
    ],
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val monthId: Long,
    val amountPaisa: Long,
    val ref: String?,
    val personId: Long? = null,
    /** Null only for rows created before payment methods existed. */
    val paymentMethodId: Long? = null,
    val date: Long,        // epoch-day
    val time: String,      // "HH:mm"
    val createdAt: Long,
    val updatedAt: Long,
    /** True for the "<previous month> entry" row created by the carry-last-month-balance action. */
    val isCarryOver: Boolean = false,
)
