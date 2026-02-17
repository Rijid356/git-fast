package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `character_profile` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `totalXp` INTEGER NOT NULL DEFAULT 0,
                `level` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            "INSERT OR IGNORE INTO `character_profile` (`id`, `totalXp`, `level`, `createdAt`) VALUES (1, 0, 1, ${System.currentTimeMillis()})"
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `xp_transactions` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `workoutId` TEXT NOT NULL,
                `xpAmount` INTEGER NOT NULL,
                `reason` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_xp_transactions_workoutId` ON `xp_transactions` (`workoutId`)")
    }
}
