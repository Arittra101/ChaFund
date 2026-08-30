package com.example.chafund.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: introduce PersonGroup + Person tables and Entry.personId.
 * Additive only — existing Month/Entry/Expense/TimeCategory rows are preserved.
 * Entry.personId has no SQL foreign key (integrity is enforced in the repository),
 * so no table recreation is required.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `PersonGroup` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `name` TEXT NOT NULL COLLATE NOCASE,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_PersonGroup_name` ON `PersonGroup` (`name`)"
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Person` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `name` TEXT NOT NULL COLLATE NOCASE,
                `groupId` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`groupId`) REFERENCES `PersonGroup`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Person_groupId` ON `Person` (`groupId`)")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_Person_groupId_name` ON `Person` (`groupId`, `name`)"
        )

        db.execSQL("ALTER TABLE `Entry` ADD COLUMN `personId` INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Entry_personId` ON `Entry` (`personId`)")
    }
}

/**
 * v2 -> v3: per-month custom cycle start that overlays a previous-month tail into this month.
 * Additive only — existing Month rows default to no cycle (plain calendar month).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `Month` ADD COLUMN `cycleStartEpochDay` INTEGER")
        db.execSQL("ALTER TABLE `Month` ADD COLUMN `includePrevTail` INTEGER NOT NULL DEFAULT 0")
    }
}
