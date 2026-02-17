package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gps_points",
    foreignKeys = [ForeignKey(
        entity = WorkoutEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workoutId")]
)
data class GpsPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val accuracy: Float,
    val sortIndex: Int
)
