package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE laps ADD COLUMN splitLatitude REAL")
        db.execSQL("ALTER TABLE laps ADD COLUMN splitLongitude REAL")
    }
}
