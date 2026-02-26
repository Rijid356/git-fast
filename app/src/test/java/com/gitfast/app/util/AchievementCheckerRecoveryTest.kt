package com.gitfast.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCheckerRecoveryTest {

    private fun makeSnapshot(
        totalSorenessLogCount: Int = 0,
        toughnessStat: Int = 1,
        unlockedIds: Set<String> = emptySet(),
    ) = AchievementSnapshot(
        allWorkouts = emptyList(),
        totalLapCount = 0,
        dogWalkCount = 0,
        characterLevel = 1,
        unlockedIds = unlockedIds,
        totalSorenessLogCount = totalSorenessLogCount,
        toughnessStat = toughnessStat,
    )

    // --- FIRST_ACHE (1 soreness log) ---

    @Test
    fun `FIRST_ACHE not earned with 0 logs`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(totalSorenessLogCount = 0))
        assertFalse(result.any { it == AchievementDef.FIRST_ACHE })
    }

    @Test
    fun `FIRST_ACHE earned with 1 log`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(totalSorenessLogCount = 1))
        assertTrue(result.any { it == AchievementDef.FIRST_ACHE })
    }

    @Test
    fun `FIRST_ACHE not re-earned if already unlocked`() {
        val result = AchievementChecker.checkNewAchievements(
            makeSnapshot(totalSorenessLogCount = 1, unlockedIds = setOf("recovery_first_ache")),
        )
        assertFalse(result.any { it == AchievementDef.FIRST_ACHE })
    }

    // --- IRON_BODY (7 soreness logs) ---

    @Test
    fun `IRON_BODY not earned with 6 logs`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(totalSorenessLogCount = 6))
        assertFalse(result.any { it == AchievementDef.IRON_BODY })
    }

    @Test
    fun `IRON_BODY earned with 7 logs`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(totalSorenessLogCount = 7))
        assertTrue(result.any { it == AchievementDef.IRON_BODY })
    }

    // --- RECOVERY_WARRIOR (30 soreness logs) ---

    @Test
    fun `RECOVERY_WARRIOR not earned with 29 logs`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(totalSorenessLogCount = 29))
        assertFalse(result.any { it == AchievementDef.RECOVERY_WARRIOR })
    }

    @Test
    fun `RECOVERY_WARRIOR earned with 30 logs`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(totalSorenessLogCount = 30))
        assertTrue(result.any { it == AchievementDef.RECOVERY_WARRIOR })
    }

    // --- BUILT_DIFFERENT (TGH stat >= 50) ---

    @Test
    fun `BUILT_DIFFERENT not earned with TGH 49`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(toughnessStat = 49))
        assertFalse(result.any { it == AchievementDef.BUILT_DIFFERENT })
    }

    @Test
    fun `BUILT_DIFFERENT earned with TGH 50`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(toughnessStat = 50))
        assertTrue(result.any { it == AchievementDef.BUILT_DIFFERENT })
    }

    @Test
    fun `BUILT_DIFFERENT earned with TGH 99`() {
        val result = AchievementChecker.checkNewAchievements(makeSnapshot(toughnessStat = 99))
        assertTrue(result.any { it == AchievementDef.BUILT_DIFFERENT })
    }

    // --- All 4 recovery achievements are user-profile (profileId=1) ---

    @Test
    fun `recovery achievements are not checked by juniperAchievements`() {
        val snapshot = makeSnapshot(totalSorenessLogCount = 100, toughnessStat = 99)
        val juniperResult = AchievementChecker.checkJuniperAchievements(snapshot)
        assertFalse(juniperResult.any { it == AchievementDef.FIRST_ACHE })
        assertFalse(juniperResult.any { it == AchievementDef.IRON_BODY })
        assertFalse(juniperResult.any { it == AchievementDef.RECOVERY_WARRIOR })
        assertFalse(juniperResult.any { it == AchievementDef.BUILT_DIFFERENT })
    }
}
