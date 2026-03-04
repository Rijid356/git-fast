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
        muscleIntensities: Map<MuscleGroup, SorenessIntensity> = mapOf(
            MuscleGroup.QUADS to SorenessIntensity.MILD,
        ),
        daysAgo: Int = 0,
    ): SorenessLog = SorenessLog(
        id = java.util.UUID.randomUUID().toString(),
        date = LocalDate.now().minusDays(daysAgo.toLong()),
        muscleIntensities = muscleIntensities,
    )

    // --- Zero state ---

    @Test
    fun `empty logs returns MIN_STAT`() {
        assertEquals(1, StatsCalculator.calculateToughness(emptyList()))
    }

    // --- Per-muscle weighting ---

    @Test
    fun `single muscle mild gives low score`() {
        // 1 muscle × MILD = 1 weighted point → between 0→1 and 8→25
        val log = buildLog(mapOf(MuscleGroup.QUADS to SorenessIntensity.MILD))
        val result = StatsCalculator.calculateToughness(listOf(log))
        assertTrue("Expected score > 1 for 1 mild muscle, got $result", result > 1)
        assertTrue("Expected score < 25 for 1 mild muscle, got $result", result < 25)
    }

    @Test
    fun `multiple muscles per log accumulate points`() {
        // 3 muscles × MILD = 3 weighted points — higher than single muscle
        val singleMuscle = buildLog(mapOf(MuscleGroup.QUADS to SorenessIntensity.MILD))
        val threeMuscles = buildLog(
            mapOf(
                MuscleGroup.QUADS to SorenessIntensity.MILD,
                MuscleGroup.HAMSTRINGS to SorenessIntensity.MILD,
                MuscleGroup.CALVES to SorenessIntensity.MILD,
            ),
        )
        val single = StatsCalculator.calculateToughness(listOf(singleMuscle))
        val triple = StatsCalculator.calculateToughness(listOf(threeMuscles))
        assertTrue("3 muscles ($triple) should be > 1 muscle ($single)", triple > single)
    }

    @Test
    fun `mixed intensities across muscles calculate correctly`() {
        // 1 MILD (1) + 1 MODERATE (2) + 1 SEVERE (3) = 6 points
        val log = buildLog(
            mapOf(
                MuscleGroup.CHEST to SorenessIntensity.MILD,
                MuscleGroup.BACK to SorenessIntensity.MODERATE,
                MuscleGroup.CORE to SorenessIntensity.SEVERE,
            ),
        )
        val result = StatsCalculator.calculateToughness(listOf(log))
        // 6 points → between 0→1 and 8→25
        assertTrue("Expected score > 1 for 6 points, got $result", result > 1)
        assertTrue("Expected score < 25 for 6 points, got $result", result < 25)
    }

    // --- Bracket boundaries (rescaled: 0→1, 8→25, 20→50, 45→75, 80→99) ---

    @Test
    fun `8 weighted points hits 25 bracket`() {
        // 8 muscles × MILD = 8 points → bracket 8→25
        val log = buildLog(
            MuscleGroup.entries.take(8).associateWith { SorenessIntensity.MILD },
        )
        assertEquals(25, StatsCalculator.calculateToughness(listOf(log)))
    }

    @Test
    fun `20 weighted points hits 50 bracket`() {
        // 10 muscles × MODERATE(2) = 20 points → bracket 20→50
        val log = buildLog(
            MuscleGroup.entries.take(10).associateWith { SorenessIntensity.MODERATE },
        )
        assertEquals(50, StatsCalculator.calculateToughness(listOf(log)))
    }

    @Test
    fun `45 weighted points hits 75 bracket`() {
        // 5 logs × 3 muscles × SEVERE(3) = 45 points → bracket 45→75
        val logs = (1..5).map {
            buildLog(
                mapOf(
                    MuscleGroup.QUADS to SorenessIntensity.SEVERE,
                    MuscleGroup.HAMSTRINGS to SorenessIntensity.SEVERE,
                    MuscleGroup.CALVES to SorenessIntensity.SEVERE,
                ),
            )
        }
        assertEquals(75, StatsCalculator.calculateToughness(logs))
    }

    @Test
    fun `80 weighted points hits 99 bracket`() {
        // 10 logs × 8 muscles × MILD(1) = 80 points → bracket 80→99
        val logs = (1..10).map {
            buildLog(
                MuscleGroup.entries.take(8).associateWith { SorenessIntensity.MILD },
            )
        }
        assertEquals(99, StatsCalculator.calculateToughness(logs))
    }

    @Test
    fun `above max bracket clamps to 99`() {
        // Well above 80 points
        val logs = (1..20).map {
            buildLog(
                MuscleGroup.entries.associateWith { SorenessIntensity.SEVERE },
            )
        }
        assertEquals(99, StatsCalculator.calculateToughness(logs))
    }

    // --- Breakdown ---

    @Test
    fun `toughnessBreakdown counts individual muscle entries`() {
        val logs = listOf(
            buildLog(
                mapOf(
                    MuscleGroup.CHEST to SorenessIntensity.MILD,
                    MuscleGroup.BACK to SorenessIntensity.MODERATE,
                ),
            ),
            buildLog(
                mapOf(
                    MuscleGroup.CORE to SorenessIntensity.SEVERE,
                ),
            ),
        )
        // 1 MILD + 1 MODERATE + 1 SEVERE = 3 entries, 6 weighted points
        val breakdown = StatsCalculator.toughnessBreakdown(logs, effectiveScore = 10)

        assertTrue(breakdown.details.any { it.first == "Logs (30d)" && it.second == "2" })
        assertTrue(breakdown.details.any { it.first == "Muscle entries" && it.second == "3" })
        assertTrue(breakdown.details.any { it.first == "Mild / Mod / Sev" && it.second == "1 / 1 / 1" })
        assertTrue(breakdown.details.any { it.first == "Weighted points" && it.second == "6" })
        assertTrue(breakdown.details.any { it.first == "Effective score" && it.second == "10" })
    }

    @Test
    fun `toughnessBreakdown has expected description and decay note`() {
        val breakdown = StatsCalculator.toughnessBreakdown(emptyList(), effectiveScore = 1)
        assertTrue(breakdown.description.contains("30-day"))
        assertTrue(breakdown.decayNote.contains("30-day"))
    }

    @Test
    fun `toughnessBreakdown brackets reflect rescaled values`() {
        val breakdown = StatsCalculator.toughnessBreakdown(emptyList(), effectiveScore = 1)
        assertTrue(breakdown.brackets.contains("8"))
        assertTrue(breakdown.brackets.contains("20"))
        assertTrue(breakdown.brackets.contains("45"))
        assertTrue(breakdown.brackets.contains("80"))
    }
}
