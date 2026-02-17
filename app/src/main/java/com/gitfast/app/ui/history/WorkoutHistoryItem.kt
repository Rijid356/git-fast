package com.gitfast.app.ui.history

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.util.DateFormatter
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import java.time.Instant

data class WorkoutHistoryItem(
    val workoutId: String,
    val startTime: Instant,
    val dateFormatted: String,
    val timeFormatted: String,
    val relativeDateFormatted: String,
    val distanceFormatted: String,
    val durationFormatted: String,
    val avgPaceFormatted: String,
    val activityType: ActivityType,
    val subtitle: String?,
    val xpEarned: Int = 0,
)

fun Workout.toHistoryItem(): WorkoutHistoryItem {
    val duration = durationMillis?.let { (it / 1000).toInt() }
    val pace = averagePaceSecondsPerMile?.toInt()

    val subtitle = when (activityType) {
        ActivityType.DOG_WALK -> listOfNotNull(dogName, routeTag).joinToString(" \u00B7 ").ifEmpty { null }
        ActivityType.RUN -> null
    }

    return WorkoutHistoryItem(
        workoutId = id,
        startTime = startTime,
        dateFormatted = DateFormatter.shortDate(startTime),
        timeFormatted = DateFormatter.timeOfDay(startTime),
        relativeDateFormatted = DateFormatter.relativeDate(startTime),
        distanceFormatted = formatDistance(distanceMeters),
        durationFormatted = duration?.let { formatElapsedTime(it) } ?: "--:--",
        avgPaceFormatted = pace?.let { formatPace(it) } ?: "-- /mi",
        activityType = activityType,
        subtitle = subtitle,
    )
}
