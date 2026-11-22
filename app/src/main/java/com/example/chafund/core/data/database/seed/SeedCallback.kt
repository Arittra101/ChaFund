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
        // Kept in step with MIGRATION_5_6 so fresh and upgraded installs match.
        val paymentMethods = listOf(
            "Cash"  to 1,
            "bKash" to 2,
        )
        db.beginTransaction()
        try {
            defaults.forEach { (name, sortOrder) ->
                db.execSQL(
                    "INSERT INTO TimeCategory (name, sortOrder, createdAt) VALUES (?, ?, ?)",
                    arrayOf(name, sortOrder, now),
                )
            }
            paymentMethods.forEach { (name, sortOrder) ->
                db.execSQL(
                    "INSERT INTO PaymentMethod (name, sortOrder, createdAt) VALUES (?, ?, ?)",
                    arrayOf(name, sortOrder, now),
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
