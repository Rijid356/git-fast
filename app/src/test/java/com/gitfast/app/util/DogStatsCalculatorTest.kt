package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DogStatsCalculatorTest {

    private fun buildDogWalk(
        distanceMeters: Double = 1609.34, // ~1 mile
        durationMillis: Long = 1_200_000L, // 20 min (~20:00/mi pace)
        startTime: Instant = Instant.now(),
    ): Workout {
        return Workout(
            id = java.util.UUID.randomUUID().toString(),
            startTime = startTime,
            endTime = startTime.plusMillis(durationMillis),
            totalSteps = 0,
            distanceMeters = distanceMeters,
            status = WorkoutStatus.COMPLETED,
            activityType = ActivityType.DOG_WALK,
            phases = emptyList(),
            gpsPoints = emptyList(),
            dogName = "Juniper",
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = null,
        )
    }

    // --- calculateDogStats returns all three stats ---

    @Test
    fun `calculateDogStats returns stats object`() {
        val walks = listOf(buildDogWalk())
        val stats = StatsCalculator.calculateDogStats(walks)
        assertTrue(stats.speed >= 1)
        assertTrue(stats.endurance >= 1)
        assertTrue(stats.consistency >= 1)
    }

    @Test
    fun `calculateDogStats with empty list returns min stats`() {
        val stats = StatsCalculator.calculateDogStats(emptyList())
        assertEquals(1, stats.speed)
        assertEquals(1, stats.endurance)
        assertEquals(1, stats.consistency)
    }

    // --- Walk speed brackets ---

    @Test
    fun `fast walk pace gives high speed stat`() {
        // 12:00/mi = 720s/mi → should be ~99
        val walk = buildDogWalk(
            distanceMeters = 1609.34, // 1 mile
            durationMillis = 720_000L, // 12 min
        )
        val speed = StatsCalculator.calculateWalkSpeed(listOf(walk))
        assertTrue("Fast walk pace should give high stat, got $speed", speed >= 90)
    }

    @Test
    fun `moderate walk pace gives medium speed stat`() {
        // 18:00/mi = 1080s/mi → should be ~50
        val walk = buildDogWalk(
            distanceMeters = 1609.34, // 1 mile
            durationMillis = 1_080_000L, // 18 min
        )
        val speed = StatsCalculator.calculateWalkSpeed(listOf(walk))
        assertTrue("Moderate walk pace should give ~50, got $speed", speed in 40..60)
    }

    @Test
    fun `slow walk pace gives low speed stat`() {
        // 30:00/mi = 1800s/mi → should be ~1
        val walk = buildDogWalk(
            distanceMeters = 1609.34, // 1 mile
            durationMillis = 1_800_000L, // 30 min
        )
        val speed = StatsCalculator.calculateWalkSpeed(listOf(walk))
        assertTrue("Slow walk pace should give low stat, got $speed", speed <= 10)
    }

    // --- Endurance reuses same brackets ---

    @Test
    fun `dog walk endurance uses distance and duration`() {
        // 3 mile walk should give ~50 endurance
        val walk = buildDogWalk(
            distanceMeters = 4828.0, // ~3 miles
            durationMillis = 2_700_000L, // 45 min
        )
        val endurance = StatsCalculator.calculateEndurance(listOf(walk))
        assertTrue("3 mile walk endurance should be ~50, got $endurance", endurance in 35..65)
    }

    // --- Consistency uses same 30-day window ---

    @Test
    fun `daily dog walks give high consistency`() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val walks = (0..27).map { daysAgo ->
            buildDogWalk(
                startTime = today.minusDays(daysAgo.toLong()).atStartOfDay(zone).toInstant()
            )
        }
        val consistency = StatsCalculator.calculateConsistency(walks)
        assertTrue("28 days of walks should give high consistency, got $consistency", consistency >= 80)
    }

    @Test
    fun `no walk speed for zero distance returns min`() {
        val walk = buildDogWalk(distanceMeters = 0.0, durationMillis = 600_000L)
        val speed = StatsCalculator.calculateWalkSpeed(listOf(walk))
        assertEquals(1, speed)
    }
}
