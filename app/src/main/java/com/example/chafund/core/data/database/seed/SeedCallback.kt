package com.example.chafund.core.data.database.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class SeedCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        val defaults = listOf(
            "Morning"   to 1,
            "Noon"      to 2,
            "Afternoon" to 3,
            "Evening"   to 4,
        )
        db.beginTransaction()
        try {
            defaults.forEach { (name, sortOrder) ->
                db.execSQL(
                    "INSERT INTO TimeCategory (name, sortOrder, createdAt) VALUES (?, ?, ?)",
                    arrayOf(name, sortOrder, now),
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
