package com.gitfast.app.data.model

import java.time.Instant

data class WorkoutPhase(
    val id: String,
    val type: PhaseType,
    val startTime: Instant,
    val endTime: Instant?,
    val distanceMeters: Double,
    val steps: Int,
    val laps: List<Lap>
)
