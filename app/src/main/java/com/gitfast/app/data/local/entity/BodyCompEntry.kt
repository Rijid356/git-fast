package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_comp_entries")
data class BodyCompEntry(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val weightKg: Double?,
    val bodyFatPercent: Double?,
    val leanBodyMassKg: Double?,
    val boneMassKg: Double?,
    val bmrKcalPerDay: Double?,
    val heightMeters: Double?,
    val source: String,
)
