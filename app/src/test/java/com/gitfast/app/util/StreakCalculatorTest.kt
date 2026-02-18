package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StreakCalculatorTest {

    private val zone = ZoneId.systemDefault()

    private fun workoutOn(date: LocalDate): Workout {
        val instant = date.atStartOfDay(zone).toInstant().plusSeconds(3600) // 1am
        return Workout(
            id = "w-${date}",
            startTime = instant,
            endTime = instant.plusSeconds(1800),
            distanceMeters = 5000.0,
            status = WorkoutStatus.COMPLETED,
            activityType = ActivityType.RUN,
            phases = emptyList(),
            gpsPoints = emptyList(),
        )
    }

    @Test
    fun `getCurrentStreak with no workouts returns 0`() {
        assertEquals(0, StreakCalculator.getCurrentStreak(emptyList()))
    }

    @Test
    fun `getCurrentStreak with one workout today returns 1`() {
        val today = LocalDate.of(2026, 2, 18)
        val workouts = listOf(workoutOn(today))
        assertEquals(1, StreakCalculator.getCurrentStreak(workouts, today))
    }

    @Test
    fun `getCurrentStreak with 3 consecutive days returns 3`() {
        val today = LocalDate.of(2026, 2, 18)
        val workouts = listOf(
            workoutOn(today),
            workoutOn(today.minusDays(1)),
            workoutOn(today.minusDays(2)),
        )
        assertEquals(3, StreakCalculator.getCurrentStreak(workouts, today))
    }

    @Test
    fun `getCurrentStreak with gap in middle only counts from most recent`() {
        val today = LocalDate.of(2026, 2, 18)
        val workouts = listOf(
            workoutOn(today),
            workoutOn(today.minusDays(1)),
            // gap on day -2
            workoutOn(today.minusDays(3)),
            workoutOn(today.minusDays(4)),
        )
        assertEquals(2, StreakCalculator.getCurrentStreak(workouts, today))
    }

    @Test
    fun `getCurrentStreak when no workout today but yesterday has one`() {
        val today = LocalDate.of(2026, 2, 18)
        val workouts = listOf(
            workoutOn(today.minusDays(1)),
            workoutOn(today.minusDays(2)),
            workoutOn(today.minusDays(3)),
        )
        assertEquals(3, StreakCalculator.getCurrentStreak(workouts, today))
    }

    @Test
    fun `getCurrentStreak returns 0 when last workout was 2 days ago`() {
        val today = LocalDate.of(2026, 2, 18)
        val workouts = listOf(
            workoutOn(today.minusDays(2)),
        )
        assertEquals(0, StreakCalculator.getCurrentStreak(workouts, today))
    }

    @Test
    fun `getMultiplier returns 1_0 for streak of 0`() {
        assertEquals(1.0, StreakCalculator.getMultiplier(0), 0.001)
    }

    @Test
    fun `getMultiplier returns 1_0 for streak of 1`() {
        assertEquals(1.0, StreakCalculator.getMultiplier(1), 0.001)
    }

    @Test
    fun `getMultiplier returns 1_1 for streak of 2`() {
        assertEquals(1.1, StreakCalculator.getMultiplier(2), 0.001)
    }

    @Test
    fun `getMultiplier returns 1_2 for streak of 3`() {
        assertEquals(1.2, StreakCalculator.getMultiplier(3), 0.001)
    }

    @Test
    fun `getMultiplier returns 1_5 for streak of 6`() {
        assertEquals(1.5, StreakCalculator.getMultiplier(6), 0.001)
    }

    @Test
    fun `getMultiplier caps at 1_5 for streak of 10`() {
        assertEquals(1.5, StreakCalculator.getMultiplier(10), 0.001)
    }

    @Test
    fun `getMultiplierLabel formats correctly`() {
        assertEquals("1.0x", StreakCalculator.getMultiplierLabel(1))
        assertEquals("1.1x", StreakCalculator.getMultiplierLabel(2))
        assertEquals("1.3x", StreakCalculator.getMultiplierLabel(4))
        assertEquals("1.5x", StreakCalculator.getMultiplierLabel(6))
        assertEquals("1.5x", StreakCalculator.getMultiplierLabel(20))
    }

    @Test
    fun `XpCalculator applies streak multiplier`() {
        val baseResult = XpCalculator.calculateXp(
            distanceMeters = 1610.0,
            durationMillis = 600_000,
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = false,
            hasCooldown = false,
            hasLaps = false,
            streakDays = 0,
        )

        val streakResult = XpCalculator.calculateXp(
            distanceMeters = 1610.0,
            durationMillis = 600_000,
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = false,
            hasCooldown = false,
            hasLaps = false,
            streakDays = 3,
        )

        // 3-day streak = 1.2x multiplier
        assertEquals(1.2, streakResult.streakMultiplier, 0.001)
        assertEquals(3, streakResult.streakDays)
        assert(streakResult.totalXp > baseResult.totalXp) {
            "Streak XP (${streakResult.totalXp}) should be greater than base XP (${baseResult.totalXp})"
        }
    }

    @Test
    fun `XpCalculator streak breakdown line present when streak active`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1610.0,
            durationMillis = 600_000,
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = false,
            hasCooldown = false,
            hasLaps = false,
            streakDays = 5,
        )

        val streakLine = result.breakdown.find { it.contains("streak") }
        assert(streakLine != null) { "Should have a streak breakdown line" }
        assert(streakLine!!.contains("1.4x")) { "Should mention 1.4x multiplier, got: $streakLine" }
    }

    @Test
    fun `XpCalculator no streak line when streak is 0 or 1`() {
        val result = XpCalculator.calculateXp(
            distanceMeters = 1610.0,
            durationMillis = 600_000,
            activityType = ActivityType.RUN,
            lapCount = 0,
            hasWarmup = false,
            hasCooldown = false,
            hasLaps = false,
            streakDays = 1,
        )

        val streakLine = result.breakdown.find { it.contains("streak") }
        assertEquals(null, streakLine)
    }
}
