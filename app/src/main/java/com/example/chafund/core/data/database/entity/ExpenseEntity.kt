package com.example.chafund.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Expense",
    foreignKeys = [
        ForeignKey(
            entity = MonthEntity::class,
            parentColumns = ["id"],
            childColumns = ["monthId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TimeCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["timeCategoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("monthId"),
        Index("date"),
        Index("timeCategoryId"),
        Index("paymentMethodId"),
    ],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val monthId: Long,
    val timeCategoryId: Long,
    val amountPaisa: Long,
    /** Null only for rows created before payment methods existed. */
    val paymentMethodId: Long? = null,
    val ref: String?,
    val date: Long,       // epoch-day
    val time: String,     // "HH:mm"
    val createdAt: Long,
    val updatedAt: Long,
)
