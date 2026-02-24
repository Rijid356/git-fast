package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS body_comp_entries (
                id TEXT NOT NULL PRIMARY KEY,
                timestamp INTEGER NOT NULL,
                weightKg REAL,
                bodyFatPercent REAL,
                leanBodyMassKg REAL,
                boneMassKg REAL,
                bmrKcalPerDay REAL,
                heightMeters REAL,
                source TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL("ALTER TABLE character_profile ADD COLUMN vitalityStat INTEGER NOT NULL DEFAULT 1")
    }
}
