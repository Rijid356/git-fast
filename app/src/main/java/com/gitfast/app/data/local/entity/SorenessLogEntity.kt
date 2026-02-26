package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soreness_logs")
data class SorenessLogEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val muscleGroups: String,
    val intensity: String,
    val notes: String? = null,
    val xpAwarded: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
