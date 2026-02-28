package com.gitfast.app.service

import com.gitfast.app.data.model.GpsPoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class AutoSprintDetectorTest {

    private lateinit var detector: AutoSprintDetector

    @Before
    fun setup() {
        detector = AutoSprintDetector()
    }

    private fun point(timeMs: Long, speed: Float? = null) = GpsPoint(
        latitude = 40.0,
        longitude = -74.0,
        timestamp = Instant.ofEpochMilli(timeMs),
        accuracy = 5f,
        speed = speed,
    )

    @Test
    fun `does not start sprint with insufficient points`() {
        val result = detector.analyzePoint(
            point(1000L, speed = 3.0f),
            isCurrentlySprinting = false,
        )
        assertFalse(result.shouldStartSprint)
        assertFalse(result.shouldEndSprint)
    }

    @Test
    fun `starts sprint after sustained high speed`() {
        val base = 10_000L
        var result = AutoSprintDetector.AnalysisResult()
        for (i in 0..3) {
            result = detector.analyzePoint(
                point(base + i * 800L, speed = 3.0f),
                isCurrentlySprinting = false,
            )
        }
        assertTrue(result.shouldStartSprint)
    }

    @Test
    fun `does not start sprint if speed below threshold`() {
        val base = 10_000L
        var result = AutoSprintDetector.AnalysisResult()
        for (i in 0..3) {
            result = detector.analyzePoint(
                point(base + i * 800L, speed = 2.0f),
                isCurrentlySprinting = false,
            )
        }
        assertFalse(result.shouldStartSprint)
    }

    @Test
    fun `ends sprint after sustained low speed`() {
        val base = 10_000L
        var result = AutoSprintDetector.AnalysisResult()
        for (i in 0..4) {
            result = detector.analyzePoint(
                point(base + i * 1200L, speed = 1.0f),
                isCurrentlySprinting = true,
            )
        }
        assertTrue(result.shouldEndSprint)
    }

    @Test
    fun `does not end sprint if speed still high`() {
        val base = 10_000L
        var result = AutoSprintDetector.AnalysisResult()
        for (i in 0..4) {
            result = detector.analyzePoint(
                point(base + i * 1200L, speed = 3.0f),
                isCurrentlySprinting = true,
            )
        }
        assertFalse(result.shouldEndSprint)
    }

    @Test
    fun `null speed does not trigger sprint start`() {
        val base = 10_000L
        var result = AutoSprintDetector.AnalysisResult()
        for (i in 0..4) {
            result = detector.analyzePoint(
                point(base + i * 800L, speed = null),
                isCurrentlySprinting = false,
            )
        }
        assertFalse(result.shouldStartSprint)
    }

    @Test
    fun `null speed timeout ends sprint after 10 seconds`() {
        val base = 10_000L
        // First point with speed to set lastNonNullSpeedMs
        detector.analyzePoint(
            point(base, speed = 3.0f),
            isCurrentlySprinting = true,
        )

        // Null speed points over 10+ seconds
        val result = detector.analyzePoint(
            point(base + 11_000L, speed = null),
            isCurrentlySprinting = true,
        )
        assertTrue("Sprint should end after 10s of null speed", result.shouldEndSprint)
    }

    @Test
    fun `null speed does not end sprint before timeout`() {
        val base = 10_000L
        detector.analyzePoint(
            point(base, speed = 3.0f),
            isCurrentlySprinting = true,
        )

        val result = detector.analyzePoint(
            point(base + 5_000L, speed = null),
            isCurrentlySprinting = true,
        )
        assertFalse("Sprint should not end before 10s null timeout", result.shouldEndSprint)
    }

    @Test
    fun `max duration timeout ends sprint after 2 minutes`() {
        val base = 10_000L
        // Feed high-speed points spread across 2+ minutes
        detector.analyzePoint(
            point(base, speed = 3.0f),
            isCurrentlySprinting = true,
        )

        val result = detector.analyzePoint(
            point(base + 121_000L, speed = 3.0f),
            isCurrentlySprinting = true,
        )
        assertTrue("Sprint should end after 2 min max duration", result.shouldEndSprint)
    }

    @Test
    fun `max duration does not trigger before 2 minutes`() {
        val base = 10_000L
        detector.analyzePoint(
            point(base, speed = 3.0f),
            isCurrentlySprinting = true,
        )

        val result = detector.analyzePoint(
            point(base + 60_000L, speed = 3.0f),
            isCurrentlySprinting = true,
        )
        assertFalse("Sprint should not end at 1 min", result.shouldEndSprint)
    }

    @Test
    fun `reset clears all state`() {
        val base = 10_000L
        // Build up sprint state
        for (i in 0..3) {
            detector.analyzePoint(
                point(base + i * 800L, speed = 3.0f),
                isCurrentlySprinting = true,
            )
        }

        detector.reset()

        // After reset, a single point should not trigger anything
        val result = detector.analyzePoint(
            point(base + 100_000L, speed = 3.0f),
            isCurrentlySprinting = false,
        )
        assertFalse(result.shouldStartSprint)
        assertFalse(result.shouldEndSprint)
    }

    @Test
    fun `sprint start time tracked from first sprinting call`() {
        val base = 10_000L
        // First call as sprinting sets sprintStartMs
        detector.analyzePoint(
            point(base, speed = 3.0f),
            isCurrentlySprinting = true,
        )

        // Second call still within duration — no timeout
        val result = detector.analyzePoint(
            point(base + 30_000L, speed = 3.0f),
            isCurrentlySprinting = true,
        )
        assertFalse(result.shouldEndSprint)
    }
}
