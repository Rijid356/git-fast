package com.gitfast.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsCalculatorStrengthTest {

    // --- Zero state ---

    @Test
    fun `empty sets returns MIN_STAT`() {
        assertEquals(1, StatsCalculator.calculateStrength(emptyList()))
    }

    // --- Bracket interpolation ---

    @Test
    fun `volume 0 returns 1`() {
        // Single set with 0 reps (if that were possible) but let's use empty
        assertEquals(1, StatsCalculator.calculateStrength(emptyList()))
    }

    @Test
    fun `low volume maps to low stat`() {
        // 10 bodyweight reps = volume 10 → between 1 and 25
        val sets = listOf(10 to false)
        val stat = StatsCalculator.calculateStrength(sets)
        assertTrue("Expected stat between 1 and 25, got $stat", stat in 1..25)
    }

    @Test
    fun `50 volume maps to 25`() {
        // 50 bodyweight reps = volume 50 → should be exactly 25
        val sets = listOf(50 to false)
        assertEquals(25, StatsCalculator.calculateStrength(sets))
    }

    @Test
    fun `150 volume maps to 50`() {
        // 150 bodyweight reps = volume 150 → should be exactly 50
        val sets = (1..15).map { 10 to false }
        assertEquals(50, StatsCalculator.calculateStrength(sets))
    }

    @Test
    fun `300 volume maps to 75`() {
        // 30 sets of 10 bodyweight reps = volume 300 → 75
        val sets = (1..30).map { 10 to false }
        assertEquals(75, StatsCalculator.calculateStrength(sets))
    }

    @Test
    fun `500 or more volume maps to 99`() {
        // 50 sets of 10 bodyweight reps = volume 500 → 99
        val sets = (1..50).map { 10 to false }
        assertEquals(99, StatsCalculator.calculateStrength(sets))
    }

    @Test
    fun `above 500 volume caps at 99`() {
        val sets = (1..100).map { 10 to false }
        assertEquals(99, StatsCalculator.calculateStrength(sets))
    }

    // --- Weight factor ---

    @Test
    fun `weighted sets have 1_5x factor`() {
        // 10 weighted reps = 10 * 1.5 = 15 volume (truncated to int = 15)
        // 10 bodyweight reps = 10 * 1.0 = 10 volume
        val weightedStat = StatsCalculator.calculateStrength(listOf(10 to true))
        val bodyweightStat = StatsCalculator.calculateStrength(listOf(10 to false))
        assertTrue("Weighted ($weightedStat) should be >= bodyweight ($bodyweightStat)",
            weightedStat >= bodyweightStat)
    }

    @Test
    fun `mix of weighted and bodyweight sets`() {
        // 5 weighted reps (5*1.5=7) + 5 bodyweight reps (5*1.0=5) = volume 12
        val sets = listOf(5 to true, 5 to false)
        val stat = StatsCalculator.calculateStrength(sets)
        assertTrue("Expected stat > 1, got $stat", stat > 1)
    }

    // --- Breakdown ---

    @Test
    fun `strengthBreakdown returns correct fields`() {
        val sets = listOf(10 to true, 15 to false, 8 to true)
        val stat = StatsCalculator.calculateStrength(sets)
        val breakdown = StatsCalculator.strengthBreakdown(sets, stat)

        assertEquals(5, breakdown.details.size)
        assertEquals("3", breakdown.details[0].second) // Sets (30d)
        assertEquals("33", breakdown.details[1].second) // Total reps
        assertEquals("2", breakdown.details[2].second) // Weighted sets
        assertTrue(breakdown.description.contains("30-day"))
        assertTrue(breakdown.brackets.contains("50"))
    }
}
