package com.gitfast.app.service

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace

data class NotificationContent(
    val title: String,
    val collapsedText: String,
    val expandedText: String
)

fun buildNotificationContent(state: WorkoutTrackingState): NotificationContent {
    val isPaused = state.isPaused
    val isDogWalk = state.activityType == ActivityType.DOG_WALK

    val elapsed = formatElapsedTime(state.elapsedSeconds)
    val distance = formatDistance(state.distanceMeters)
    val currentPace = state.currentPaceSecondsPerMile?.let { formatPace(it) } ?: "-- /mi"
    val avgPace = state.averagePaceSecondsPerMile?.let { formatPace(it) } ?: "-- /mi"

    val activityLabel = if (isDogWalk) "Dog Walk" else "Running"

    val title = if (state.isHomeArrivalPaused) {
        "git-fast \u2022 Home!"
    } else if (isPaused) {
        "git-fast \u2022 Paused"
    } else {
        "git-fast \u2022 $activityLabel"
    }

    val collapsedText = if (isDogWalk) {
        "$elapsed \u2022 $distance"
    } else {
        "$elapsed \u2022 $distance \u2022 $currentPace"
    }

    val expandedText = buildString {
        append("Time        $elapsed")
        if (state.isHomeArrivalPaused) append("  (home)")
        else if (isPaused) append("  (paused)")
        append("\n")
        append("Distance    $distance")
        if (!isDogWalk) {
            append("\n")
            append("Pace        $currentPace\n")
            append("Avg Pace    $avgPace")
        }
    }

    return NotificationContent(
        title = title,
        collapsedText = collapsedText,
        expandedText = expandedText
    )
}
