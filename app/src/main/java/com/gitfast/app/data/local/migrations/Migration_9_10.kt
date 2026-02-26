package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS soreness_logs (
                id TEXT NOT NULL PRIMARY KEY,
                date INTEGER NOT NULL,
                muscleGroups TEXT NOT NULL,
                intensity TEXT NOT NULL,
                notes TEXT,
                xpAwarded INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_soreness_logs_date ON soreness_logs(date)")

        db.execSQL("ALTER TABLE character_profile ADD COLUMN toughnessStat INTEGER NOT NULL DEFAULT 1")
    }
}
