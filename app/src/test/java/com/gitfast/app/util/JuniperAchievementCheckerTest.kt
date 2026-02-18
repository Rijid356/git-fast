package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class JuniperAchievementCheckerTest {

    private fun makeSnapshot(
        dogWalkCount: Int = 0,
        totalDogWalkDistanceMiles: Double = 0.0,
        characterLevel: Int = 1,
        unlockedIds: Set<String> = emptySet(),
    ) = AchievementSnapshot(
        allWorkouts = emptyList(),
        totalLapCount = 0,
        dogWalkCount = dogWalkCount,
        characterLevel = characterLevel,
        unlockedIds = unlockedIds,
        totalDogWalkDistanceMiles = totalDogWalkDistanceMiles,
    )

    // --- First Sniff ---

    @Test
    fun `first sniff unlocked with 1 dog walk`() {
        val result = AchievementChecker.checkJuniperAchievements(makeSnapshot(dogWalkCount = 1))
        assertTrue(result.any { it == AchievementDef.JUNIPER_FIRST_SNIFF })
    }

    @Test
    fun `first sniff not unlocked with 0 dog walks`() {
        val result = AchievementChecker.checkJuniperAchievements(makeSnapshot(dogWalkCount = 0))
        assertFalse(result.any { it == AchievementDef.JUNIPER_FIRST_SNIFF })
    }

    // --- Trail Sniffer (10 miles) ---

    @Test
    fun `trail sniffer unlocked at 10 miles`() {
        val result = AchievementChecker.checkJuniperAchievements(
            makeSnapshot(totalDogWalkDistanceMiles = 10.0)
        )
        assertTrue(result.any { it == AchievementDef.JUNIPER_TRAIL_SNIFFER })
    }

    @Test
    fun `trail sniffer not unlocked at 9 miles`() {
        val result = AchievementChecker.checkJuniperAchievements(
            makeSnapshot(totalDogWalkDistanceMiles = 9.9)
        )
        assertFalse(result.any { it == AchievementDef.JUNIPER_TRAIL_SNIFFER })
    }

    // --- Adventure Pup (25 miles) ---

    @Test
    fun `adventure pup unlocked at 25 miles`() {
        val result = AchievementChecker.checkJuniperAchievements(
            makeSnapshot(totalDogWalkDistanceMiles = 25.0)
        )
        assertTrue(result.any { it == AchievementDef.JUNIPER_ADVENTURE_PUP })
    }

    @Test
    fun `adventure pup not unlocked at 24 miles`() {
        val result = AchievementChecker.checkJuniperAchievements(
            makeSnapshot(totalDogWalkDistanceMiles = 24.9)
        )
        assertFalse(result.any { it == AchievementDef.JUNIPER_ADVENTURE_PUP })
    }

    // --- Pack Leader (10 walks) ---

    @Test
    fun `pack leader unlocked at 10 walks`() {
        val result = AchievementChecker.checkJuniperAchievements(makeSnapshot(dogWalkCount = 10))
        assertTrue(result.any { it == AchievementDef.JUNIPER_PACK_LEADER })
    }

    @Test
    fun `pack leader not unlocked at 9 walks`() {
        val result = AchievementChecker.checkJuniperAchievements(makeSnapshot(dogWalkCount = 9))
        assertFalse(result.any { it == AchievementDef.JUNIPER_PACK_LEADER })
    }

    // --- Trail Master (50 walks) ---

    @Test
    fun `trail master unlocked at 50 walks`() {
        val result = AchievementChecker.checkJuniperAchievements(makeSnapshot(dogWalkCount = 50))
        assertTrue(result.any { it == AchievementDef.JUNIPER_TRAIL_MASTER })
    }

    @Test
    fun `trail master not unlocked at 49 walks`() {
        val result = AchievementChecker.checkJuniperAchievements(makeSnapshot(dogWalkCount = 49))
        assertFalse(result.any { it == AchievementDef.JUNIPER_TRAIL_MASTER })
    }

    // --- Good Girl (level 5) ---

    @Test
    fun `good girl unlocked at Juniper level 5`() {
        val result = AchievementChecker.checkJuniperAchievements(makeSnapshot(characterLevel = 5))
        assertTrue(result.any { it == AchievementDef.JUNIPER_GOOD_GIRL })
    }

    @Test
    fun `good girl not unlocked at level 4`() {
        val result = AchievementChecker.checkJuniperAchievements(makeSnapshot(characterLevel = 4))
        assertFalse(result.any { it == AchievementDef.JUNIPER_GOOD_GIRL })
    }

    // --- Profile isolation ---

    @Test
    fun `juniper achievements do not appear in user check`() {
        val snapshot = makeSnapshot(dogWalkCount = 100, totalDogWalkDistanceMiles = 100.0, characterLevel = 10)
        val result = AchievementChecker.checkNewAchievements(snapshot)
        val juniperIds = AchievementDef.entries.filter { it.profileId == 2 }.map { it.id }.toSet()
        assertTrue("User check should not return Juniper achievements",
            result.none { it.id in juniperIds })
    }

    @Test
    fun `user achievements do not appear in juniper check`() {
        val snapshot = makeSnapshot(dogWalkCount = 100, totalDogWalkDistanceMiles = 100.0, characterLevel = 10)
        val result = AchievementChecker.checkJuniperAchievements(snapshot)
        val userIds = AchievementDef.entries.filter { it.profileId == 1 }.map { it.id }.toSet()
        assertTrue("Juniper check should not return user achievements",
            result.none { it.id in userIds })
    }

    // --- Filtering already unlocked ---

    @Test
    fun `already unlocked juniper achievements are filtered out`() {
        val result = AchievementChecker.checkJuniperAchievements(
            makeSnapshot(
                dogWalkCount = 1,
                unlockedIds = setOf(AchievementDef.JUNIPER_FIRST_SNIFF.id),
            )
        )
        assertFalse(result.any { it == AchievementDef.JUNIPER_FIRST_SNIFF })
    }

    // --- Multiple achievements at once ---

    @Test
    fun `multiple juniper achievements unlocked at once`() {
        val result = AchievementChecker.checkJuniperAchievements(
            makeSnapshot(
                dogWalkCount = 10,
                totalDogWalkDistanceMiles = 10.0,
            )
        )
        assertTrue(result.any { it == AchievementDef.JUNIPER_FIRST_SNIFF })
        assertTrue(result.any { it == AchievementDef.JUNIPER_TRAIL_SNIFFER })
        assertTrue(result.any { it == AchievementDef.JUNIPER_PACK_LEADER })
    }
}
