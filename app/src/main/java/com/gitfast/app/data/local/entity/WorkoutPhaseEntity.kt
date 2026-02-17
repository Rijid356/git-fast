package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gitfast.app.data.model.PhaseType

@Entity(
    tableName = "workout_phases",
    foreignKeys = [ForeignKey(
        entity = WorkoutEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("workoutId")]
)
data class WorkoutPhaseEntity(
    @PrimaryKey
    val id: String,
    val workoutId: String,
    val type: PhaseType,
    val startTime: Long,
    val endTime: Long?,
    val distanceMeters: Double,
    val steps: Int
)
