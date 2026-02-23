package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StatsCalculatorTest {

    // --- Helper ---

    private fun buildWorkout(
        distanceMeters: Double = 1609.34, // ~1 mile
        durationMillis: Long = 600_000L, // 10 min
        activityType: ActivityType = ActivityType.RUN,
        startTime: Instant = Instant.now(),
    ): Workout {
        return Workout(
            id = java.util.UUID.randomUUID().toString(),
            startTime = startTime,
            endTime = startTime.plusMillis(durationMillis),
            totalSteps = 0,
            distanceMeters = distanceMeters,
            status = WorkoutStatus.COMPLETED,
            activityType = activityType,
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

    // =========================================================================
    // Speed Tests
    // =========================================================================

    @Test
    fun `speed - empty list returns 1`() {
        assertEquals(1, StatsCalculator.calculateSpeed(emptyList()))
    }

    @Test
    fun `speed - fast run yields high stat`() {
        // 5:00/mi pace = 300s/mi. Distance: 5 miles in 25 min
        val run = buildWorkout(
            distanceMeters = 5 * 1609.34,
            durationMillis = 25 * 60_000L,
        )
        val stat = StatsCalculator.calculateSpeed(listOf(run))
        assertTrue("Fast run should yield high stat, got $stat", stat >= 80)
    }

    @Test
    fun `speed - slow run yields low stat`() {
        // ~15:00/mi pace = 900s/mi. Distance: 1 mile in 15 min
        val run = buildWorkout(
            distanceMeters = 1609.34,
            durationMillis = 15 * 60_000L,
        )
        val stat = StatsCalculator.calculateSpeed(listOf(run))
        assertTrue("Slow run should yield low stat, got $stat", stat <= 15)
    }

    @Test
    fun `speed - sub-5 pace caps at 99`() {
        // 4:00/mi pace. Distance: 4 miles in 16 min
        val run = buildWorkout(
            distanceMeters = 4 * 1609.34,
            durationMillis = 16 * 60_000L,
        )
        val stat = StatsCalculator.calculateSpeed(listOf(run))
        assertEquals(99, stat)
    }

    @Test
    fun `speed - 16 min pace floors at 1`() {
        // 16:00/mi pace = 960s/mi. Distance: 1 mile in 16 min
        val run = buildWorkout(
            distanceMeters = 1609.34,
            durationMillis = 16 * 60_000L,
        )
        val stat = StatsCalculator.calculateSpeed(listOf(run))
        assertEquals(1, stat)
    }

    @Test
    fun `speed - worse than 16 min pace still floors at 1`() {
        // 20:00/mi pace. Distance: 1 mile in 20 min
        val run = buildWorkout(
            distanceMeters = 1609.34,
            durationMillis = 20 * 60_000L,
        )
        val stat = StatsCalculator.calculateSpeed(listOf(run))
        assertEquals(1, stat)
    }

    @Test
    fun `speed - blend of best and median`() {
        // Best: 6:00/mi (360s), Median: 10:00/mi (600s)
        // Effective = 0.6*360 + 0.4*600 = 216 + 240 = 456s → between 300(99) and 420(75)
        val fast = buildWorkout(
            distanceMeters = 3 * 1609.34,
            durationMillis = 18 * 60_000L, // 6 min/mi
        )
        val slow = buildWorkout(
            distanceMeters = 1609.34,
            durationMillis = 10 * 60_000L, // 10 min/mi
        )
        val medium = buildWorkout(
            distanceMeters = 2 * 1609.34,
            durationMillis = 16 * 60_000L, // 8 min/mi
        )
        val stat = StatsCalculator.calculateSpeed(listOf(fast, slow, medium))
        assertTrue("Blended stat should be moderate-high, got $stat", stat in 50..90)
    }

    @Test
    fun `speed - 7 min pace yields around 75`() {
        // 7:00/mi = 420s
        val run = buildWorkout(
            distanceMeters = 3 * 1609.34,
            durationMillis = 21 * 60_000L,
        )
        val stat = StatsCalculator.calculateSpeed(listOf(run))
        assertTrue("7 min/mi should be around 75, got $stat", stat in 70..80)
    }

    @Test
    fun `speed - 9 min pace yields around 50`() {
        // 9:00/mi = 540s
        val run = buildWorkout(
            distanceMeters = 3 * 1609.34,
            durationMillis = 27 * 60_000L,
        )
        val stat = StatsCalculator.calculateSpeed(listOf(run))
        assertTrue("9 min/mi should be around 50, got $stat", stat in 45..55)
    }

    @Test
    fun `speed - invalid zero-distance paces filtered out`() {
        val zeroDistance = buildWorkout(distanceMeters = 0.0, durationMillis = 60_000L)
        val normal = buildWorkout(
            distanceMeters = 3 * 1609.34,
            durationMillis = 21 * 60_000L, // 7 min/mi
        )
        val stat = StatsCalculator.calculateSpeed(listOf(zeroDistance, normal))
        assertTrue("Should ignore zero-distance, got $stat", stat in 70..80)
    }

    // =========================================================================
    // Endurance Tests
    // =========================================================================

    @Test
    fun `endurance - empty list returns 1`() {
        assertEquals(1, StatsCalculator.calculateEndurance(emptyList()))
    }

    @Test
    fun `endurance - long distance yields high stat`() {
        // 10 miles, 90 min
        val workout = buildWorkout(
            distanceMeters = 10 * 1609.34,
            durationMillis = 90 * 60_000L,
        )
        val stat = StatsCalculator.calculateEndurance(listOf(workout))
        assertTrue("Long distance should yield high stat, got $stat", stat >= 70)
    }

    @Test
    fun `endurance - short distance yields low stat`() {
        // 0.5 miles, 8 min
        val workout = buildWorkout(
            distanceMeters = 0.5 * 1609.34,
            durationMillis = 8 * 60_000L,
        )
        val stat = StatsCalculator.calculateEndurance(listOf(workout))
        assertTrue("Short distance should yield low stat, got $stat", stat <= 25)
    }

    @Test
    fun `endurance - distance and duration blend`() {
        // Long distance but short duration: 5 miles in 20 min
        val workout = buildWorkout(
            distanceMeters = 5 * 1609.34,
            durationMillis = 20 * 60_000L,
        )
        val stat = StatsCalculator.calculateEndurance(listOf(workout))
        // Distance component high (~75), duration low (~25-35) → blended ~50-55
        assertTrue("Blended endurance should be moderate, got $stat", stat in 30..70)
    }

    @Test
    fun `endurance - max vs recent average weighting`() {
        // One long run + several short runs
        val longRun = buildWorkout(
            distanceMeters = 10 * 1609.34,
            durationMillis = 90 * 60_000L,
        )
        val shortRuns = (1..5).map {
            buildWorkout(
                distanceMeters = 1 * 1609.34,
                durationMillis = 10 * 60_000L,
            )
        }
        val allWorkouts = listOf(longRun) + shortRuns
        val stat = StatsCalculator.calculateEndurance(allWorkouts)
        // Max is high but avg is pulled down → moderate-high
        assertTrue("Mixed distances should yield moderate stat, got $stat", stat in 35..80)
    }

    @Test
    fun `endurance - dog walks count`() {
        val dogWalk = buildWorkout(
            distanceMeters = 3 * 1609.34,
            durationMillis = 45 * 60_000L,
            activityType = ActivityType.DOG_WALK,
        )
        val stat = StatsCalculator.calculateEndurance(listOf(dogWalk))
        assertTrue("Dog walks should count for endurance, got $stat", stat > 1)
    }

    // =========================================================================
    // Consistency Tests
    // =========================================================================

    @Test
    fun `consistency - no workouts returns 1`() {
        assertEquals(1, StatsCalculator.calculateConsistency(emptyList()))
    }

    @Test
    fun `consistency - daily for 30 days yields high stat`() {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val workouts = (0 until 28).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            buildWorkout(
                startTime = date.atStartOfDay(zone).toInstant().plusSeconds(3600),
            )
        }
        val stat = StatsCalculator.calculateConsistency(workouts, now)
        assertTrue("Daily workouts should yield high stat, got $stat", stat >= 85)
    }

    @Test
    fun `consistency - 3x per week yields moderate stat`() {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        // ~12 workouts over 30 days (every 2-3 days)
        val workouts = (0 until 12).map { i ->
            val date = today.minusDays((i * 2.5).toLong())
            buildWorkout(
                startTime = date.atStartOfDay(zone).toInstant().plusSeconds(3600),
            )
        }
        val stat = StatsCalculator.calculateConsistency(workouts, now)
        assertTrue("3x/week should yield moderate stat, got $stat", stat in 25..75)
    }

    @Test
    fun `consistency - single workout yields low stat`() {
        val now = Instant.now()
        val workout = buildWorkout(startTime = now.minusSeconds(86400)) // yesterday
        val stat = StatsCalculator.calculateConsistency(listOf(workout), now)
        assertTrue("Single workout should yield low stat, got $stat", stat <= 20)
    }

    @Test
    fun `consistency - workouts older than 30 days excluded`() {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        // All workouts 31+ days ago
        val workouts = (31..40).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            buildWorkout(
                startTime = date.atStartOfDay(zone).toInstant().plusSeconds(3600),
            )
        }
        val stat = StatsCalculator.calculateConsistency(workouts, now)
        assertEquals("Old workouts should be excluded", 1, stat)
    }

    @Test
    fun `consistency - streak calculation works`() {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        // 7-day streak ending today
        val workouts = (0 until 7).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            buildWorkout(
                startTime = date.atStartOfDay(zone).toInstant().plusSeconds(3600),
            )
        }
        val stat = StatsCalculator.calculateConsistency(workouts, now)
        // 7 workouts → frequency around 25; 7-day streak → streak stat ~50
        assertTrue("7-day streak should contribute, got $stat", stat >= 20)
    }

    @Test
    fun `consistency - same day dedup for streak`() {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        // Two workouts on same day + one the day before = streak of 2
        val workouts = listOf(
            buildWorkout(
                startTime = today.atStartOfDay(zone).toInstant().plusSeconds(3600),
            ),
            buildWorkout(
                startTime = today.atStartOfDay(zone).toInstant().plusSeconds(7200),
            ),
            buildWorkout(
                startTime = today.minusDays(1).atStartOfDay(zone).toInstant().plusSeconds(3600),
            ),
        )
        val stat = StatsCalculator.calculateConsistency(workouts, now)
        // 3 workouts in frequency but only 2-day streak
        assertTrue("Same-day should be deduped for streak, got $stat", stat in 1..30)
    }

    // =========================================================================
    // calculateAll integration
    // =========================================================================

    @Test
    fun `calculateAll - empty inputs return all 1s`() {
        val stats = StatsCalculator.calculateAll(emptyList(), emptyList())
        assertEquals(1, stats.speed)
        assertEquals(1, stats.endurance)
        assertEquals(1, stats.consistency)
    }

    @Test
    fun `calculateAll - with data returns non-default stats`() {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val runs = (0 until 10).map { i ->
            val date = today.minusDays(i.toLong())
            buildWorkout(
                distanceMeters = 5 * 1609.34,
                durationMillis = 35 * 60_000L, // 7 min/mi
                startTime = date.atStartOfDay(zone).toInstant().plusSeconds(3600),
            )
        }
        val stats = StatsCalculator.calculateAll(runs, runs)
        assertTrue("Speed should be > 1, got ${stats.speed}", stats.speed > 1)
        assertTrue("Endurance should be > 1, got ${stats.endurance}", stats.endurance > 1)
        assertTrue("Consistency should be > 1, got ${stats.consistency}", stats.consistency > 1)
    }

    // =========================================================================
    // calculateWalkSpeed Tests
    // =========================================================================

    @Test
    fun `walkSpeed - empty list returns 1`() {
        assertEquals(1, StatsCalculator.calculateWalkSpeed(emptyList()))
    }

    @Test
    fun `walkSpeed - fast walk at 12 min per mile yields 99`() {
        // 12:00/mi = 720s/mi → at the 99-bracket boundary
        val walk = buildWorkout(distanceMeters = 1609.34, durationMillis = 12 * 60_000L)
        assertEquals(99, StatsCalculator.calculateWalkSpeed(listOf(walk)))
    }

    @Test
    fun `walkSpeed - slow walk over 30 min per mile yields 1`() {
        // 31:00/mi = 1860s/mi → above 1800s (the 1-bracket floor)
        val walk = buildWorkout(distanceMeters = 1609.34, durationMillis = 31 * 60_000L)
        assertEquals(1, StatsCalculator.calculateWalkSpeed(listOf(walk)))
    }

    @Test
    fun `walkSpeed - moderate walk at 18 min per mile yields around 50`() {
        // 18:00/mi = 1080s/mi → at the 50-bracket boundary
        val walk = buildWorkout(distanceMeters = 1609.34, durationMillis = 18 * 60_000L)
        val stat = StatsCalculator.calculateWalkSpeed(listOf(walk))
        assertTrue("18 min/mi should yield ~50, got $stat", stat in 45..55)
    }

    // =========================================================================
    // calculateDogStats Tests
    // =========================================================================

    @Test
    fun `dogStats - empty list returns all 1s`() {
        val stats = StatsCalculator.calculateDogStats(emptyList())
        assertEquals(1, stats.speed)
        assertEquals(1, stats.endurance)
        assertEquals(1, stats.consistency)
    }

    @Test
    fun `dogStats - with data returns non-default stats`() {
        val now = java.time.Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val walks = (0 until 10).map { i ->
            val date = today.minusDays(i.toLong())
            buildWorkout(
                distanceMeters = 3 * 1609.34,
                durationMillis = 45 * 60_000L,
                activityType = ActivityType.DOG_WALK,
                startTime = date.atStartOfDay(zone).toInstant().plusSeconds(3600),
            )
        }
        val stats = StatsCalculator.calculateDogStats(walks)
        assertTrue("Dog speed should be > 1, got ${stats.speed}", stats.speed > 1)
        assertTrue("Dog endurance should be > 1, got ${stats.endurance}", stats.endurance > 1)
        assertTrue("Dog consistency should be > 1, got ${stats.consistency}", stats.consistency > 1)
    }

    // =========================================================================
    // speedBreakdown Tests
    // =========================================================================

    @Test
    fun `speedBreakdown - with run workouts includes pace detail keys`() {
        val run = buildWorkout(distanceMeters = 3 * 1609.34, durationMillis = 21 * 60_000L)
        val breakdown = StatsCalculator.speedBreakdown(listOf(run), isWalk = false)
        val keys = breakdown.details.map { it.first }
        assertTrue("Should have Best pace", keys.contains("Best pace"))
        assertTrue("Should have Median pace", keys.contains("Median pace"))
        assertTrue("Should have Effective", keys.contains("Effective"))
        assertTrue("Should have Workouts used", keys.contains("Workouts used"))
        assertTrue("Run brackets should contain 5:00", breakdown.brackets.contains("5:00"))
    }

    @Test
    fun `speedBreakdown - walk mode shows walk pace brackets`() {
        val walk = buildWorkout(distanceMeters = 1609.34, durationMillis = 15 * 60_000L)
        val breakdown = StatsCalculator.speedBreakdown(listOf(walk), isWalk = true)
        assertTrue("Walk brackets should contain 12:00", breakdown.brackets.contains("12:00"))
        assertTrue("Walk brackets should contain 30:00", breakdown.brackets.contains("30:00"))
    }

    @Test
    fun `speedBreakdown - empty list shows only workouts used as zero`() {
        val breakdown = StatsCalculator.speedBreakdown(emptyList(), isWalk = false)
        assertEquals(1, breakdown.details.size)
        assertEquals("Workouts used", breakdown.details[0].first)
        assertEquals("0", breakdown.details[0].second)
    }

    // =========================================================================
    // enduranceBreakdown Tests
    // =========================================================================

    @Test
    fun `enduranceBreakdown - with data includes distance and duration detail keys`() {
        val workout = buildWorkout(distanceMeters = 5 * 1609.34, durationMillis = 40 * 60_000L)
        val breakdown = StatsCalculator.enduranceBreakdown(listOf(workout))
        val keys = breakdown.details.map { it.first }
        assertTrue("Should have Max distance", keys.contains("Max distance"))
        assertTrue("Should have Recent avg dist", keys.contains("Recent avg dist"))
        assertTrue("Should have Max duration", keys.contains("Max duration"))
        assertTrue("Should have Recent avg dur", keys.contains("Recent avg dur"))
    }

    @Test
    fun `enduranceBreakdown - empty list shows no data message`() {
        val breakdown = StatsCalculator.enduranceBreakdown(emptyList())
        assertEquals(1, breakdown.details.size)
        assertEquals("No data", breakdown.details[0].first)
        assertEquals("Complete a workout!", breakdown.details[0].second)
    }

    // =========================================================================
    // consistencyBreakdown Tests
    // =========================================================================

    @Test
    fun `consistencyBreakdown - with recent workouts includes streak detail keys`() {
        val now = java.time.Instant.now()
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val workouts = (0 until 5).map { i ->
            val date = today.minusDays(i.toLong())
            buildWorkout(startTime = date.atStartOfDay(zone).toInstant().plusSeconds(3600))
        }
        val breakdown = StatsCalculator.consistencyBreakdown(workouts, now)
        val keys = breakdown.details.map { it.first }
        assertTrue("Should have 30-day count", keys.contains("30-day count"))
        assertTrue("Should have Current streak", keys.contains("Current streak"))
        assertTrue("Should have Longest streak", keys.contains("Longest streak"))
    }

    // =========================================================================
    // formatPace via speedBreakdown
    // =========================================================================

    @Test
    fun `speedBreakdown - formats best pace as minutes colon seconds per mile`() {
        // 7:00/mi = 420s: 3 miles in 21 min → single workout: best=median=effective=420s
        val run = buildWorkout(distanceMeters = 3 * 1609.34, durationMillis = 21 * 60_000L)
        val breakdown = StatsCalculator.speedBreakdown(listOf(run), isWalk = false)
        val bestPace = breakdown.details.find { it.first == "Best pace" }?.second
        assertNotNull("Best pace detail should exist", bestPace)
        assertEquals("7:00/mi", bestPace)
    }

    // =========================================================================
    // interpolateBrackets edge cases (via calculateSpeed)
    // =========================================================================

    @Test
    fun `speed - exact 300s boundary pace yields 99`() {
        // 5:00/mi = 300s exactly → at or below the 99-cap boundary
        val run = buildWorkout(distanceMeters = 1609.34, durationMillis = 5 * 60_000L)
        assertEquals(99, StatsCalculator.calculateSpeed(listOf(run)))
    }

    @Test
    fun `speed - midpoint 450s pace interpolates to around 69`() {
        // 7:30/mi = 450s: between 420(75) and 540(50)
        // t = (450-420)/(540-420) = 0.25, stat = 75 + 0.25*(50-75) ≈ 69
        val run = buildWorkout(distanceMeters = 2 * 1609.34, durationMillis = 15 * 60_000L)
        val stat = StatsCalculator.calculateSpeed(listOf(run))
        assertTrue("7:30/mi should interpolate to ~69, got $stat", stat in 65..73)
    }
}
