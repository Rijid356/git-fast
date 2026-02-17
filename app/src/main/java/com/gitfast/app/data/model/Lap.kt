package com.gitfast.app.data.model

import java.time.Instant

data class Lap(
    val id: String,
    val lapNumber: Int,
    val startTime: Instant,
    val endTime: Instant?,
    val distanceMeters: Double,
    val steps: Int
) {
    val durationMillis: Long?
        get() = endTime?.let { it.toEpochMilli() - startTime.toEpochMilli() }
}
