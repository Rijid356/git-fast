package com.gitfast.app.util

import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.data.model.SorenessLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StatsCalculatorToughnessTest {

    private fun buildLog(
        intensity: SorenessIntensity = SorenessIntensity.MILD,
        daysAgo: Int = 0,
    ): SorenessLog = SorenessLog(
        id = java.util.UUID.randomUUID().toString(),
        date = LocalDate.now().minusDays(daysAgo.toLong()),
        muscleGroups = setOf(MuscleGroup.QUADS),
        intensity = intensity,
    )

    // --- Zero state ---

    @Test
    fun `empty logs returns MIN_STAT`() {
        assertEquals(1, StatsCalculator.calculateToughness(emptyList()))
    }

    // --- Single log intensity weights ---

    @Test
    fun `single mild log gives low score`() {
        // 1 MILD = 1 weighted point → between bracket 0→1 and 3→25
        val result = StatsCalculator.calculateToughness(listOf(buildLog(SorenessIntensity.MILD)))
        assertTrue("Expected score > 1 for 1 mild log, got $result", result > 1)
        assertTrue("Expected score < 25 for 1 mild log, got $result", result < 25)
    }

    @Test
    fun `single moderate log gives higher score than mild`() {
        // 1 MODERATE = 2 weighted points
        val mild = StatsCalculator.calculateToughness(listOf(buildLog(SorenessIntensity.MILD)))
        val moderate = StatsCalculator.calculateToughness(listOf(buildLog(SorenessIntensity.MODERATE)))
        assertTrue("Moderate ($moderate) should be > mild ($mild)", moderate > mild)
    }

    @Test
    fun `single severe log gives highest single-log score`() {
        // 1 SEVERE = 3 weighted points → exactly at bracket 3→25
        val result = StatsCalculator.calculateToughness(listOf(buildLog(SorenessIntensity.SEVERE)))
        assertEquals(25, result)
    }

    // --- Bracket boundaries ---

    @Test
    fun `3 mild logs hits 25 bracket exactly`() {
        // 3 MILD = 3 weighted points → bracket 3→25
        val logs = (1..3).map { buildLog(SorenessIntensity.MILD) }
        assertEquals(25, StatsCalculator.calculateToughness(logs))
    }

    @Test
    fun `7 mild logs hits 50 bracket exactly`() {
        // 7 MILD = 7 weighted points → bracket 7→50
        val logs = (1..7).map { buildLog(SorenessIntensity.MILD) }
        assertEquals(50, StatsCalculator.calculateToughness(logs))
    }

    @Test
    fun `14 mild logs hits 75 bracket exactly`() {
        // 14 MILD = 14 weighted points → bracket 14→75
        val logs = (1..14).map { buildLog(SorenessIntensity.MILD) }
        assertEquals(75, StatsCalculator.calculateToughness(logs))
    }

    @Test
    fun `25 mild logs hits 99 bracket`() {
        // 25 MILD = 25 weighted points → bracket 25→99
        val logs = (1..25).map { buildLog(SorenessIntensity.MILD) }
        assertEquals(99, StatsCalculator.calculateToughness(logs))
    }

    @Test
    fun `above max bracket clamps to 99`() {
        // 50 MILD = 50 weighted points → above max bracket, should clamp
        val logs = (1..50).map { buildLog(SorenessIntensity.MILD) }
        assertEquals(99, StatsCalculator.calculateToughness(logs))
    }

    // --- Mixed intensity ---

    @Test
    fun `mixed intensities calculate weighted total correctly`() {
        // 2 MILD (2) + 1 MODERATE (2) + 1 SEVERE (3) = 7 → bracket 7→50
        val logs = listOf(
            buildLog(SorenessIntensity.MILD),
            buildLog(SorenessIntensity.MILD),
            buildLog(SorenessIntensity.MODERATE),
            buildLog(SorenessIntensity.SEVERE),
        )
        assertEquals(50, StatsCalculator.calculateToughness(logs))
    }

    // --- Breakdown ---

    @Test
    fun `toughnessBreakdown counts intensity categories correctly`() {
        val logs = listOf(
            buildLog(SorenessIntensity.MILD),
            buildLog(SorenessIntensity.MILD),
            buildLog(SorenessIntensity.MODERATE),
            buildLog(SorenessIntensity.SEVERE),
        )
        val breakdown = StatsCalculator.toughnessBreakdown(logs, effectiveScore = 50)

        assertTrue(breakdown.details.any { it.first == "Logs (30d)" && it.second == "4" })
        assertTrue(breakdown.details.any { it.first == "Mild / Mod / Sev" && it.second == "2 / 1 / 1" })
        assertTrue(breakdown.details.any { it.first == "Weighted points" && it.second == "7" })
        assertTrue(breakdown.details.any { it.first == "Effective score" && it.second == "50" })
    }

    @Test
    fun `toughnessBreakdown has expected description and decay note`() {
        val breakdown = StatsCalculator.toughnessBreakdown(emptyList(), effectiveScore = 1)
        assertTrue(breakdown.description.contains("30-day"))
        assertTrue(breakdown.decayNote.contains("30-day"))
    }
}
