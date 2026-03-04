package com.gitfast.app.ui.soreness

import com.gitfast.app.data.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyMapLayoutTest {

    @Test
    fun `front zones cover all 6 front muscle groups`() {
        val frontMuscles = FRONT_ZONES.map { it.muscleGroup }.toSet()
        assertEquals(
            setOf(
                MuscleGroup.CHEST,
                MuscleGroup.SHOULDERS,
                MuscleGroup.BICEPS,
                MuscleGroup.FOREARMS,
                MuscleGroup.CORE,
                MuscleGroup.QUADS,
            ),
            frontMuscles,
        )
    }

    @Test
    fun `back zones cover all 5 back muscle groups`() {
        val backMuscles = BACK_ZONES.map { it.muscleGroup }.toSet()
        assertEquals(
            setOf(
                MuscleGroup.BACK,
                MuscleGroup.TRICEPS,
                MuscleGroup.GLUTES,
                MuscleGroup.HAMSTRINGS,
                MuscleGroup.CALVES,
            ),
            backMuscles,
        )
    }

    @Test
    fun `front plus back zones cover all 11 muscle groups`() {
        val allMuscles = (FRONT_ZONES + BACK_ZONES).map { it.muscleGroup }.toSet()
        assertEquals(MuscleGroup.entries.toSet(), allMuscles)
    }

    @Test
    fun `all zones have valid normalized bounds`() {
        for (zone in FRONT_ZONES + BACK_ZONES) {
            assertTrue(
                "Zone ${zone.label} left (${zone.relativeRect.left}) should be >= 0",
                zone.relativeRect.left >= 0f,
            )
            assertTrue(
                "Zone ${zone.label} top (${zone.relativeRect.top}) should be >= 0",
                zone.relativeRect.top >= 0f,
            )
            assertTrue(
                "Zone ${zone.label} right (${zone.relativeRect.right}) should be <= 1",
                zone.relativeRect.right <= 1f,
            )
            assertTrue(
                "Zone ${zone.label} bottom (${zone.relativeRect.bottom}) should be <= 1",
                zone.relativeRect.bottom <= 1f,
            )
            assertTrue(
                "Zone ${zone.label} should have positive width",
                zone.relativeRect.width > 0,
            )
            assertTrue(
                "Zone ${zone.label} should have positive height",
                zone.relativeRect.height > 0,
            )
        }
    }

    @Test
    fun `no overlaps within same muscle group zones and other groups on front`() {
        verifyNoOverlapsBetweenDifferentGroups(FRONT_ZONES)
    }

    @Test
    fun `no overlaps within same muscle group zones and other groups on back`() {
        verifyNoOverlapsBetweenDifferentGroups(BACK_ZONES)
    }

    @Test
    fun `bilateral muscles have paired zones on front`() {
        val bicepZones = FRONT_ZONES.filter { it.muscleGroup == MuscleGroup.BICEPS }
        assertEquals("BICEPS should have 2 zones (L/R)", 2, bicepZones.size)

        val forearmZones = FRONT_ZONES.filter { it.muscleGroup == MuscleGroup.FOREARMS }
        assertEquals("FOREARMS should have 2 zones (L/R)", 2, forearmZones.size)

        val quadZones = FRONT_ZONES.filter { it.muscleGroup == MuscleGroup.QUADS }
        assertEquals("QUADS should have 2 zones (L/R)", 2, quadZones.size)
    }

    @Test
    fun `bilateral muscles have paired zones on back`() {
        val tricepZones = BACK_ZONES.filter { it.muscleGroup == MuscleGroup.TRICEPS }
        assertEquals("TRICEPS should have 2 zones (L/R)", 2, tricepZones.size)

        val hamstringZones = BACK_ZONES.filter { it.muscleGroup == MuscleGroup.HAMSTRINGS }
        assertEquals("HAMSTRINGS should have 2 zones (L/R)", 2, hamstringZones.size)

        val calfZones = BACK_ZONES.filter { it.muscleGroup == MuscleGroup.CALVES }
        assertEquals("CALVES should have 2 zones (L/R)", 2, calfZones.size)
    }

    @Test
    fun `all zones have non-empty labels`() {
        for (zone in FRONT_ZONES + BACK_ZONES) {
            assertTrue(
                "Zone for ${zone.muscleGroup} should have a non-empty label",
                zone.label.isNotBlank(),
            )
        }
    }

    private fun verifyNoOverlapsBetweenDifferentGroups(zones: List<BodyZone>) {
        for (i in zones.indices) {
            for (j in i + 1 until zones.size) {
                val a = zones[i]
                val b = zones[j]
                // Only check zones of DIFFERENT muscle groups
                if (a.muscleGroup != b.muscleGroup) {
                    val overlapX = a.relativeRect.left < b.relativeRect.right &&
                        a.relativeRect.right > b.relativeRect.left
                    val overlapY = a.relativeRect.top < b.relativeRect.bottom &&
                        a.relativeRect.bottom > b.relativeRect.top
                    assertFalse(
                        "Zone ${a.label}(${a.muscleGroup}) overlaps with ${b.label}(${b.muscleGroup})",
                        overlapX && overlapY,
                    )
                }
            }
        }
    }
}
