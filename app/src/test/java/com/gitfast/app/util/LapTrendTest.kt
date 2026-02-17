package com.gitfast.app.util

import com.gitfast.app.ui.detail.LapTrend
import org.junit.Assert.assertEquals
import org.junit.Test

class LapTrendTest {

    @Test
    fun `calculateTrend fewer than 3 laps returns TOO_FEW_LAPS`() {
        assertEquals(LapTrend.TOO_FEW_LAPS, LapAnalyzer.calculateTrend(emptyList()))
        assertEquals(LapTrend.TOO_FEW_LAPS, LapAnalyzer.calculateTrend(listOf(120)))
        assertEquals(LapTrend.TOO_FEW_LAPS, LapAnalyzer.calculateTrend(listOf(120, 115)))
    }

    @Test
    fun `calculateTrend decreasing durations returns GETTING_FASTER`() {
        // 120 -> 115 -> 110: slope = -5, which is < -2
        val result = LapAnalyzer.calculateTrend(listOf(120, 115, 110))
        assertEquals(LapTrend.GETTING_FASTER, result)
    }

    @Test
    fun `calculateTrend increasing durations returns GETTING_SLOWER`() {
        // 110 -> 115 -> 120: slope = +5, which is > +2
        val result = LapAnalyzer.calculateTrend(listOf(110, 115, 120))
        assertEquals(LapTrend.GETTING_SLOWER, result)
    }

    @Test
    fun `calculateTrend similar durations returns CONSISTENT`() {
        // 120 -> 119 -> 121: slope within +/- 2 threshold
        val result = LapAnalyzer.calculateTrend(listOf(120, 119, 121))
        assertEquals(LapTrend.CONSISTENT, result)
    }

    @Test
    fun `calculateTrend all identical times returns CONSISTENT`() {
        val result = LapAnalyzer.calculateTrend(listOf(120, 120, 120, 120))
        assertEquals(LapTrend.CONSISTENT, result)
    }

    @Test
    fun `calculateTrend dramatic improvement returns GETTING_FASTER`() {
        // 200 -> 180 -> 160 -> 140 -> 120: slope = -20, well below -2
        val result = LapAnalyzer.calculateTrend(listOf(200, 180, 160, 140, 120))
        assertEquals(LapTrend.GETTING_FASTER, result)
    }
}
