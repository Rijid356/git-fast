package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenshots")
data class ScreenshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val filename: String,
    val galleryUri: String,
    val workoutId: String? = null,
    val activityType: String? = null,
    val screenRoute: String? = null,
)
