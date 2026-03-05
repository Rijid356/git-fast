package com.gitfast.app.data.model

import com.gitfast.app.data.local.entity.SorenessLogEntity
import com.gitfast.app.data.local.mappers.toDomain
import com.gitfast.app.data.local.mappers.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SorenessMapperTest {

    @Test
    fun `new format entity to domain maps per-muscle intensities`() {
        val today = LocalDate.now()
        val dateEpoch = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val createdAt = Instant.now().toEpochMilli()

        val entity = SorenessLogEntity(
            id = "log1",
            date = dateEpoch,
            muscleGroups = "QUADS:MODERATE,HAMSTRINGS:SEVERE,CALVES:MILD",
            intensity = "SEVERE",
            notes = "Leg day aftermath",
            xpAwarded = 8,
            createdAt = createdAt,
        )

        val domain = entity.toDomain()

        assertEquals("log1", domain.id)
        assertEquals(today, domain.date)
        assertEquals(
            mapOf(
                MuscleGroup.QUADS to SorenessIntensity.MODERATE,
                MuscleGroup.HAMSTRINGS to SorenessIntensity.SEVERE,
                MuscleGroup.CALVES to SorenessIntensity.MILD,
            ),
            domain.muscleIntensities,
        )
        assertEquals(
            setOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
            domain.muscleGroups,
        )
        assertEquals(SorenessIntensity.SEVERE, domain.maxIntensity)
        assertEquals("Leg day aftermath", domain.notes)
        assertEquals(8, domain.xpAwarded)
        assertEquals(Instant.ofEpochMilli(createdAt), domain.createdAt)
    }

    @Test
    fun `legacy format entity to domain uses global intensity for all muscles`() {
        val today = LocalDate.now()
        val dateEpoch = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val entity = SorenessLogEntity(
            id = "legacy-1",
            date = dateEpoch,
            muscleGroups = "CHEST,BACK",
            intensity = "MODERATE",
        )

        val domain = entity.toDomain()

        assertEquals(
            mapOf(
                MuscleGroup.CHEST to SorenessIntensity.MODERATE,
                MuscleGroup.BACK to SorenessIntensity.MODERATE,
            ),
            domain.muscleIntensities,
        )
        assertEquals(SorenessIntensity.MODERATE, domain.maxIntensity)
    }

    @Test
    fun `domain to entity serializes new format`() {
        val today = LocalDate.now()
        val createdAt = Instant.now()

        val domain = SorenessLog(
            id = "log2",
            date = today,
            muscleIntensities = mapOf(
                MuscleGroup.CHEST to SorenessIntensity.SEVERE,
                MuscleGroup.BACK to SorenessIntensity.MODERATE,
            ),
            notes = "Push day",
            xpAwarded = 10,
            createdAt = createdAt,
        )

        val entity = domain.toEntity()

        assertEquals("log2", entity.id)
        assertEquals(
            today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            entity.date,
        )
        // Muscle groups stored as KEY:VALUE pairs
        val pairs = entity.muscleGroups.split(",").toSet()
        assertTrue(pairs.contains("CHEST:SEVERE"))
        assertTrue(pairs.contains("BACK:MODERATE"))
        // Intensity column holds max intensity for backward compat
        assertEquals("SEVERE", entity.intensity)
        assertEquals("Push day", entity.notes)
        assertEquals(10, entity.xpAwarded)
        assertEquals(createdAt.toEpochMilli(), entity.createdAt)
    }

    @Test
    fun `round trip preserves data`() {
        val today = LocalDate.now()
        val original = SorenessLog(
            id = "log3",
            date = today,
            muscleIntensities = mapOf(
                MuscleGroup.CORE to SorenessIntensity.MILD,
                MuscleGroup.GLUTES to SorenessIntensity.SEVERE,
            ),
            notes = null,
            xpAwarded = 5,
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.date, roundTripped.date)
        assertEquals(original.muscleIntensities, roundTripped.muscleIntensities)
        assertNull(roundTripped.notes)
        assertEquals(original.xpAwarded, roundTripped.xpAwarded)
    }

    @Test
    fun `single muscle round trips correctly`() {
        val log = SorenessLog(
            id = "log4",
            date = LocalDate.now(),
            muscleIntensities = mapOf(MuscleGroup.SHOULDERS to SorenessIntensity.MILD),
        )
        val roundTripped = log.toEntity().toDomain()
        assertEquals(
            mapOf(MuscleGroup.SHOULDERS to SorenessIntensity.MILD),
            roundTripped.muscleIntensities,
        )
    }

    @Test
    fun `all muscle groups with different intensities round trip correctly`() {
        val intensities = MuscleGroup.entries.associateWith { group ->
            SorenessIntensity.entries[group.ordinal % SorenessIntensity.entries.size]
        }
        val log = SorenessLog(
            id = "log5",
            date = LocalDate.now(),
            muscleIntensities = intensities,
        )
        val roundTripped = log.toEntity().toDomain()
        assertEquals(intensities, roundTripped.muscleIntensities)
    }

    @Test
    fun `all intensity values map correctly in new format`() {
        for (intensity in SorenessIntensity.entries) {
            val entity = SorenessLogEntity(
                id = "test-${intensity.name}",
                date = 0L,
                muscleGroups = "CORE:${intensity.name}",
                intensity = intensity.name,
            )
            val domain = entity.toDomain()
            assertEquals(
                mapOf(MuscleGroup.CORE to intensity),
                domain.muscleIntensities,
            )
        }
    }

    @Test
    fun `maxIntensity returns highest ordinal intensity`() {
        val log = SorenessLog(
            id = "max-test",
            date = LocalDate.now(),
            muscleIntensities = mapOf(
                MuscleGroup.CHEST to SorenessIntensity.MILD,
                MuscleGroup.BACK to SorenessIntensity.SEVERE,
                MuscleGroup.CORE to SorenessIntensity.MODERATE,
            ),
        )
        assertEquals(SorenessIntensity.SEVERE, log.maxIntensity)
    }

    @Test
    fun `maxIntensity returns null for empty map`() {
        val log = SorenessLog(
            id = "empty",
            date = LocalDate.now(),
            muscleIntensities = emptyMap(),
        )
        assertNull(log.maxIntensity)
    }

    @Test
    fun `empty muscle groups entity produces empty map`() {
        val entity = SorenessLogEntity(
            id = "empty-legacy",
            date = 0L,
            muscleGroups = "",
            intensity = "MILD",
        )
        val domain = entity.toDomain()
        assertTrue(domain.muscleIntensities.isEmpty())
    }

    @Test
    fun `toEntity sets intensity to MILD when map is empty`() {
        val log = SorenessLog(
            id = "empty-entity",
            date = LocalDate.now(),
            muscleIntensities = emptyMap(),
        )
        val entity = log.toEntity()
        assertEquals("MILD", entity.intensity)
        assertEquals("", entity.muscleGroups)
    }
}
