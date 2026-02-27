package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutPhase
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StreakCalculatorUnifiedTest {

    private val zone = ZoneId.systemDefault()

    private fun workoutOn(date: LocalDate): Workout {
        val instant = date.atStartOfDay(zone).toInstant()
        return Workout(
            id = java.util.UUID.randomUUID().toString(),
            startTime = instant,
            endTime = instant.plusSeconds(3600),
            totalSteps = 0,
            distanceMeters = 5000.0,
            activityType = ActivityType.RUN,
            status = WorkoutStatus.COMPLETED,
            phases = emptyList(),
            gpsPoints = emptyList(),
            dogName = null,
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = null,
        )
    }

    private val today = LocalDate.of(2026, 3, 6)

    // --- getCurrentStreak with exercise sessions ---

    @Test
    fun `unified getCurrentStreak with only workouts`() {
        val workouts = listOf(
            workoutOn(today),
            workoutOn(today.minusDays(1)),
            workoutOn(today.minusDays(2)),
        )
        val streak = StreakCalculator.getCurrentStreak(workouts, emptySet(), today)
        assertEquals(3, streak)
    }

    @Test
    fun `unified getCurrentStreak with only exercise sessions`() {
        val sessionDates = setOf(today, today.minusDays(1))
        val streak = StreakCalculator.getCurrentStreak(emptyList(), sessionDates, today)
        assertEquals(2, streak)
    }

    @Test
    fun `unified getCurrentStreak merges workouts and sessions`() {
        // Day 1 (today): exercise session only
        // Day 2 (yesterday): workout only
        // Day 3 (2 days ago): exercise session only
        val workouts = listOf(workoutOn(today.minusDays(1)))
        val sessionDates = setOf(today, today.minusDays(2))
        val streak = StreakCalculator.getCurrentStreak(workouts, sessionDates, today)
        assertEquals(3, streak)
    }

    @Test
    fun `unified getCurrentStreak returns 0 with no activity`() {
        val streak = StreakCalculator.getCurrentStreak(emptyList(), emptySet(), today)
        assertEquals(0, streak)
    }

    @Test
    fun `unified getCurrentStreak gap breaks streak`() {
        // Today: exercise, 2 days ago: workout, yesterday: nothing → streak = 1
        val workouts = listOf(workoutOn(today.minusDays(2)))
        val sessionDates = setOf(today)
        val streak = StreakCalculator.getCurrentStreak(workouts, sessionDates, today)
        assertEquals(1, streak)
    }

    @Test
    fun `unified getCurrentStreak starts from yesterday if no today activity`() {
        val workouts = listOf(
            workoutOn(today.minusDays(1)),
            workoutOn(today.minusDays(2)),
        )
        val sessionDates = setOf(today.minusDays(3))
        val streak = StreakCalculator.getCurrentStreak(workouts, sessionDates, today)
        assertEquals(3, streak)
    }

    // --- getLongestStreak with exercise sessions ---

    @Test
    fun `unified getLongestStreak with only workouts`() {
        val workouts = listOf(
            workoutOn(today),
            workoutOn(today.minusDays(1)),
            workoutOn(today.minusDays(5)),
        )
        val longest = StreakCalculator.getLongestStreak(workouts, emptySet())
        assertEquals(2, longest)
    }

    @Test
    fun `unified getLongestStreak merges activities for longer streak`() {
        // Workouts on day 1, 3; sessions on day 2 → fills the gap → streak = 3
        val workouts = listOf(
            workoutOn(today),
            workoutOn(today.minusDays(2)),
        )
        val sessionDates = setOf(today.minusDays(1))
        val longest = StreakCalculator.getLongestStreak(workouts, sessionDates)
        assertEquals(3, longest)
    }

    @Test
    fun `unified getLongestStreak returns 0 with no activity`() {
        assertEquals(0, StreakCalculator.getLongestStreak(emptyList(), emptySet()))
    }

    @Test
    fun `unified getLongestStreak handles duplicate dates`() {
        // Workout and session on same day should not double-count
        val workouts = listOf(workoutOn(today))
        val sessionDates = setOf(today)
        val longest = StreakCalculator.getLongestStreak(workouts, sessionDates)
        assertEquals(1, longest)
    }
}
