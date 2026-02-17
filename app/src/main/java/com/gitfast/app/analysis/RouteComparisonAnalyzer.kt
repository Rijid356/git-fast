package com.gitfast.app.analysis

import com.gitfast.app.data.model.Workout
import com.gitfast.app.util.DateFormatter
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime

object RouteComparisonAnalyzer {

    data class RouteComparisonItem(
        val workoutId: String,
        val dateFormatted: String,
        val durationFormatted: String,
        val distanceFormatted: String,
        val deltaFormatted: String?,
        val deltaMillis: Long?,
        val isCurrentWalk: Boolean
    )

    fun compare(
        currentWalk: Workout,
        previousWalks: List<Workout>,
        maxComparisons: Int = 5
    ): List<RouteComparisonItem> {
        val currentDuration = currentWalk.durationMillis ?: return emptyList()
        val currentDurationSeconds = (currentDuration / 1000).toInt()

        val currentItem = RouteComparisonItem(
            workoutId = currentWalk.id,
            dateFormatted = "Today",
            durationFormatted = formatElapsedTime(currentDurationSeconds),
            distanceFormatted = formatDistance(currentWalk.distanceMeters),
            deltaFormatted = null,
            deltaMillis = null,
            isCurrentWalk = true
        )

        val previousItems = previousWalks
            .take(maxComparisons)
            .map { walk ->
                val walkDuration = walk.durationMillis ?: 0L
                val delta = walkDuration - currentDuration
                val deltaSeconds = (delta / 1000).toInt()
                val walkDurationSeconds = (walkDuration / 1000).toInt()

                RouteComparisonItem(
                    workoutId = walk.id,
                    dateFormatted = DateFormatter.shortDate(walk.startTime),
                    durationFormatted = formatElapsedTime(walkDurationSeconds),
                    distanceFormatted = formatDistance(walk.distanceMeters),
                    deltaFormatted = formatDelta(deltaSeconds),
                    deltaMillis = delta,
                    isCurrentWalk = false
                )
            }

        return listOf(currentItem) + previousItems
    }

    private fun formatDelta(deltaSeconds: Int): String {
        val absDelta = kotlin.math.abs(deltaSeconds)
        val minutes = absDelta / 60
        val seconds = absDelta % 60
        val formatted = if (minutes > 0) {
            String.format("%d:%02d", minutes, seconds)
        } else {
            "${seconds}s"
        }
        return when {
            deltaSeconds < 0 -> "-$formatted"
            deltaSeconds > 0 -> "+$formatted"
            else -> "0s"
        }
    }
}
