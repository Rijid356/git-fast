package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `character_profile` ADD COLUMN `speedStat` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `character_profile` ADD COLUMN `enduranceStat` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `character_profile` ADD COLUMN `consistencyStat` INTEGER NOT NULL DEFAULT 1")
    }
}
