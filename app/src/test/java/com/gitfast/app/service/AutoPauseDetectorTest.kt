package com.gitfast.app.service

import com.gitfast.app.data.model.GpsPoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class AutoPauseDetectorTest {

    private lateinit var detector: AutoPauseDetector

    @Before
    fun setup() {
        detector = AutoPauseDetector()
    }

    private fun point(timeMs: Long, speed: Float? = null) = GpsPoint(
        latitude = 40.0,
        longitude = -74.0,
        timestamp = Instant.ofEpochMilli(timeMs),
        accuracy = 5f,
        speed = speed
    )

    @Test
    fun `triggers pause after sustained low speed`() {
        // Feed 5 points over 5 seconds, all below threshold
        val baseTime = 10_000L
        for (i in 0..4) {
            val result = detector.analyzePoint(
                point(baseTime + i * 1200L, speed = 0.2f),
                isCurrentlyAutoPaused = false
            )
            if (i < 2) {
                // Not enough points yet (need MIN_POINTS_FOR_PAUSE = 3)
                assertFalse("Should not pause with only ${i + 1} points", result.shouldAutoPause)
            }
        }
        // After 5 points spanning >5s, all below threshold => should pause
        val finalResult = detector.analyzePoint(
            point(baseTime + 6000L, speed = 0.1f),
            isCurrentlyAutoPaused = false
        )
        assertTrue("Should trigger pause after sustained low speed", finalResult.shouldAutoPause)
    }

    @Test
    fun `does NOT trigger on single slow reading`() {
        val result = detector.analyzePoint(
            point(10_000L, speed = 0.1f),
            isCurrentlyAutoPaused = false
        )
        assertFalse("Single slow point should not trigger pause", result.shouldAutoPause)
    }

    @Test
    fun `does NOT trigger pause when some points are above threshold`() {
        val baseTime = 10_000L
        // 3 slow points
        for (i in 0..2) {
            detector.analyzePoint(
                point(baseTime + i * 1500L, speed = 0.2f),
                isCurrentlyAutoPaused = false
            )
        }
        // 1 fast point within the window
        val result = detector.analyzePoint(
            point(baseTime + 4500L, speed = 2.0f),
            isCurrentlyAutoPaused = false
        )
        assertFalse("Should not pause when a fast point exists in window", result.shouldAutoPause)
    }

    @Test
    fun `triggers resume after movement detected`() {
        // First auto-pause (setup)
        val baseTime = 10_000L

        // Now simulate being auto-paused and detecting movement
        val result = detector.analyzePoint(
            point(baseTime, speed = 2.0f),
            isCurrentlyAutoPaused = true
        )
        assertTrue("Should trigger resume when speed is above threshold", result.shouldAutoResume)
    }

    @Test
    fun `resume is faster than pause - asymmetric thresholds`() {
        // Resume only needs ANY point above threshold in last 3s
        // Pause needs ALL points below threshold in last 5s (min 3 points)

        // Test resume: single fast point triggers resume
        val resumeResult = detector.analyzePoint(
            point(10_000L, speed = 1.0f),
            isCurrentlyAutoPaused = true
        )
        assertTrue("Single fast point should trigger resume", resumeResult.shouldAutoResume)

        // Reset and test pause: single slow point does NOT trigger pause
        detector.reset()
        val pauseResult = detector.analyzePoint(
            point(10_000L, speed = 0.1f),
            isCurrentlyAutoPaused = false
        )
        assertFalse("Single slow point should NOT trigger pause", pauseResult.shouldAutoPause)
    }

    @Test
    fun `handles null speed gracefully - returns no-op`() {
        val result = detector.analyzePoint(
            point(10_000L, speed = null),
            isCurrentlyAutoPaused = false
        )
        assertFalse("Null speed should not trigger pause", result.shouldAutoPause)
        assertFalse("Null speed should not trigger resume", result.shouldAutoResume)
    }

    @Test
    fun `handles null speed when auto-paused - returns no-op`() {
        val result = detector.analyzePoint(
            point(10_000L, speed = null),
            isCurrentlyAutoPaused = true
        )
        assertFalse("Null speed should not trigger pause", result.shouldAutoPause)
        assertFalse("Null speed should not trigger resume", result.shouldAutoResume)
    }

    @Test
    fun `reset clears state`() {
        val baseTime = 10_000L
        // Accumulate some slow points
        for (i in 0..3) {
            detector.analyzePoint(
                point(baseTime + i * 1500L, speed = 0.1f),
                isCurrentlyAutoPaused = false
            )
        }

        // Reset
        detector.reset()

        // After reset, a single slow point should not trigger pause
        val result = detector.analyzePoint(
            point(baseTime + 10_000L, speed = 0.1f),
            isCurrentlyAutoPaused = false
        )
        assertFalse("After reset, should not have enough data to pause", result.shouldAutoPause)
    }

    @Test
    fun `sliding window evicts old points`() {
        val baseTime = 10_000L

        // Add slow points at time 0
        for (i in 0..2) {
            detector.analyzePoint(
                point(baseTime + i * 1000L, speed = 0.1f),
                isCurrentlyAutoPaused = false
            )
        }

        // Jump 15 seconds ahead (beyond 10s retention window)
        // Old points should be evicted, so even though we add slow points
        // we start fresh
        val result = detector.analyzePoint(
            point(baseTime + 25_000L, speed = 0.1f),
            isCurrentlyAutoPaused = false
        )
        assertFalse(
            "Old points should be evicted, not enough recent data to pause",
            result.shouldAutoPause
        )
    }

    @Test
    fun `does not trigger resume when speed below threshold during auto-pause`() {
        val result = detector.analyzePoint(
            point(10_000L, speed = 0.2f),
            isCurrentlyAutoPaused = true
        )
        assertFalse("Slow speed should not trigger resume", result.shouldAutoResume)
    }
}
