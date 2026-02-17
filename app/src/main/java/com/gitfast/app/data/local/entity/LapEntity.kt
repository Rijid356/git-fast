package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "laps",
    foreignKeys = [ForeignKey(
        entity = WorkoutPhaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["phaseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("phaseId")]
)
data class LapEntity(
    @PrimaryKey
    val id: String,
    val phaseId: String,
    val lapNumber: Int,
    val startTime: Long,
    val endTime: Long?,
    val distanceMeters: Double,
    val steps: Int
)
