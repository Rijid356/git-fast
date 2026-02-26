package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gitfast.app.data.model.DogWalkEventType

@Entity(
    tableName = "dog_walk_events",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId")]
)
data class DogWalkEventEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val eventType: DogWalkEventType,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
)
