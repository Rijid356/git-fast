package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Transform legacy "CHEST,BACK" + intensity="MODERATE" format
        // into new "CHEST:MODERATE,BACK:MODERATE" per-muscle format.
        // Guard: skip empty rows and already-migrated rows (containing ':').
        db.execSQL(
            """
            UPDATE soreness_logs
            SET muscleGroups = REPLACE(muscleGroups, ',', ':' || intensity || ',') || ':' || intensity
            WHERE muscleGroups != '' AND muscleGroups NOT LIKE '%:%'
            """.trimIndent()
        )
    }
}
