package com.gitfast.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity

@Database(
    entities = [
        WorkoutEntity::class,
        WorkoutPhaseEntity::class,
        LapEntity::class,
        GpsPointEntity::class,
        RouteTagEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GitFastDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
