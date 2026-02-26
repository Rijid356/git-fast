package com.gitfast.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DogWalkEventAchievementTest {

    private fun snapshot(
        totalEventCount: Int = 0,
        eventCountByType: Map<String, Int> = emptyMap(),
    ) = AchievementSnapshot(
        allWorkouts = emptyList(),
        totalLapCount = 0,
        dogWalkCount = 0,
        characterLevel = 1,
        unlockedIds = emptySet(),
        totalDogWalkEventCount = totalEventCount,
        eventCountByType = eventCountByType,
    )

    @Test
    fun `JUNIPER_FIRST_FIND earned at 1 total event`() {
        val result = AchievementChecker.checkJuniperAchievements(snapshot(totalEventCount = 1))
        assertTrue(result.any { it == AchievementDef.JUNIPER_FIRST_FIND })
    }

    @Test
    fun `JUNIPER_FIRST_FIND not earned at 0 events`() {
        val result = AchievementChecker.checkJuniperAchievements(snapshot(totalEventCount = 0))
        assertFalse(result.any { it == AchievementDef.JUNIPER_FIRST_FIND })
    }

    @Test
    fun `JUNIPER_KEEN_NOSE earned at 10 deep sniffs`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("DEEP_SNIFF" to 10))
        )
        assertTrue(result.any { it == AchievementDef.JUNIPER_KEEN_NOSE })
    }

    @Test
    fun `JUNIPER_KEEN_NOSE not earned at 9 deep sniffs`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("DEEP_SNIFF" to 9))
        )
        assertFalse(result.any { it == AchievementDef.JUNIPER_KEEN_NOSE })
    }

    @Test
    fun `JUNIPER_SNACK_HUNTER earned at 10 snacks`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("SNACK_FOUND" to 10))
        )
        assertTrue(result.any { it == AchievementDef.JUNIPER_SNACK_HUNTER })
    }

    @Test
    fun `JUNIPER_SNACK_HUNTER not earned at 9 snacks`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("SNACK_FOUND" to 9))
        )
        assertFalse(result.any { it == AchievementDef.JUNIPER_SNACK_HUNTER })
    }

    @Test
    fun `JUNIPER_SQUIRREL_NEMESIS earned at 5 squirrel chases`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("SQUIRREL_CHASE" to 5))
        )
        assertTrue(result.any { it == AchievementDef.JUNIPER_SQUIRREL_NEMESIS })
    }

    @Test
    fun `JUNIPER_SQUIRREL_NEMESIS not earned at 4 squirrel chases`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("SQUIRREL_CHASE" to 4))
        )
        assertFalse(result.any { it == AchievementDef.JUNIPER_SQUIRREL_NEMESIS })
    }

    @Test
    fun `JUNIPER_SOCIAL_BUTTERFLY earned at 10 friendly dogs`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("FRIENDLY_DOG" to 10))
        )
        assertTrue(result.any { it == AchievementDef.JUNIPER_SOCIAL_BUTTERFLY })
    }

    @Test
    fun `JUNIPER_SOCIAL_BUTTERFLY not earned at 9 friendly dogs`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("FRIENDLY_DOG" to 9))
        )
        assertFalse(result.any { it == AchievementDef.JUNIPER_SOCIAL_BUTTERFLY })
    }

    @Test
    fun `JUNIPER_ZOOMIE_CHAMPION earned at 10 zoomies`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("ZOOMIES" to 10))
        )
        assertTrue(result.any { it == AchievementDef.JUNIPER_ZOOMIE_CHAMPION })
    }

    @Test
    fun `JUNIPER_ZOOMIE_CHAMPION not earned at 9 zoomies`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(eventCountByType = mapOf("ZOOMIES" to 9))
        )
        assertFalse(result.any { it == AchievementDef.JUNIPER_ZOOMIE_CHAMPION })
    }

    @Test
    fun `JUNIPER_ADVENTURE_LOG_50 earned at 50 total events`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(totalEventCount = 50)
        )
        assertTrue(result.any { it == AchievementDef.JUNIPER_ADVENTURE_LOG_50 })
    }

    @Test
    fun `JUNIPER_ADVENTURE_LOG_50 not earned at 49 total events`() {
        val result = AchievementChecker.checkJuniperAchievements(
            snapshot(totalEventCount = 49)
        )
        assertFalse(result.any { it == AchievementDef.JUNIPER_ADVENTURE_LOG_50 })
    }

    @Test
    fun `already unlocked achievements are not re-earned`() {
        val snap = snapshot(totalEventCount = 100).copy(
            unlockedIds = setOf(AchievementDef.JUNIPER_FIRST_FIND.id)
        )
        val result = AchievementChecker.checkJuniperAchievements(snap)
        assertFalse(result.any { it == AchievementDef.JUNIPER_FIRST_FIND })
    }

    @Test
    fun `multiple achievements can be earned at once`() {
        val snap = snapshot(
            totalEventCount = 50,
            eventCountByType = mapOf(
                "DEEP_SNIFF" to 10,
                "SNACK_FOUND" to 10,
                "SQUIRREL_CHASE" to 5,
                "FRIENDLY_DOG" to 10,
                "ZOOMIES" to 10,
            )
        )
        val result = AchievementChecker.checkJuniperAchievements(snap)
        assertTrue(result.any { it == AchievementDef.JUNIPER_FIRST_FIND })
        assertTrue(result.any { it == AchievementDef.JUNIPER_KEEN_NOSE })
        assertTrue(result.any { it == AchievementDef.JUNIPER_SNACK_HUNTER })
        assertTrue(result.any { it == AchievementDef.JUNIPER_SQUIRREL_NEMESIS })
        assertTrue(result.any { it == AchievementDef.JUNIPER_SOCIAL_BUTTERFLY })
        assertTrue(result.any { it == AchievementDef.JUNIPER_ZOOMIE_CHAMPION })
        assertTrue(result.any { it == AchievementDef.JUNIPER_ADVENTURE_LOG_50 })
    }
}
