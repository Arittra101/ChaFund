package com.example.chafund.core.data.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.chafund.core.data.database.entity.ExpenseEntity
import com.example.chafund.core.data.database.entity.TimeCategoryEntity

data class ExpenseWithCategory(
    @Embedded val expense: ExpenseEntity,
    @Relation(
        parentColumn = "timeCategoryId",
        entityColumn = "id",
    )
    val category: TimeCategoryEntity,
)
