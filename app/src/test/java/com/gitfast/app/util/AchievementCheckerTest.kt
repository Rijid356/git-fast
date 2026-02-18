package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AchievementCheckerTest {

    private fun makeWorkout(
        distanceMeters: Double = 0.0,
        activityType: ActivityType = ActivityType.RUN,
        startTime: Instant = Instant.now(),
        endTime: Instant = Instant.now(),
    ): Workout = Workout(
        id = java.util.UUID.randomUUID().toString(),
        startTime = startTime,
        endTime = endTime,
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

    private fun makeSnapshot(
        workouts: List<Workout> = emptyList(),
        totalLapCount: Int = 0,
        dogWalkCount: Int = 0,
        characterLevel: Int = 1,
        unlockedIds: Set<String> = emptySet(),
    ) = AchievementSnapshot(
        allWorkouts = workouts,
        totalLapCount = totalLapCount,
        dogWalkCount = dogWalkCount,
        characterLevel = characterLevel,
        unlockedIds = unlockedIds,
    )

    // --- Zero state ---

    @Test
    fun `no workouts returns empty`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot())
        assertTrue(result.isEmpty())
    }

    // --- Distance milestones ---

    @Test
    fun `first mile unlocked at 1 mile total`() {
        val workouts = listOf(makeWorkout(distanceMeters = 1610.0)) // ~1 mile
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.FIRST_MILE })
    }

    @Test
    fun `first mile not unlocked below 1 mile`() {
        val workouts = listOf(makeWorkout(distanceMeters = 1000.0))
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertFalse(result.any { it == AchievementDef.FIRST_MILE })
    }

    @Test
    fun `marathon club unlocked at 26_2 miles cumulative`() {
        val workouts = List(27) { makeWorkout(distanceMeters = 1610.0) } // 27 miles
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.MARATHON_CLUB })
    }

    @Test
    fun `century runner not unlocked at 99 miles`() {
        val workouts = List(99) { makeWorkout(distanceMeters = 1610.0) }
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertFalse(result.any { it == AchievementDef.CENTURY_RUNNER })
    }

    @Test
    fun `century runner unlocked at 100 miles`() {
        val workouts = List(100) { makeWorkout(distanceMeters = 1610.0) }
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.CENTURY_RUNNER })
    }

    // --- Single-workout PRs ---

    @Test
    fun `5K finisher unlocked with single run over 3_1 miles`() {
        val workouts = listOf(makeWorkout(distanceMeters = 5000.0)) // ~3.1 miles
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.FIVE_K_FINISHER })
    }

    @Test
    fun `5K finisher not unlocked with dog walk over 3_1 miles`() {
        val workouts = listOf(makeWorkout(distanceMeters = 5000.0, activityType = ActivityType.DOG_WALK))
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertFalse(result.any { it == AchievementDef.FIVE_K_FINISHER })
    }

    @Test
    fun `10K finisher unlocked with single run over 6_2 miles`() {
        val workouts = listOf(makeWorkout(distanceMeters = 10000.0))
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.TEN_K_FINISHER })
    }

    // --- Workout count ---

    @Test
    fun `first steps unlocked with 1 workout`() {
        val workouts = listOf(makeWorkout())
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.FIRST_STEPS })
    }

    @Test
    fun `getting started unlocked at 5 workouts`() {
        val workouts = List(5) { makeWorkout() }
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.GETTING_STARTED })
    }

    @Test
    fun `getting started not unlocked at 4 workouts`() {
        val workouts = List(4) { makeWorkout() }
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertFalse(result.any { it == AchievementDef.GETTING_STARTED })
    }

    @Test
    fun `dedicated unlocked at 25 workouts`() {
        val workouts = List(25) { makeWorkout() }
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.DEDICATED })
    }

    // --- Streaks ---

    @Test
    fun `three-peat unlocked with 3 consecutive days`() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val workouts = (0..2).map { daysAgo ->
            makeWorkout(startTime = today.minusDays(daysAgo.toLong()).atStartOfDay(zone).toInstant())
        }
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.THREE_PEAT })
    }

    @Test
    fun `three-peat not unlocked with 2 consecutive days`() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val workouts = (0..1).map { daysAgo ->
            makeWorkout(startTime = today.minusDays(daysAgo.toLong()).atStartOfDay(zone).toInstant())
        }
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertFalse(result.any { it == AchievementDef.THREE_PEAT })
    }

    @Test
    fun `week warrior unlocked with 7 consecutive days`() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val workouts = (0..6).map { daysAgo ->
            makeWorkout(startTime = today.minusDays(daysAgo.toLong()).atStartOfDay(zone).toInstant())
        }
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.WEEK_WARRIOR })
    }

    // --- Laps ---

    @Test
    fun `lap leader unlocked at 10 total laps`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(totalLapCount = 10))
        assertTrue(result.any { it == AchievementDef.LAP_LEADER })
    }

    @Test
    fun `lap leader not unlocked at 9 laps`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(totalLapCount = 9))
        assertFalse(result.any { it == AchievementDef.LAP_LEADER })
    }

    @Test
    fun `track star unlocked at 50 total laps`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(totalLapCount = 50))
        assertTrue(result.any { it == AchievementDef.TRACK_STAR })
    }

    // --- Dog Walks ---

    @Test
    fun `good boy unlocked with 1 dog walk`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(dogWalkCount = 1))
        assertTrue(result.any { it == AchievementDef.GOOD_BOY })
    }

    @Test
    fun `good boy not unlocked with 0 dog walks`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(dogWalkCount = 0))
        assertFalse(result.any { it == AchievementDef.GOOD_BOY })
    }

    @Test
    fun `dogs best friend unlocked at 25 dog walks`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(dogWalkCount = 25))
        assertTrue(result.any { it == AchievementDef.DOGS_BEST_FRIEND })
    }

    // --- Leveling ---

    @Test
    fun `level 5 unlocked at character level 5`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(characterLevel = 5))
        assertTrue(result.any { it == AchievementDef.LEVEL_5 })
    }

    @Test
    fun `level 5 not unlocked at character level 4`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(characterLevel = 4))
        assertFalse(result.any { it == AchievementDef.LEVEL_5 })
    }

    @Test
    fun `level 10 unlocked at character level 10`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(characterLevel = 10))
        assertTrue(result.any { it == AchievementDef.LEVEL_10 })
    }

    // --- Filtering already unlocked ---

    @Test
    fun `already unlocked achievements are filtered out`() {
        val workouts = listOf(makeWorkout(distanceMeters = 1610.0))
        val result = AchievementChecker.checkNewAchievements(
            makeSnapshot(
                workouts = workouts,
                unlockedIds = setOf(AchievementDef.FIRST_MILE.id),
            )
        )
        assertFalse(result.any { it == AchievementDef.FIRST_MILE })
    }

    // --- Multiple achievements in one check ---

    @Test
    fun `multiple achievements unlocked at once`() {
        val workouts = List(5) { makeWorkout(distanceMeters = 1610.0) }
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(workouts = workouts))
        assertTrue(result.any { it == AchievementDef.FIRST_MILE })
        assertTrue(result.any { it == AchievementDef.FIRST_STEPS })
        assertTrue(result.any { it == AchievementDef.GETTING_STARTED })
    }

    // --- Streak calculation ---

    @Test
    fun `longestStreak returns correct value for non-consecutive days`() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        // Days: today, yesterday, 5 days ago, 6 days ago, 7 days ago → streak of 3 (5,6,7 ago)
        val workouts = listOf(
            makeWorkout(startTime = today.atStartOfDay(zone).toInstant()),
            makeWorkout(startTime = today.minusDays(1).atStartOfDay(zone).toInstant()),
            makeWorkout(startTime = today.minusDays(5).atStartOfDay(zone).toInstant()),
            makeWorkout(startTime = today.minusDays(6).atStartOfDay(zone).toInstant()),
            makeWorkout(startTime = today.minusDays(7).atStartOfDay(zone).toInstant()),
        )
        val snapshot = makeSnapshot(workouts = workouts)
        val streak = AchievementChecker.longestStreak(snapshot)
        assertEquals(3, streak)
    }
}
