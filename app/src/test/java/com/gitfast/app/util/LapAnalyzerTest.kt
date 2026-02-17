package com.gitfast.app.util

import com.gitfast.app.data.model.Lap
import com.gitfast.app.ui.detail.LapTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LapAnalyzerTest {

    // --- Helper ---

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
    fun `analyze returns null for empty laps list`() {
        val result = LapAnalyzer.analyze(emptyList())
        assertNull(result)
    }

    @Test
    fun `analyze single lap — best and slowest are same, no delta`() {
        val laps = listOf(makeLap(1, 120))
        val result = LapAnalyzer.analyze(laps)

        assertNotNull(result)
        result!!

        assertEquals(1, result.lapCount)
        assertEquals(1, result.bestLapNumber)
        assertEquals(1, result.slowestLapNumber)
        assertEquals(formatElapsedTime(120), result.bestLapTime)
        assertEquals(formatElapsedTime(120), result.slowestLapTime)
        assertEquals(formatElapsedTime(120), result.averageLapTime)

        // Single lap: isFastest = true, isSlowest = false (size == 1 guard)
        val display = result.laps.first()
        assertTrue(display.isFastest)
        assertFalse(display.isSlowest)

        // First lap always has null delta
        assertNull(display.deltaFormatted)
        assertNull(display.deltaSeconds)
    }

    @Test
    fun `analyze identifies correct fastest lap`() {
        val laps = listOf(
            makeLap(1, 130),
            makeLap(2, 110), // fastest
            makeLap(3, 125)
        )
        val result = LapAnalyzer.analyze(laps)!!

        assertEquals(2, result.bestLapNumber)
        assertEquals(formatElapsedTime(110), result.bestLapTime)
        assertTrue(result.laps[1].isFastest)
        assertFalse(result.laps[0].isFastest)
        assertFalse(result.laps[2].isFastest)
    }

    @Test
    fun `analyze identifies correct slowest lap`() {
        val laps = listOf(
            makeLap(1, 130),
            makeLap(2, 145), // slowest
            makeLap(3, 120)
        )
        val result = LapAnalyzer.analyze(laps)!!

        assertEquals(2, result.slowestLapNumber)
        assertEquals(formatElapsedTime(145), result.slowestLapTime)
        assertTrue(result.laps[1].isSlowest)
        assertFalse(result.laps[0].isSlowest)
        assertFalse(result.laps[2].isSlowest)
    }

    @Test
    fun `analyze average lap time calculated correctly`() {
        // Laps: 100s, 120s, 130s => average = 116 (int truncation of 116.666...)
        val laps = listOf(
            makeLap(1, 100),
            makeLap(2, 120),
            makeLap(3, 130)
        )
        val result = LapAnalyzer.analyze(laps)!!

        val expectedAvg = (100 + 120 + 130) / 3 // 116
        assertEquals(expectedAvg, result.averageLapSeconds)
        assertEquals(formatElapsedTime(expectedAvg), result.averageLapTime)
    }

    @Test
    fun `analyze first lap has null delta`() {
        val laps = listOf(
            makeLap(1, 120),
            makeLap(2, 130)
        )
        val result = LapAnalyzer.analyze(laps)!!

        assertNull(result.laps[0].deltaSeconds)
        assertNull(result.laps[0].deltaFormatted)
    }

    @Test
    fun `analyze subsequent laps have correct deltas`() {
        // Lap 1: 120s, Lap 2: 115s (delta = -5), Lap 3: 125s (delta = +10)
        val laps = listOf(
            makeLap(1, 120),
            makeLap(2, 115),
            makeLap(3, 125)
        )
        val result = LapAnalyzer.analyze(laps)!!

        // Lap 2 delta: 115 - 120 = -5
        assertEquals(-5, result.laps[1].deltaSeconds)
        assertEquals(LapAnalyzer.formatDelta(-5), result.laps[1].deltaFormatted)

        // Lap 3 delta: 125 - 115 = +10
        assertEquals(10, result.laps[2].deltaSeconds)
        assertEquals(LapAnalyzer.formatDelta(10), result.laps[2].deltaFormatted)
    }

    @Test
    fun `analyze isFastest and isSlowest flags set correctly`() {
        val laps = listOf(
            makeLap(1, 130),
            makeLap(2, 100), // fastest
            makeLap(3, 150), // slowest
            makeLap(4, 120)
        )
        val result = LapAnalyzer.analyze(laps)!!

        // Only lap 2 (index 1) should be fastest
        assertFalse(result.laps[0].isFastest)
        assertTrue(result.laps[1].isFastest)
        assertFalse(result.laps[2].isFastest)
        assertFalse(result.laps[3].isFastest)

        // Only lap 3 (index 2) should be slowest
        assertFalse(result.laps[0].isSlowest)
        assertFalse(result.laps[1].isSlowest)
        assertTrue(result.laps[2].isSlowest)
        assertFalse(result.laps[3].isSlowest)
    }

    @Test
    fun `analyze chart points have correct lap numbers and durations`() {
        val laps = listOf(
            makeLap(1, 120),
            makeLap(2, 115),
            makeLap(3, 125)
        )
        val result = LapAnalyzer.analyze(laps)!!

        assertEquals(3, result.trendChartPoints.size)

        assertEquals(1, result.trendChartPoints[0].lapNumber)
        assertEquals(120, result.trendChartPoints[0].durationSeconds)

        assertEquals(2, result.trendChartPoints[1].lapNumber)
        assertEquals(115, result.trendChartPoints[1].durationSeconds)

        assertEquals(3, result.trendChartPoints[2].lapNumber)
        assertEquals(125, result.trendChartPoints[2].durationSeconds)
    }

    @Test
    fun `analyze with only one lap isSlowest is false`() {
        val laps = listOf(makeLap(1, 90))
        val result = LapAnalyzer.analyze(laps)!!

        // When there's only 1 lap, isSlowest should be false because of `laps.size > 1` guard
        assertFalse(result.laps[0].isSlowest)
        // isFastest is still true because it is the minimum
        assertTrue(result.laps[0].isFastest)
    }
}
