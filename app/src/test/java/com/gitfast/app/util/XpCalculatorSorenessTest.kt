package com.gitfast.app.util

import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XpCalculatorSorenessTest {

    @Test
    fun `mild soreness gives base 5 XP only`() {
        val result = XpCalculator.calculateSorenessXp(
            mapOf(MuscleGroup.QUADS to SorenessIntensity.MILD),
        )
        assertEquals(5, result.totalXp)
        assertTrue(result.breakdown.any { it.contains("soreness check-in") })
    }

    @Test
    fun `moderate soreness gives base 5 plus 3 bonus`() {
        val result = XpCalculator.calculateSorenessXp(
            mapOf(MuscleGroup.QUADS to SorenessIntensity.MODERATE),
        )
        assertEquals(8, result.totalXp)
        assertTrue(result.breakdown.any { it.contains("moderate intensity bonus") })
    }

    @Test
    fun `severe soreness gives base 5 plus 5 bonus`() {
        val result = XpCalculator.calculateSorenessXp(
            mapOf(MuscleGroup.QUADS to SorenessIntensity.SEVERE),
        )
        assertEquals(10, result.totalXp)
        assertTrue(result.breakdown.any { it.contains("severe intensity bonus") })
    }

    @Test
    fun `max intensity used when multiple muscles have different intensities`() {
        val result = XpCalculator.calculateSorenessXp(
            mapOf(
                MuscleGroup.CHEST to SorenessIntensity.MILD,
                MuscleGroup.BACK to SorenessIntensity.SEVERE,
            ),
        )
        // Max is SEVERE → 5 base + 5 bonus = 10
        assertEquals(10, result.totalXp)
        assertTrue(result.breakdown.any { it.contains("severe intensity bonus") })
    }

    @Test
    fun `multiple mild muscles still gives mild XP bonus`() {
        val result = XpCalculator.calculateSorenessXp(
            mapOf(
                MuscleGroup.CHEST to SorenessIntensity.MILD,
                MuscleGroup.BACK to SorenessIntensity.MILD,
                MuscleGroup.CORE to SorenessIntensity.MILD,
            ),
        )
        // Max is MILD → 5 base + 0 bonus = 5
        assertEquals(5, result.totalXp)
    }

    @Test
    fun `streak multiplier applies to soreness XP`() {
        // Day 3 streak = 1.2x multiplier
        val result = XpCalculator.calculateSorenessXp(
            mapOf(MuscleGroup.QUADS to SorenessIntensity.MILD),
            streakDays = 3,
        )
        // 5 * 1.2 = 6
        assertEquals(6, result.totalXp)
        assertTrue(result.breakdown.any { it.contains("streak") })
    }

    @Test
    fun `no streak multiplier at day 0`() {
        val result = XpCalculator.calculateSorenessXp(
            mapOf(MuscleGroup.QUADS to SorenessIntensity.MILD),
            streakDays = 0,
        )
        assertEquals(5, result.totalXp)
        assertEquals(1.0, result.streakMultiplier, 0.001)
    }

    @Test
    fun `no streak multiplier at day 1`() {
        val result = XpCalculator.calculateSorenessXp(
            mapOf(MuscleGroup.QUADS to SorenessIntensity.MILD),
            streakDays = 1,
        )
        assertEquals(5, result.totalXp)
        assertEquals(1.0, result.streakMultiplier, 0.001)
    }

    @Test
    fun `moderate with day 5 streak`() {
        // Day 5 = 1.0 + (5-1)*0.1 = 1.4x. 8 * 1.4 = 11.2 → 11
        val result = XpCalculator.calculateSorenessXp(
            mapOf(MuscleGroup.QUADS to SorenessIntensity.MODERATE),
            streakDays = 5,
        )
        assertEquals(11, result.totalXp)
    }

    @Test
    fun `severe with max streak`() {
        // Day 6+ = 1.5x cap. 10 * 1.5 = 15
        val result = XpCalculator.calculateSorenessXp(
            mapOf(MuscleGroup.QUADS to SorenessIntensity.SEVERE),
            streakDays = 6,
        )
        assertEquals(15, result.totalXp)
    }

    @Test
    fun `minimum XP floor applies`() {
        val result = XpCalculator.calculateSorenessXp(
            mapOf(MuscleGroup.QUADS to SorenessIntensity.MILD),
            streakDays = 0,
        )
        assertTrue(result.totalXp >= 5)
    }
}
