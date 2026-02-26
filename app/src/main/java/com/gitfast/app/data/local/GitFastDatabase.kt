package com.gitfast.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gitfast.app.data.local.entity.BodyCompEntry
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.local.entity.DogWalkEventEntity
import com.gitfast.app.data.local.entity.UnlockedAchievementEntity
import com.gitfast.app.data.local.entity.XpTransactionEntity

@Database(
    entities = [
        WorkoutEntity::class,
        WorkoutPhaseEntity::class,
        LapEntity::class,
        GpsPointEntity::class,
        RouteTagEntity::class,
        CharacterProfileEntity::class,
        XpTransactionEntity::class,
        UnlockedAchievementEntity::class,
        BodyCompEntry::class,
        DogWalkEventEntity::class,
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GitFastDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun characterDao(): CharacterDao
    abstract fun bodyCompDao(): BodyCompDao
}
