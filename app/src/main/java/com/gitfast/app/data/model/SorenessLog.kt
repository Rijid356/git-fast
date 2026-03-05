package com.gitfast.app.data.model

import java.time.Instant
import java.time.LocalDate

data class SorenessLog(
    val id: String,
    val date: LocalDate,
    val muscleIntensities: Map<MuscleGroup, SorenessIntensity>,
    val notes: String? = null,
    val xpAwarded: Int = 0,
    val createdAt: Instant = Instant.now(),
) {
    val muscleGroups: Set<MuscleGroup> get() = muscleIntensities.keys
    val maxIntensity: SorenessIntensity? get() = muscleIntensities.values.maxByOrNull { it.ordinal }
}
