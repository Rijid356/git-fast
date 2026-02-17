package com.gitfast.app.util

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
