package com.gitfast.app.data.model

import java.time.Instant

data class Workout(
    val id: String,
    val startTime: Instant,
    val endTime: Instant?,
    val totalSteps: Int,
    val distanceMeters: Double,
    val status: WorkoutStatus,
    val phases: List<WorkoutPhase>,
    val gpsPoints: List<GpsPoint>
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
}
