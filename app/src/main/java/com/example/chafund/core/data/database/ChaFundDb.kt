package com.example.chafund.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chafund.core.data.database.dao.EntryDao
import com.example.chafund.core.data.database.dao.ExpenseDao
import com.example.chafund.core.data.database.dao.MonthDao
import com.example.chafund.core.data.database.dao.TimeCategoryDao
import com.example.chafund.core.data.database.entity.EntryEntity
import com.example.chafund.core.data.database.entity.ExpenseEntity
import com.example.chafund.core.data.database.entity.MonthEntity
import com.example.chafund.core.data.database.entity.TimeCategoryEntity

@Database(
    entities = [
        MonthEntity::class,
        TimeCategoryEntity::class,
        EntryEntity::class,
        ExpenseEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class ChaFundDb : RoomDatabase() {
    abstract fun monthDao(): MonthDao
    abstract fun timeCategoryDao(): TimeCategoryDao
    abstract fun entryDao(): EntryDao
    abstract fun expenseDao(): ExpenseDao
}
