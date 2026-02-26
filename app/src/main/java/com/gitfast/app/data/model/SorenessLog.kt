package com.gitfast.app.data.model

import java.time.Instant
import java.time.LocalDate

data class SorenessLog(
    val id: String,
    val date: LocalDate,
    val muscleGroups: Set<MuscleGroup>,
    val intensity: SorenessIntensity,
    val notes: String? = null,
    val xpAwarded: Int = 0,
    val createdAt: Instant = Instant.now(),
)
