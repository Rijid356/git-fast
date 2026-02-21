package com.gitfast.app.util

import com.gitfast.app.data.model.Lap
import com.gitfast.app.ui.detail.LapAnalysis
import com.gitfast.app.ui.detail.LapChartPoint
import com.gitfast.app.ui.detail.LapDisplayItem
import com.gitfast.app.ui.detail.LapTrend

/**
 * Pure functions that take raw lap data and produce display-ready analysis.
 * No Android dependencies — fully unit testable.
 */
object LapAnalyzer {

    /**
     * Build complete lap analysis from domain Lap objects.
     */
    fun analyze(laps: List<Lap>): LapAnalysis? {
        if (laps.isEmpty()) return null

        val durations = laps.map { lap ->
            lap.durationMillis?.let { (it / 1000).toInt() } ?: 0
        }

        val bestIndex = durations.indexOf(durations.min())
        val slowestIndex = durations.indexOf(durations.max())
        val avgSeconds = durations.average().toInt()

        val displayItems = laps.mapIndexed { index, lap ->
            val durationSeconds = durations[index]
            val deltaSeconds = if (index > 0) {
                durationSeconds - durations[index - 1]
            } else null

            LapDisplayItem(
                id = lap.id,
                lapNumber = lap.lapNumber,
                timeFormatted = formatElapsedTime(durationSeconds),
                distanceFormatted = formatDistance(lap.distanceMeters),
                paceFormatted = PaceCalculator.segmentPace(
                    lap.startTime,
                    lap.endTime ?: lap.startTime,
                    lap.distanceMeters
                )?.let { formatPace(it) } ?: "-- /mi",
                deltaFormatted = deltaSeconds?.let { formatDelta(it) },
                deltaSeconds = deltaSeconds,
                isFastest = index == bestIndex,
                isSlowest = index == slowestIndex && laps.size > 1
            )
        }

        val chartPoints = laps.mapIndexed { index, _ ->
            LapChartPoint(
                lapNumber = index + 1,
                durationSeconds = durations[index]
            )
        }

        return LapAnalysis(
            laps = displayItems,
            lapCount = laps.size,
            bestLapTime = formatElapsedTime(durations.min()),
            bestLapNumber = bestIndex + 1,
            slowestLapTime = formatElapsedTime(durations.max()),
            slowestLapNumber = slowestIndex + 1,
            averageLapTime = formatElapsedTime(avgSeconds),
            averageLapSeconds = avgSeconds,
            trend = calculateTrend(durations),
            trendChartPoints = chartPoints
        )
    }

    /**
     * Determine if the runner is getting faster, slower, or staying consistent.
     *
     * Uses simple linear regression on lap durations.
     * Positive slope = getting slower, negative slope = getting faster.
     * If the slope is within ±2 seconds per lap, it's "consistent."
     */
    fun calculateTrend(durationSeconds: List<Int>): LapTrend {
        if (durationSeconds.size < 3) return LapTrend.TOO_FEW_LAPS

        // Simple linear regression: y = mx + b
        // x = lap number (0-indexed), y = duration in seconds
        val n = durationSeconds.size.toDouble()
        val xMean = (n - 1) / 2.0
        val yMean = durationSeconds.average()

        var numerator = 0.0
        var denominator = 0.0
        durationSeconds.forEachIndexed { i, duration ->
            val xDiff = i - xMean
            numerator += xDiff * (duration - yMean)
            denominator += xDiff * xDiff
        }

        val slope = if (denominator != 0.0) numerator / denominator else 0.0

        // Threshold: ±2 seconds per lap is considered consistent
        return when {
            slope < -2.0 -> LapTrend.GETTING_FASTER
            slope > 2.0 -> LapTrend.GETTING_SLOWER
            else -> LapTrend.CONSISTENT
        }
    }

    /**
     * Format a delta value with direction indicator.
     * Negative = faster (improvement), Positive = slower.
     */
    fun formatDelta(deltaSeconds: Int): String {
        return when {
            deltaSeconds < 0 -> "▲ ${deltaSeconds}s"
            deltaSeconds > 0 -> "▼ +${deltaSeconds}s"
            else -> "= 0s"
        }
    }
}
