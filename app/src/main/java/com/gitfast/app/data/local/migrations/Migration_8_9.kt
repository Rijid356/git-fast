package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dog_walk_events (
                id TEXT NOT NULL PRIMARY KEY,
                workoutId TEXT NOT NULL,
                eventType TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                latitude REAL,
                longitude REAL,
                FOREIGN KEY (workoutId) REFERENCES workouts(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_dog_walk_events_workoutId ON dog_walk_events(workoutId)")

        db.execSQL("ALTER TABLE character_profile ADD COLUMN foragingStat INTEGER NOT NULL DEFAULT 1")
    }
}
