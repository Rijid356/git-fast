package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class ExerciseSetEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val setNumber: Int,
    val reps: Int,
    val weightLbs: Double? = null,
    val durationSeconds: Int? = null,
    val isWarmup: Boolean = false,
    val completedAt: Long,
)
