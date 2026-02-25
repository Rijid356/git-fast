package com.gitfast.app.service

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatPace

data class NotificationContent(
    val title: String,
    val collapsedText: String,
    val expandedText: String
)

fun buildNotificationContent(state: WorkoutTrackingState): NotificationContent {
    val isPaused = state.isPaused
    val isDogActivity = state.activityType.isDogActivity

    val distance = formatDistance(state.distanceMeters)
    val currentPace = state.currentPaceSecondsPerMile?.let { formatPace(it) } ?: "-- /mi"
    val avgPace = state.averagePaceSecondsPerMile?.let { formatPace(it) } ?: "-- /mi"

    val activityLabel = when (state.activityType) {
        ActivityType.DOG_WALK -> "Dog Walk"
        ActivityType.DOG_RUN -> "Dog Run"
        else -> "Running"
    }

    val title = if (state.isHomeArrivalPaused) {
        "git-fast \u2022 Home!"
    } else if (isPaused) {
        "git-fast \u2022 Paused"
    } else {
        "git-fast \u2022 $activityLabel"
    }

    val collapsedText = if (isDogActivity) {
        distance
    } else {
        "$distance \u2022 $currentPace"
    }

    val expandedText = buildString {
        append("Distance    $distance")
        if (!isDogActivity) {
            append("\n")
            append("Pace        $currentPace\n")
            append("Avg Pace    $avgPace")
        }
        if (state.isHomeArrivalPaused) append("\n(arrived home)")
        else if (isPaused) append("\n(paused)")
    }

    return NotificationContent(
        title = title,
        collapsedText = collapsedText,
        expandedText = expandedText
    )
}
