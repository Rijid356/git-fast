package com.gitfast.app.service

import com.gitfast.app.data.model.GpsPoint
import javax.inject.Inject

class AutoSprintDetector @Inject constructor() {

    companion object {
        const val SPRINT_START_THRESHOLD = 2.5f  // m/s (~5.6 mph) — running
        const val SPRINT_END_THRESHOLD = 2.0f    // m/s (~4.5 mph) — walking
        const val START_WINDOW_MS = 3_000L       // 3s sustained above threshold to start
        const val END_WINDOW_MS = 5_000L         // 5s sustained below threshold to end
        const val WINDOW_RETENTION_MS = 10_000L  // keep 10s of history
        const val MIN_POINTS = 3                 // minimum GPS readings for detection
    }

    data class AnalysisResult(
        val shouldStartSprint: Boolean = false,
        val shouldEndSprint: Boolean = false,
    )

    private val recentPoints = mutableListOf<TimedSpeed>()

    private data class TimedSpeed(
        val timestampMs: Long,
        val speed: Float?,
    )

    fun analyzePoint(point: GpsPoint, isCurrentlySprinting: Boolean): AnalysisResult {
        val nowMs = point.timestamp.toEpochMilli()

        recentPoints.add(TimedSpeed(nowMs, point.speed))

        // Evict points older than retention window
        recentPoints.removeAll { nowMs - it.timestampMs > WINDOW_RETENTION_MS }

        // If speed data is unavailable on this point, return no-op
        if (point.speed == null) {
            return AnalysisResult()
        }

        return if (isCurrentlySprinting) {
            analyzeForEnd(nowMs)
        } else {
            analyzeForStart(nowMs)
        }
    }

    private fun analyzeForStart(nowMs: Long): AnalysisResult {
        val cutoff = nowMs - START_WINDOW_MS
        val windowPoints = recentPoints.filter { it.timestampMs >= cutoff }

        val pointsWithSpeed = windowPoints.filter { it.speed != null }
        if (pointsWithSpeed.size < MIN_POINTS) {
            return AnalysisResult()
        }

        val allAboveThreshold = pointsWithSpeed.all { it.speed!! >= SPRINT_START_THRESHOLD }
        return AnalysisResult(shouldStartSprint = allAboveThreshold)
    }

    private fun analyzeForEnd(nowMs: Long): AnalysisResult {
        val cutoff = nowMs - END_WINDOW_MS
        val windowPoints = recentPoints.filter { it.timestampMs >= cutoff }

        val pointsWithSpeed = windowPoints.filter { it.speed != null }
        if (pointsWithSpeed.size < MIN_POINTS) {
            return AnalysisResult()
        }

        val allBelowThreshold = pointsWithSpeed.all { it.speed!! < SPRINT_END_THRESHOLD }
        return AnalysisResult(shouldEndSprint = allBelowThreshold)
    }

    fun reset() {
        recentPoints.clear()
    }
}
