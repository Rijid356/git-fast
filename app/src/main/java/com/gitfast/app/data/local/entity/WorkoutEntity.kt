package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gitfast.app.data.model.WorkoutStatus

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey
    val id: String,
    val startTime: Long,
    val endTime: Long?,
    val totalSteps: Int,
    val distanceMeters: Double,
    val status: WorkoutStatus
)
