package com.gitfast.app.data.model

import java.time.Instant

data class ExerciseSession(
    val id: String,
    val startTime: Instant,
    val endTime: Instant? = null,
    val sets: List<ExerciseSet> = emptyList(),
    val notes: String? = null,
    val xpAwarded: Int = 0,
)
