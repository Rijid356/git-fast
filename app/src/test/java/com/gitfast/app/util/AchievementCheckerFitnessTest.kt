package com.gitfast.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCheckerFitnessTest {

    private fun snapshot(
        sessionCount: Int = 0,
        setCount: Int = 0,
        repCount: Int = 0,
        strengthStat: Int = 1,
        unlockedIds: Set<String> = emptySet(),
    ) = AchievementSnapshot(
        allWorkouts = emptyList(),
        totalLapCount = 0,
        dogWalkCount = 0,
        characterLevel = 1,
        unlockedIds = unlockedIds,
        totalExerciseSessionCount = sessionCount,
        totalExerciseSetCount = setCount,
        totalExerciseReps = repCount,
        strengthStat = strengthStat,
    )

    // --- FIRST_REP ---

    @Test
    fun `FIRST_REP earned after 1 session`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(sessionCount = 1))
        assertTrue(earned.any { it == AchievementDef.FIRST_REP })
    }

    @Test
    fun `FIRST_REP not earned with 0 sessions`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(sessionCount = 0))
        assertTrue(earned.none { it == AchievementDef.FIRST_REP })
    }

    @Test
    fun `FIRST_REP not re-earned when already unlocked`() {
        val earned = AchievementChecker.checkNewAchievements(
            snapshot(sessionCount = 1, unlockedIds = setOf("fitness_first_rep"))
        )
        assertTrue(earned.none { it == AchievementDef.FIRST_REP })
    }

    // --- GYM_RAT ---

    @Test
    fun `GYM_RAT earned after 10 sessions`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(sessionCount = 10))
        assertTrue(earned.any { it == AchievementDef.GYM_RAT })
    }

    @Test
    fun `GYM_RAT not earned with 9 sessions`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(sessionCount = 9))
        assertTrue(earned.none { it == AchievementDef.GYM_RAT })
    }

    // --- IRON_ADDICT ---

    @Test
    fun `IRON_ADDICT earned after 50 sessions`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(sessionCount = 50))
        assertTrue(earned.any { it == AchievementDef.IRON_ADDICT })
    }

    // --- CENTURY_SETS ---

    @Test
    fun `CENTURY_SETS earned after 100 sets`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(setCount = 100))
        assertTrue(earned.any { it == AchievementDef.CENTURY_SETS })
    }

    @Test
    fun `CENTURY_SETS not earned with 99 sets`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(setCount = 99))
        assertTrue(earned.none { it == AchievementDef.CENTURY_SETS })
    }

    // --- THOUSAND_REPS ---

    @Test
    fun `THOUSAND_REPS earned after 1000 reps`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(repCount = 1000))
        assertTrue(earned.any { it == AchievementDef.THOUSAND_REPS })
    }

    @Test
    fun `THOUSAND_REPS not earned with 999 reps`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(repCount = 999))
        assertTrue(earned.none { it == AchievementDef.THOUSAND_REPS })
    }

    // --- STRENGTH_TITAN ---

    @Test
    fun `STRENGTH_TITAN earned when STR is 50 or higher`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(strengthStat = 50))
        assertTrue(earned.any { it == AchievementDef.STRENGTH_TITAN })
    }

    @Test
    fun `STRENGTH_TITAN not earned when STR is 49`() {
        val earned = AchievementChecker.checkNewAchievements(snapshot(strengthStat = 49))
        assertTrue(earned.none { it == AchievementDef.STRENGTH_TITAN })
    }

    // --- All 6 FITNESS achievements are profileId=1 ---

    @Test
    fun `all FITNESS achievements have profileId 1`() {
        val fitnessAchievements = AchievementDef.entries.filter {
            it.category == AchievementCategory.FITNESS
        }
        assertEquals(6, fitnessAchievements.size)
        assertTrue(fitnessAchievements.all { it.profileId == 1 })
    }
}
