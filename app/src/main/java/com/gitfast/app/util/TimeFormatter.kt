package com.gitfast.app.util

import com.gitfast.app.data.model.DistanceUnit

fun formatElapsedTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun formatPace(secondsPerMile: Int): String {
    val minutes = secondsPerMile / 60
    val seconds = secondsPerMile % 60
    return String.format("%d:%02d /mi", minutes, seconds)
}

/**
 * Format pace with unit conversion. Input is always seconds-per-mile
 * (from PaceCalculator). Converts to seconds-per-km when displaying km.
 */
fun formatPace(secondsPerMile: Int, unit: DistanceUnit): String {
    val displaySeconds = when (unit) {
        DistanceUnit.MILES -> secondsPerMile
        DistanceUnit.KILOMETERS -> (secondsPerMile / 1.60934).toInt()
    }
    val minutes = displaySeconds / 60
    val seconds = displaySeconds % 60
    val label = when (unit) {
        DistanceUnit.MILES -> "/mi"
        DistanceUnit.KILOMETERS -> "/km"
    }
    return String.format("%d:%02d %s", minutes, seconds, label)
}
