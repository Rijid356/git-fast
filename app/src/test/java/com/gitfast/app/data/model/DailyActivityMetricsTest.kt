package com.gitfast.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyActivityMetricsTest {

    @Test
    fun `zero values give zero progress`() {
        val metrics = DailyActivityMetrics()
        assertEquals(0f, metrics.activeMinutesProgress, 0.001f)
        assertEquals(0f, metrics.distanceProgress, 0.001f)
        assertEquals(0f, metrics.activeDaysProgress, 0.001f)
    }

    @Test
    fun `half progress calculates correctly`() {
        val metrics = DailyActivityMetrics(
            activeMinutes = 11,
            activeMinutesGoal = 22,
            distanceMiles = 0.75,
            distanceGoal = 1.5,
            activeDaysThisWeek = 2,
            activeDaysGoal = 4,
        )
        assertEquals(0.5f, metrics.activeMinutesProgress, 0.001f)
        assertEquals(0.5f, metrics.distanceProgress, 0.001f)
        assertEquals(0.5f, metrics.activeDaysProgress, 0.001f)
    }

    @Test
    fun `full progress at 100 percent`() {
        val metrics = DailyActivityMetrics(
            activeMinutes = 22,
            activeMinutesGoal = 22,
            distanceMiles = 1.5,
            distanceGoal = 1.5,
            activeDaysThisWeek = 5,
            activeDaysGoal = 5,
        )
        assertEquals(1.0f, metrics.activeMinutesProgress, 0.001f)
        assertEquals(1.0f, metrics.distanceProgress, 0.001f)
        assertEquals(1.0f, metrics.activeDaysProgress, 0.001f)
    }

    @Test
    fun `overflow progress exceeds 1`() {
        val metrics = DailyActivityMetrics(
            activeMinutes = 44,
            activeMinutesGoal = 22,
            distanceMiles = 3.0,
            distanceGoal = 1.5,
            activeDaysThisWeek = 7,
            activeDaysGoal = 5,
        )
        assertEquals(2.0f, metrics.activeMinutesProgress, 0.001f)
        assertEquals(2.0f, metrics.distanceProgress, 0.001f)
        assertEquals(1.4f, metrics.activeDaysProgress, 0.001f)
    }

    @Test
    fun `zero goal returns zero progress`() {
        val metrics = DailyActivityMetrics(
            activeMinutes = 10,
            activeMinutesGoal = 0,
            distanceMiles = 1.0,
            distanceGoal = 0.0,
            activeDaysThisWeek = 3,
            activeDaysGoal = 0,
        )
        assertEquals(0f, metrics.activeMinutesProgress, 0.001f)
        assertEquals(0f, metrics.distanceProgress, 0.001f)
        assertEquals(0f, metrics.activeDaysProgress, 0.001f)
    }

    @Test
    fun `default values match AHA guidelines`() {
        val metrics = DailyActivityMetrics()
        assertEquals(22, metrics.activeMinutesGoal)
        assertEquals(1.5, metrics.distanceGoal, 0.001)
        assertEquals(5, metrics.activeDaysGoal)
    }
}
