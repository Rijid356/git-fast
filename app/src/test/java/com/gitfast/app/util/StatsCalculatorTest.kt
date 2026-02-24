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

    // =========================================================================
    // Vitality (VIT) Tests
    // =========================================================================

    @Test
    fun `vitality - zero weigh-in days returns low stat`() {
        // frequency: 0 → below brackets → 1, trend: null → 50
        // blend: (1 * 0.5 + 50 * 0.5) = 25.5 → 26
        val stat = StatsCalculator.calculateVitality(weighInCount = 0, bodyFatTrendPercent = null)
        assertTrue("Zero weigh-ins + null trend should yield ~26, got $stat", stat in 20..30)
    }

    @Test
    fun `vitality - 1 weigh-in day with no trend data yields low stat`() {
        // frequency: 1 day → 1, trend: null → 50
        // blend: (1 + 50) / 2 ≈ 26
        val stat = StatsCalculator.calculateVitality(weighInCount = 1, bodyFatTrendPercent = null)
        assertTrue("1 day + null trend should yield ~26, got $stat", stat in 20..30)
    }

    @Test
    fun `vitality - 7 weigh-in days yields frequency around 25`() {
        // frequency: 7 days → 25, trend: null → 50
        // blend: (25 + 50) / 2 ≈ 38
        val stat = StatsCalculator.calculateVitality(weighInCount = 7, bodyFatTrendPercent = null)
        assertTrue("7 days + null trend should yield ~38, got $stat", stat in 30..45)
    }

    @Test
    fun `vitality - 14 weigh-in days yields frequency around 50`() {
        // frequency: 14 days → 50, trend: null → 50
        // blend: (50 + 50) / 2 = 50
        val stat = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = null)
        assertTrue("14 days + null trend should yield ~50, got $stat", stat in 45..55)
    }

    @Test
    fun `vitality - 21 weigh-in days yields frequency around 75`() {
        // frequency: 21 days → 75, trend: null → 50
        // blend: (75 + 50) / 2 ≈ 63
        val stat = StatsCalculator.calculateVitality(weighInCount = 21, bodyFatTrendPercent = null)
        assertTrue("21 days + null trend should yield ~63, got $stat", stat in 55..70)
    }

    @Test
    fun `vitality - 28 weigh-in days yields frequency around 99`() {
        // frequency: 28 days → 99, trend: null → 50
        // blend: (99 + 50) / 2 ≈ 75
        val stat = StatsCalculator.calculateVitality(weighInCount = 28, bodyFatTrendPercent = null)
        assertTrue("28 days + null trend should yield ~75, got $stat", stat in 70..80)
    }

    @Test
    fun `vitality - improving body fat trend yields high trend component`() {
        // frequency: 14 → 50, trend: -1.5% drop → 99
        // blend: (50 + 99) / 2 ≈ 75
        val stat = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = -1.5)
        assertTrue("Improving trend should yield high stat, got $stat", stat >= 65)
    }

    @Test
    fun `vitality - stable body fat yields moderate-high trend`() {
        // frequency: 14 → 50, trend: 0.0% change → 75
        // blend: (50 + 75) / 2 ≈ 63
        val stat = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = 0.0)
        assertTrue("Stable trend should yield ~63, got $stat", stat in 55..70)
    }

    @Test
    fun `vitality - slight gain in body fat yields moderate trend`() {
        // frequency: 14 → 50, trend: 1.25% gain → interpolates 0.5→75, 2.0→50, gives ~63
        // blend: (50 + 63) / 2 ≈ 57
        val stat = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = 1.25)
        assertTrue("Slight gain should yield ~57, got $stat", stat in 50..65)
    }

    @Test
    fun `vitality - moderate gain in body fat yields low trend`() {
        // frequency: 14 → 50, trend: 3.0% gain → 25
        // blend: (50 + 25) / 2 ≈ 38
        val stat = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = 3.0)
        assertTrue("Moderate gain should yield ~38, got $stat", stat in 30..45)
    }

    @Test
    fun `vitality - significant gain in body fat yields minimum trend`() {
        // frequency: 14 → 50, trend: 5.0%+ gain → 1
        // blend: (50 + 1) / 2 ≈ 26
        val stat = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = 5.0)
        assertTrue("Significant gain should yield low stat, got $stat", stat in 20..35)
    }

    @Test
    fun `vitality - null body fat trend defaults to 50 for trend component`() {
        // Verify null trend uses 50 as default
        val statWithNull = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = null)
        val statWithStable = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = 0.0)
        // null defaults to 50 (neutral), stable ≈ 75, so statWithNull < statWithStable
        assertTrue(
            "Null trend (50) should yield lower stat than stable (75), got null=$statWithNull stable=$statWithStable",
            statWithNull <= statWithStable,
        )
    }

    @Test
    fun `vitality - perfect consistency with improving trend yields near 99`() {
        // frequency: 28 → 99, trend: -2.0% drop → 99
        // blend: (99 + 99) / 2 = 99
        val stat = StatsCalculator.calculateVitality(weighInCount = 28, bodyFatTrendPercent = -2.0)
        assertTrue("Perfect consistency + improving should be near 99, got $stat", stat >= 90)
    }

    @Test
    fun `vitality - zero data returns low stat`() {
        // frequency: 0 → below brackets → 1, trend: null → 50
        // blend: (1 * 0.5 + 50 * 0.5) = 25.5 → 26
        val stat = StatsCalculator.calculateVitality(weighInCount = 0, bodyFatTrendPercent = null)
        assertTrue("Zero data should yield ~26, got $stat", stat in 20..30)
    }

    @Test
    fun `vitality - stat clamped to 1-99 range`() {
        // Test extreme values don't exceed range
        val high = StatsCalculator.calculateVitality(weighInCount = 30, bodyFatTrendPercent = -5.0)
        val low = StatsCalculator.calculateVitality(weighInCount = 0, bodyFatTrendPercent = 10.0)
        assertTrue("High VIT should be <= 99, got $high", high <= 99)
        assertTrue("Low VIT should be >= 1, got $low", low >= 1)
    }

    @Test
    fun `vitality - frequency only (no body fat data) still works`() {
        // 21 days of weigh-ins, no body fat data
        val stat = StatsCalculator.calculateVitality(weighInCount = 21, bodyFatTrendPercent = null)
        // frequency ≈ 75, trend = 50 (default), blend ≈ 63
        assertTrue("Frequency-only VIT should be moderate, got $stat", stat in 50..70)
    }

    @Test
    fun `vitality - 50-50 blend verified with known inputs`() {
        // 28 days → freq 99, stable 0.0% → trend 75
        // expected: (99 + 75) / 2 ≈ 87
        val stat = StatsCalculator.calculateVitality(weighInCount = 28, bodyFatTrendPercent = 0.0)
        assertTrue("28d + stable should blend to ~87, got $stat", stat in 80..92)
    }

    // =========================================================================
    // vitalityBreakdown Tests
    // =========================================================================

    @Test
    fun `vitalityBreakdown includes weigh-in count detail`() {
        val breakdown = StatsCalculator.vitalityBreakdown(weighInCount = 14, bodyFatTrendPercent = -0.5, effectiveScore = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = -0.5))
        val keys = breakdown.details.map { it.first }
        assertTrue("Should include weigh-in count", keys.any { it.contains("eigh") || it.contains("count") || it.contains("frequency") })
    }

    @Test
    fun `vitalityBreakdown includes trend detail`() {
        val breakdown = StatsCalculator.vitalityBreakdown(weighInCount = 14, bodyFatTrendPercent = -0.5, effectiveScore = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = -0.5))
        val keys = breakdown.details.map { it.first }
        assertTrue("Should include trend info", keys.any { it.contains("rend") || it.contains("fat") || it.contains("direction") })
    }

    @Test
    fun `vitalityBreakdown shows no trend data message when null`() {
        val breakdown = StatsCalculator.vitalityBreakdown(weighInCount = 14, bodyFatTrendPercent = null, effectiveScore = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = null))
        val values = breakdown.details.map { it.second }
        assertTrue(
            "Should indicate no trend data",
            values.any { it.contains("o data") || it.contains("neutral") || it.contains("N/A") || it.contains("50") },
        )
    }

    @Test
    fun `vitalityBreakdown brackets string is not empty`() {
        val breakdown = StatsCalculator.vitalityBreakdown(weighInCount = 14, bodyFatTrendPercent = 0.0, effectiveScore = StatsCalculator.calculateVitality(weighInCount = 14, bodyFatTrendPercent = 0.0))
        assertTrue("Brackets should not be empty", breakdown.brackets.isNotEmpty())
    }
}
