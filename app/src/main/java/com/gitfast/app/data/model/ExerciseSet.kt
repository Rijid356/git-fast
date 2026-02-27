package com.gitfast.app.data.model

import java.time.Instant

data class ExerciseSet(
    val id: String,
    val sessionId: String,
    val exerciseId: String,
    val setNumber: Int,
    val reps: Int,
    val weightLbs: Double? = null,
    val durationSeconds: Int? = null,
    val isWarmup: Boolean = false,
    val completedAt: Instant,
)
