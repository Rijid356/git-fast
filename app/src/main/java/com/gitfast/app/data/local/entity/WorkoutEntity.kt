package com.gitfast.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.model.WorkoutStatus

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey
    val id: String,
    val startTime: Long,
    val endTime: Long?,
    val totalSteps: Int,
    val distanceMeters: Double,
    val status: WorkoutStatus,
    val activityType: ActivityType,
    val dogName: String?,
    val notes: String?,
    val weatherCondition: WeatherCondition?,
    val weatherTemp: WeatherTemp?,
    val energyLevel: EnergyLevel?,
    val routeTag: String?,
    val narrativeDescription: String? = null,
    val weatherTempF: Int? = null,
    val weatherWindMph: Int? = null,
    val weatherHumidity: Int? = null,
    val weatherDescription: String? = null,
)
