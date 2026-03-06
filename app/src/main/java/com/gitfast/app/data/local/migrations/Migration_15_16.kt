package com.gitfast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Weather data columns
        db.execSQL("ALTER TABLE workouts ADD COLUMN weatherTempF INTEGER")
        db.execSQL("ALTER TABLE workouts ADD COLUMN weatherWindMph INTEGER")
        db.execSQL("ALTER TABLE workouts ADD COLUMN weatherHumidity INTEGER")
        db.execSQL("ALTER TABLE workouts ADD COLUMN weatherDescription TEXT")

        // Walk photos table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `walk_photos` (
                `id` TEXT NOT NULL,
                `workoutId` TEXT NOT NULL,
                `filePath` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_walk_photos_workoutId` ON `walk_photos` (`workoutId`)")
    }
}
