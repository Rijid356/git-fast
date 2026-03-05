package com.gitfast.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gitfast.app.data.local.entity.BodyCompEntry
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.SorenessLogEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.local.entity.DogWalkEventEntity
import com.gitfast.app.data.local.entity.ExerciseSessionEntity
import com.gitfast.app.data.local.entity.ExerciseSetEntity
import com.gitfast.app.data.local.entity.LapStartPointEntity
import com.gitfast.app.data.local.entity.ScreenshotEntity
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
        SorenessLogEntity::class,
        ExerciseSessionEntity::class,
        ExerciseSetEntity::class,
        LapStartPointEntity::class,
        ScreenshotEntity::class,
    ],
    version = 15,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GitFastDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun characterDao(): CharacterDao
    abstract fun bodyCompDao(): BodyCompDao
    abstract fun sorenessDao(): SorenessDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun lapStartPointDao(): LapStartPointDao
    abstract fun screenshotDao(): ScreenshotDao
}
