package com.gitfast.app.service

import com.gitfast.app.data.model.GpsPoint
import javax.inject.Inject

class AutoPauseDetector @Inject constructor() {

    companion object {
        const val SPEED_THRESHOLD = 0.5f // m/s (~1.1 mph)
        const val PAUSE_WINDOW_MS = 5_000L // 5 seconds sustained stillness
        const val RESUME_WINDOW_MS = 3_000L // 3 seconds — resume should be snappier
        const val WINDOW_RETENTION_MS = 10_000L // keep last 10s of points
        const val MIN_POINTS_FOR_PAUSE = 3
    }

    data class AnalysisResult(
        val shouldAutoPause: Boolean = false,
        val shouldAutoResume: Boolean = false
    )

    private val recentPoints = mutableListOf<TimedSpeed>()

    private data class TimedSpeed(
        val timestampMs: Long,
        val speed: Float?
    )

    fun analyzePoint(point: GpsPoint, isCurrentlyAutoPaused: Boolean): AnalysisResult {
        val nowMs = point.timestamp.toEpochMilli()

        recentPoints.add(TimedSpeed(nowMs, point.speed))

        // Evict points older than retention window
        recentPoints.removeAll { nowMs - it.timestampMs > WINDOW_RETENTION_MS }

        // If speed data is unavailable on this point, return no-op
        if (point.speed == null) {
            return AnalysisResult()
        }

        return if (isCurrentlyAutoPaused) {
            analyzeForResume(nowMs)
        } else {
            analyzeForPause(nowMs)
        }
    }

    private fun analyzeForPause(nowMs: Long): AnalysisResult {
        val cutoff = nowMs - PAUSE_WINDOW_MS
        val windowPoints = recentPoints.filter { it.timestampMs >= cutoff }

        // Need at least MIN_POINTS_FOR_PAUSE points with speed data in the window
        val pointsWithSpeed = windowPoints.filter { it.speed != null }
        if (pointsWithSpeed.size < MIN_POINTS_FOR_PAUSE) {
            return AnalysisResult()
        }

        // ALL points with speed in the pause window must be below threshold
        val allBelowThreshold = pointsWithSpeed.all { it.speed!! < SPEED_THRESHOLD }

        return AnalysisResult(shouldAutoPause = allBelowThreshold)
    }

    private fun analyzeForResume(nowMs: Long): AnalysisResult {
        val cutoff = nowMs - RESUME_WINDOW_MS
        val windowPoints = recentPoints.filter { it.timestampMs >= cutoff }

        // ANY point in the resume window above threshold triggers resume
        val anyAboveThreshold = windowPoints.any { speed ->
            speed.speed != null && speed.speed >= SPEED_THRESHOLD
        }

        return AnalysisResult(shouldAutoResume = anyAboveThreshold)
    }

    fun reset() {
        recentPoints.clear()
    }
}
