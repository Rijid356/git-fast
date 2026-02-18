package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS unlocked_achievements (
                achievementId TEXT NOT NULL PRIMARY KEY,
                unlockedAt INTEGER NOT NULL,
                xpAwarded INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
