package com.gitfast.app.util

import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WorkoutPhase

object PhaseAnalyzer {

    data class PhaseDisplayItem(
        val type: PhaseType,
        val label: String,              // "WARMUP", "LAPS (5)", "COOLDOWN"
        val durationFormatted: String,  // "5:32"
        val distanceFormatted: String,  // "0.42 mi"
        val paceFormatted: String       // "13:10 /mi"
    )

    fun analyzePhases(phases: List<WorkoutPhase>): List<PhaseDisplayItem> {
        return phases.map { phase ->
            val durationMillis = phase.endTime?.let {
                it.toEpochMilli() - phase.startTime.toEpochMilli()
            }
            val durationSeconds = durationMillis?.let { (it / 1000).toInt() } ?: 0
            val lapCount = phase.laps.size

            PhaseDisplayItem(
                type = phase.type,
                label = when (phase.type) {
                    PhaseType.WARMUP -> "WARMUP"
                    PhaseType.LAPS -> "LAPS ($lapCount)"
                    PhaseType.COOLDOWN -> "COOLDOWN"
                },
                durationFormatted = formatElapsedTime(durationSeconds),
                distanceFormatted = formatDistance(phase.distanceMeters),
                paceFormatted = PaceCalculator.segmentPace(
                    phase.startTime,
                    phase.endTime ?: phase.startTime,
                    phase.distanceMeters
                )?.let { formatPace(it) } ?: "-- /mi"
            )
        }
    }
}
