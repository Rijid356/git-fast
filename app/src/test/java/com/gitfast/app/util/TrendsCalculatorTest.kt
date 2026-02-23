package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TrendsCalculatorTest {

    private val today = LocalDate.of(2026, 2, 22) // a Sunday

    @Test
    fun `groupByWeek returns 8 buckets`() {
        val result = TrendsCalculator.groupByWeek(emptyList(), weeksBack = 8, today = today)
        assertEquals(8, result.size)
    }

    @Test
    fun `workouts grouped into correct weeks`() {
        // Today is 2026-02-22 (Sunday). Current week starts Mon 2026-02-16.
        val mondayThisWeek = LocalDate.of(2026, 2, 16)
        val mondayLastWeek = LocalDate.of(2026, 2, 9)

        val workouts = listOf(
            createRun("r1", dateMs = mondayThisWeek.toEpochMs()),
            createRun("r2", dateMs = mondayThisWeek.plusDays(2).toEpochMs()),
            createRun("r3", dateMs = mondayLastWeek.toEpochMs()),
        )

        val result = TrendsCalculator.groupByWeek(workouts, weeksBack = 8, today = today)

        // Last bucket = current week, second to last = previous week
        assertEquals(2, result.last().workoutCount)
        assertEquals(1, result[result.size - 2].workoutCount)
    }

    @Test
    fun `empty weeks have zero counts`() {
        val result = TrendsCalculator.groupByWeek(emptyList(), weeksBack = 8, today = today)
        assertTrue(result.all { it.workoutCount == 0 })
        assertTrue(result.all { it.totalDistanceMeters == 0.0 })
        assertTrue(result.all { it.totalDurationMillis == 0L })
    }

    @Test
    fun `groupByMonth returns 6 buckets`() {
        val result = TrendsCalculator.groupByMonth(emptyList(), monthsBack = 6, today = today)
        assertEquals(6, result.size)
    }

    @Test
    fun `workouts grouped into correct months`() {
        val feb = LocalDate.of(2026, 2, 5)
        val jan = LocalDate.of(2026, 1, 15)

        val workouts = listOf(
            createRun("r1", dateMs = feb.toEpochMs()),
            createRun("r2", dateMs = feb.toEpochMs()),
            createRun("r3", dateMs = jan.toEpochMs()),
        )

        val result = TrendsCalculator.groupByMonth(workouts, monthsBack = 6, today = today)

        // Last bucket = Feb, second to last = Jan
        assertEquals(2, result.last().workoutCount)
        assertEquals(1, result[result.size - 2].workoutCount)
    }

    @Test
    fun `distance summed correctly per period`() {
        val mondayThisWeek = LocalDate.of(2026, 2, 16)

        val workouts = listOf(
            createRun("r1", dateMs = mondayThisWeek.toEpochMs(), distanceMeters = 3000.0),
            createRun("r2", dateMs = mondayThisWeek.plusDays(1).toEpochMs(), distanceMeters = 5000.0),
        )

        val result = TrendsCalculator.groupByWeek(workouts, weeksBack = 8, today = today)
        assertEquals(8000.0, result.last().totalDistanceMeters, 0.01)
    }

    @Test
    fun `workout count correct per period`() {
        val mondayThisWeek = LocalDate.of(2026, 2, 16)

        val workouts = listOf(
            createRun("r1", dateMs = mondayThisWeek.toEpochMs()),
            createRun("r2", dateMs = mondayThisWeek.plusDays(1).toEpochMs()),
            createRun("r3", dateMs = mondayThisWeek.plusDays(2).toEpochMs()),
        )

        val result = TrendsCalculator.groupByWeek(workouts, weeksBack = 8, today = today)
        assertEquals(3, result.last().workoutCount)
    }

    @Test
    fun `avg pace computed for runs only`() {
        val mondayThisWeek = LocalDate.of(2026, 2, 16)

        // Two runs: 3000m in 15min = 8:02/mi pace; 3000m in 18min = 9:39/mi pace
        val workouts = listOf(
            createRun("r1", dateMs = mondayThisWeek.toEpochMs(), distanceMeters = 3000.0, durationMs = 900_000),
            createRun("r2", dateMs = mondayThisWeek.plusDays(1).toEpochMs(), distanceMeters = 3000.0, durationMs = 1080_000),
            createWalk("w1", dateMs = mondayThisWeek.plusDays(2).toEpochMs(), distanceMeters = 1500.0, durationMs = 1200_000),
        )

        val result = TrendsCalculator.groupByWeek(workouts, weeksBack = 8, today = today)
        val pace = result.last().avgPaceSecondsPerMile
        assertNotNull(pace)
        assertTrue(pace!! > 0)
    }

    @Test
    fun `avg pace null when no runs in period`() {
        val mondayThisWeek = LocalDate.of(2026, 2, 16)

        val workouts = listOf(
            createWalk("w1", dateMs = mondayThisWeek.toEpochMs()),
        )

        val result = TrendsCalculator.groupByWeek(workouts, weeksBack = 8, today = today)
        assertNull(result.last().avgPaceSecondsPerMile)
    }

    @Test
    fun `compare returns correct delta percentages`() {
        val current = TrendsCalculator.PeriodSummary(
            label = "2/16",
            startDate = LocalDate.of(2026, 2, 16),
            workoutCount = 5,
            totalDistanceMeters = 15000.0,
            totalDurationMillis = 5400_000,
            avgPaceSecondsPerMile = 480,
        )
        val previous = TrendsCalculator.PeriodSummary(
            label = "2/9",
            startDate = LocalDate.of(2026, 2, 9),
            workoutCount = 4,
            totalDistanceMeters = 10000.0,
            totalDurationMillis = 3600_000,
            avgPaceSecondsPerMile = 520,
        )

        val result = TrendsCalculator.compare(current, previous)

        assertEquals(50.0, result.distanceDeltaPercent!!, 0.1)   // 15000 vs 10000 = +50%
        assertEquals(25.0, result.workoutCountDeltaPercent!!, 0.1) // 5 vs 4 = +25%
        assertEquals(50.0, result.durationDeltaPercent!!, 0.1)    // 5400 vs 3600 = +50%
        // Pace went from 520 to 480: (480-520)/520 = -7.7%
        assertTrue(result.paceDeltaPercent!! < 0)
    }

    @Test
    fun `compare with no previous returns null deltas`() {
        val current = TrendsCalculator.PeriodSummary(
            label = "2/16",
            startDate = LocalDate.of(2026, 2, 16),
            workoutCount = 3,
            totalDistanceMeters = 5000.0,
            totalDurationMillis = 1800_000,
            avgPaceSecondsPerMile = 500,
        )

        val result = TrendsCalculator.compare(current, null)

        assertNull(result.distanceDeltaPercent)
        assertNull(result.workoutCountDeltaPercent)
        assertNull(result.durationDeltaPercent)
        assertNull(result.paceDeltaPercent)
    }

    @Test
    fun `partial current week included`() {
        // Today is Sunday 2026-02-22, current week started Mon 2026-02-16
        // Workout on Wednesday 2026-02-18
        val wednesday = LocalDate.of(2026, 2, 18)
        val workouts = listOf(
            createRun("r1", dateMs = wednesday.toEpochMs()),
        )

        val result = TrendsCalculator.groupByWeek(workouts, weeksBack = 8, today = today)
        assertEquals(1, result.last().workoutCount)
    }

    // --- Helpers ---

    private fun LocalDate.toEpochMs(): Long =
        this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun createRun(
        id: String,
        dateMs: Long = 0,
        distanceMeters: Double = 3000.0,
        durationMs: Long = 1800_000,
    ) = Workout(
        id = id,
        startTime = Instant.ofEpochMilli(dateMs),
        endTime = Instant.ofEpochMilli(dateMs + durationMs),
        totalSteps = 0,
        distanceMeters = distanceMeters,
        status = WorkoutStatus.COMPLETED,
        activityType = ActivityType.RUN,
        phases = emptyList(),
        gpsPoints = emptyList(),
        dogName = null,
        notes = null,
        weatherCondition = null,
        weatherTemp = null,
        energyLevel = null,
        routeTag = null,
    )

    private fun createWalk(
        id: String,
        dateMs: Long = 0,
        distanceMeters: Double = 1500.0,
        durationMs: Long = 1800_000,
    ) = Workout(
        id = id,
        startTime = Instant.ofEpochMilli(dateMs),
        endTime = Instant.ofEpochMilli(dateMs + durationMs),
        totalSteps = 2000,
        distanceMeters = distanceMeters,
        status = WorkoutStatus.COMPLETED,
        activityType = ActivityType.DOG_WALK,
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
