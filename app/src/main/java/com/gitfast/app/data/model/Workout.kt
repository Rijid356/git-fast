package com.gitfast.app.data.model

import java.time.Instant

data class Workout(
    val id: String,
    val startTime: Instant,
    val endTime: Instant?,
    val totalSteps: Int,
    val distanceMeters: Double,
    val status: WorkoutStatus,
    val activityType: ActivityType,
    val phases: List<WorkoutPhase>,
    val gpsPoints: List<GpsPoint>,
    val dogName: String?,
    val notes: String?,
    val weatherCondition: WeatherCondition?,
    val weatherTemp: WeatherTemp?,
    val energyLevel: EnergyLevel?,
    val routeTag: String?
) {
    val durationMillis: Long?
        get() = endTime?.let { it.toEpochMilli() - startTime.toEpochMilli() }

    val distanceMiles: Double
        get() = distanceMeters * 0.000621371

    val averagePaceSecondsPerMile: Double?
        get() {
            val miles = distanceMiles
            val seconds = (durationMillis ?: return null) / 1000.0
            return if (miles > 0) seconds / miles else null
        }

    val activityLabel: String
        get() = when (activityType) {
            ActivityType.RUN -> "Run"
            ActivityType.DOG_WALK -> "Dog Walk"
        }

    val weatherSummary: String?
        get() {
            val parts = listOfNotNull(
                weatherTemp?.name?.lowercase()?.replaceFirstChar { it.uppercase() },
                weatherCondition?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
            )
            return if (parts.isEmpty()) null else parts.joinToString(", ")
        }
}
