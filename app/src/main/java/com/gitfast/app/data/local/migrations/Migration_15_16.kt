package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workouts ADD COLUMN weatherTempF INTEGER")
        db.execSQL("ALTER TABLE workouts ADD COLUMN weatherWindMph INTEGER")
        db.execSQL("ALTER TABLE workouts ADD COLUMN weatherHumidity INTEGER")
        db.execSQL("ALTER TABLE workouts ADD COLUMN weatherDescription TEXT")
    }
}
