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

/**
 * v3 -> v4: drop the custom cycle-start feature. Recreate the Month table without the
 * `cycleStartEpochDay` / `includePrevTail` columns; all existing month rows are preserved.
 * Table recreation is used (rather than DROP COLUMN) for compatibility with the older SQLite
 * engines bundled on some Android versions.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Month_new` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `year` INTEGER NOT NULL,
                `month` INTEGER NOT NULL,
                `label` TEXT NOT NULL,
                `isCurrent` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `Month_new` (`id`, `year`, `month`, `label`, `isCurrent`, `createdAt`)
            SELECT `id`, `year`, `month`, `label`, `isCurrent`, `createdAt` FROM `Month`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `Month`")
        db.execSQL("ALTER TABLE `Month_new` RENAME TO `Month`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Month_year_month` ON `Month` (`year`, `month`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Month_isCurrent` ON `Month` (`isCurrent`)")
    }
}

/**
 * v4 -> v5: add Entry.isCarryOver to flag the opening "<previous month> entry" produced by the
 * carry-last-month-balance action. Additive only — existing entries default to 0 (not a carry-over).
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `Entry` ADD COLUMN `isCarryOver` INTEGER NOT NULL DEFAULT 0")
    }
}
