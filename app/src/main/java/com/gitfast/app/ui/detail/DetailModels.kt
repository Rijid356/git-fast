package com.gitfast.app.ui.detail

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.Workout
import com.gitfast.app.util.DateFormatter
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace

data class WorkoutDetailItem(
    val workoutId: String,
    val dateFormatted: String,
    val timeFormatted: String,
    val distanceFormatted: String,
    val durationFormatted: String,
    val avgPaceFormatted: String,
    val stepsFormatted: String,
    val gpsPointCount: Int,
    val avgGpsAccuracy: Float?,
    val routePoints: List<LatLngPoint>,
    val routeBounds: RouteBounds?,
    // Dog walk fields
    val activityType: ActivityType,
    val dogName: String?,
    val routeTag: String?,
    val weatherSummary: String?,
    val energyLevel: EnergyLevel?,
    val notes: String?,
    val xpEarned: Int = 0,
    val xpBreakdown: String? = null,
)

data class LatLngPoint(
    val latitude: Double,
    val longitude: Double
)

data class RouteBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
)

fun Workout.toDetailItem(): WorkoutDetailItem {
    val points = gpsPoints.map { LatLngPoint(it.latitude, it.longitude) }

    val bounds = if (points.size >= 2) {
        RouteBounds(
            minLat = points.minOf { it.latitude },
            maxLat = points.maxOf { it.latitude },
            minLng = points.minOf { it.longitude },
            maxLng = points.maxOf { it.longitude }
        )
    } else {
        null
    }

    val avgAccuracy = if (gpsPoints.isNotEmpty()) {
        gpsPoints.map { it.accuracy }.average().toFloat()
    } else {
        null
    }

    val durationSeconds = durationMillis?.let { (it / 1000).toInt() }

    return WorkoutDetailItem(
        workoutId = id,
        dateFormatted = DateFormatter.shortDate(startTime),
        timeFormatted = DateFormatter.timeOfDay(startTime),
        distanceFormatted = formatDistance(distanceMeters),
        durationFormatted = durationSeconds?.let { formatElapsedTime(it) } ?: "--:--",
        avgPaceFormatted = averagePaceSecondsPerMile?.let { formatPace(it.toInt()) } ?: "-- /mi",
        stepsFormatted = if (totalSteps > 0) totalSteps.toString() else "--",
        gpsPointCount = gpsPoints.size,
        avgGpsAccuracy = avgAccuracy,
        routePoints = points,
        routeBounds = bounds,
        // Dog walk fields
        activityType = activityType,
        dogName = dogName,
        routeTag = routeTag,
        weatherSummary = weatherSummary,
        energyLevel = energyLevel,
        notes = notes
    )
}
