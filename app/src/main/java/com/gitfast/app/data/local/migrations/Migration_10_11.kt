package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exercise_sessions (
                id TEXT NOT NULL PRIMARY KEY,
                startTime INTEGER NOT NULL,
                endTime INTEGER,
                notes TEXT,
                xpAwarded INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exercise_sets (
                id TEXT NOT NULL PRIMARY KEY,
                sessionId TEXT NOT NULL,
                exerciseId TEXT NOT NULL,
                setNumber INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                weightLbs REAL,
                durationSeconds INTEGER,
                isWarmup INTEGER NOT NULL DEFAULT 0,
                completedAt INTEGER NOT NULL,
                FOREIGN KEY (sessionId) REFERENCES exercise_sessions(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_sets_sessionId ON exercise_sets(sessionId)")

        db.execSQL("ALTER TABLE character_profile ADD COLUMN strengthStat INTEGER NOT NULL DEFAULT 1")
    }
}
