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
    fun `entity to domain maps all fields`() {
        val today = LocalDate.now()
        val dateEpoch = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val createdAt = Instant.now().toEpochMilli()

        val entity = SorenessLogEntity(
            id = "log1",
            date = dateEpoch,
            muscleGroups = "QUADS,HAMSTRINGS,CALVES",
            intensity = "MODERATE",
            notes = "Leg day aftermath",
            xpAwarded = 8,
            createdAt = createdAt,
        )

        val domain = entity.toDomain()

        assertEquals("log1", domain.id)
        assertEquals(today, domain.date)
        assertEquals(setOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES), domain.muscleGroups)
        assertEquals(SorenessIntensity.MODERATE, domain.intensity)
        assertEquals("Leg day aftermath", domain.notes)
        assertEquals(8, domain.xpAwarded)
        assertEquals(Instant.ofEpochMilli(createdAt), domain.createdAt)
    }

    @Test
    fun `domain to entity maps all fields`() {
        val today = LocalDate.now()
        val createdAt = Instant.now()

        val domain = SorenessLog(
            id = "log2",
            date = today,
            muscleGroups = setOf(MuscleGroup.CHEST, MuscleGroup.BACK),
            intensity = SorenessIntensity.SEVERE,
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
        // Muscle groups stored as comma-separated names (order may vary in a set)
        val groups = entity.muscleGroups.split(",").toSet()
        assertEquals(setOf("CHEST", "BACK"), groups)
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
            muscleGroups = setOf(MuscleGroup.CORE, MuscleGroup.GLUTES),
            intensity = SorenessIntensity.MILD,
            notes = null,
            xpAwarded = 5,
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.date, roundTripped.date)
        assertEquals(original.muscleGroups, roundTripped.muscleGroups)
        assertEquals(original.intensity, roundTripped.intensity)
        assertNull(roundTripped.notes)
        assertEquals(original.xpAwarded, roundTripped.xpAwarded)
    }

    @Test
    fun `single muscle group round trips correctly`() {
        val log = SorenessLog(
            id = "log4",
            date = LocalDate.now(),
            muscleGroups = setOf(MuscleGroup.SHOULDERS),
            intensity = SorenessIntensity.MILD,
        )
        val roundTripped = log.toEntity().toDomain()
        assertEquals(setOf(MuscleGroup.SHOULDERS), roundTripped.muscleGroups)
    }

    @Test
    fun `all muscle groups round trip correctly`() {
        val allGroups = MuscleGroup.entries.toSet()
        val log = SorenessLog(
            id = "log5",
            date = LocalDate.now(),
            muscleGroups = allGroups,
            intensity = SorenessIntensity.MODERATE,
        )
        val roundTripped = log.toEntity().toDomain()
        assertEquals(allGroups, roundTripped.muscleGroups)
    }

    @Test
    fun `all intensity values map correctly`() {
        for (intensity in SorenessIntensity.entries) {
            val entity = SorenessLogEntity(
                id = "test-${intensity.name}",
                date = 0L,
                muscleGroups = "CORE",
                intensity = intensity.name,
            )
            assertEquals(intensity, entity.toDomain().intensity)
        }
    }
}
