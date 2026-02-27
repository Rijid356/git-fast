package com.gitfast.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XpCalculatorSessionTest {

    // --- Base XP ---

    @Test
    fun `zero sets returns minimum XP`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 0)
        assertEquals(5, result.totalXp) // MINIMUM_XP
    }

    @Test
    fun `base XP is 3 per set`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 5)
        assertEquals(15, result.totalXp) // 5 * 3 = 15
        assertTrue(result.breakdown[0].contains("5 sets"))
    }

    @Test
    fun `single set gives 3 XP`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 1)
        assertEquals(5, result.totalXp) // 3 XP but minimum is 5
    }

    // --- Weight bonus ---

    @Test
    fun `weighted sets add 1 XP per set`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 5, weightedSets = 3)
        // 5*3 = 15 base + 3*1 = 3 weight bonus = 18
        assertEquals(18, result.totalXp)
        assertTrue(result.breakdown.any { it.contains("weighted") })
    }

    @Test
    fun `zero weighted sets gives no weight bonus`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 5, weightedSets = 0)
        assertEquals(15, result.totalXp)
        assertTrue(result.breakdown.none { it.contains("weighted") })
    }

    // --- Volume bonus ---

    @Test
    fun `10 or more sets gives volume bonus`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 10)
        // 10*3 = 30 base + 10 volume bonus = 40
        assertEquals(40, result.totalXp)
        assertTrue(result.breakdown.any { it.contains("volume bonus") })
    }

    @Test
    fun `9 sets does not get volume bonus`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 9)
        // 9*3 = 27
        assertEquals(27, result.totalXp)
        assertTrue(result.breakdown.none { it.contains("volume bonus") })
    }

    @Test
    fun `15 sets with 5 weighted gives all bonuses`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 15, weightedSets = 5)
        // 15*3 = 45 base + 5*1 = 5 weight + 10 volume = 60
        assertEquals(60, result.totalXp)
    }

    // --- Streak multiplier ---

    @Test
    fun `streak day 1 gives no bonus`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 10, streakDays = 1)
        // 30 + 10 = 40 (no multiplier)
        assertEquals(40, result.totalXp)
        assertEquals(1.0, result.streakMultiplier, 0.01)
    }

    @Test
    fun `streak day 3 gives 1_2x multiplier`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 10, streakDays = 3)
        // base: 30 + 10 volume = 40 → 40 * 1.2 = 48
        assertEquals(48, result.totalXp)
        assertEquals(1.2, result.streakMultiplier, 0.01)
    }

    @Test
    fun `streak day 6 gives capped 1_5x multiplier`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 10, streakDays = 6)
        // 40 * 1.5 = 60
        assertEquals(60, result.totalXp)
        assertEquals(1.5, result.streakMultiplier, 0.01)
    }

    // --- Breakdown ---

    @Test
    fun `breakdown includes all components`() {
        val result = XpCalculator.calculateSessionXp(totalSets = 12, weightedSets = 4, streakDays = 3)
        assertTrue(result.breakdown.any { it.contains("12 sets") })
        assertTrue(result.breakdown.any { it.contains("4 weighted") })
        assertTrue(result.breakdown.any { it.contains("volume bonus") })
        assertTrue(result.breakdown.any { it.contains("streak") })
    }
}
