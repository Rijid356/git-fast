package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Add profileId column to xp_transactions (default 1 = user)
        db.execSQL(
            "ALTER TABLE xp_transactions ADD COLUMN profileId INTEGER NOT NULL DEFAULT 1"
        )

        // 2. Recreate unlocked_achievements with composite PK (achievementId, profileId)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS unlocked_achievements_new (
                achievementId TEXT NOT NULL,
                unlockedAt INTEGER NOT NULL,
                xpAwarded INTEGER NOT NULL,
                profileId INTEGER NOT NULL DEFAULT 1,
                PRIMARY KEY (achievementId, profileId)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO unlocked_achievements_new (achievementId, unlockedAt, xpAwarded, profileId)
            SELECT achievementId, unlockedAt, xpAwarded, 1 FROM unlocked_achievements
            """.trimIndent()
        )
        db.execSQL("DROP TABLE unlocked_achievements")
        db.execSQL("ALTER TABLE unlocked_achievements_new RENAME TO unlocked_achievements")

        // 3. Insert Juniper profile row (id=2)
        db.execSQL(
            """
            INSERT OR IGNORE INTO character_profile (id, totalXp, level, createdAt, speedStat, enduranceStat, consistencyStat)
            VALUES (2, 0, 1, ${System.currentTimeMillis()}, 1, 1, 1)
            """.trimIndent()
        )
    }
}
