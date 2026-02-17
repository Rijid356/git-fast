package com.gitfast.app.util

import com.gitfast.app.data.model.Lap
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WorkoutPhase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class PhaseAnalyzerTest {

    // --- Helper ---

    private fun makePhase(
        type: PhaseType,
        startMillis: Long,
        endMillis: Long?,
        distanceMeters: Double,
        laps: List<Lap> = emptyList()
    ): WorkoutPhase {
        return WorkoutPhase(
            id = "phase-${type.name}",
            type = type,
            startTime = Instant.ofEpochMilli(startMillis),
            endTime = endMillis?.let { Instant.ofEpochMilli(it) },
            distanceMeters = distanceMeters,
            steps = 0,
            laps = laps
        )
    }

    private fun makeLap(number: Int, durationSeconds: Int, distanceMeters: Double = 400.0): Lap {
        val start = Instant.ofEpochMilli(1000L * number * 300)
        val end = start.plusSeconds(durationSeconds.toLong())
        return Lap(
            id = "lap-$number",
            lapNumber = number,
            startTime = start,
            endTime = end,
            distanceMeters = distanceMeters,
            steps = 0
        )
    }

    // --- Tests ---

    @Test
    fun `analyzePhases single warmup phase produces one display item`() {
        // 5 minutes warmup, 500 meters
        val phases = listOf(
            makePhase(PhaseType.WARMUP, 0, 300_000, 500.0)
        )
        val result = PhaseAnalyzer.analyzePhases(phases)

        assertEquals(1, result.size)
        val item = result[0]
        assertEquals(PhaseType.WARMUP, item.type)
        assertEquals("WARMUP", item.label)
        assertEquals(formatElapsedTime(300), item.durationFormatted)
        assertEquals(formatDistance(500.0), item.distanceFormatted)
    }

    @Test
    fun `analyzePhases three phases produces correct labels`() {
        val laps = listOf(makeLap(1, 120), makeLap(2, 115), makeLap(3, 118))
        val phases = listOf(
            makePhase(PhaseType.WARMUP, 0, 300_000, 500.0),
            makePhase(PhaseType.LAPS, 300_000, 660_000, 1200.0, laps),
            makePhase(PhaseType.COOLDOWN, 660_000, 960_000, 400.0)
        )
        val result = PhaseAnalyzer.analyzePhases(phases)

        assertEquals(3, result.size)
        assertEquals("WARMUP", result[0].label)
        assertEquals("LAPS (3)", result[1].label)
        assertEquals("COOLDOWN", result[2].label)
    }

    @Test
    fun `analyzePhases LAPS label includes lap count`() {
        val laps = listOf(
            makeLap(1, 120),
            makeLap(2, 115),
            makeLap(3, 118),
            makeLap(4, 122),
            makeLap(5, 110)
        )
        val phases = listOf(
            makePhase(PhaseType.LAPS, 0, 600_000, 2000.0, laps)
        )
        val result = PhaseAnalyzer.analyzePhases(phases)

        assertEquals(1, result.size)
        assertEquals("LAPS (5)", result[0].label)
    }

    @Test
    fun `analyzePhases missing endTime shows dashes pace`() {
        // Phase with null endTime: duration = 0, pace should be "-- /mi"
        val phases = listOf(
            makePhase(PhaseType.COOLDOWN, 0, null, 0.0)
        )
        val result = PhaseAnalyzer.analyzePhases(phases)

        assertEquals(1, result.size)
        val item = result[0]
        assertEquals("-- /mi", item.paceFormatted)
        assertEquals(formatElapsedTime(0), item.durationFormatted)
    }
}
