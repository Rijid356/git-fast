package com.gitfast.app.data.model

import com.gitfast.app.data.local.mappers.toDomain
import com.gitfast.app.data.local.mappers.toEntity
import com.gitfast.app.data.local.entity.ExerciseSessionEntity
import com.gitfast.app.data.local.entity.ExerciseSetEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ExerciseMapperTest {

    private val now = Instant.parse("2026-03-06T12:00:00Z")
    private val later = Instant.parse("2026-03-06T13:00:00Z")

    // --- Session mapping ---

    @Test
    fun `session entity to domain round-trip preserves all fields`() {
        val entity = ExerciseSessionEntity(
            id = "session-1",
            startTime = now.toEpochMilli(),
            endTime = later.toEpochMilli(),
            notes = "Great workout",
            xpAwarded = 42,
        )
        val domain = entity.toDomain()
        assertEquals("session-1", domain.id)
        assertEquals(now, domain.startTime)
        assertEquals(later, domain.endTime)
        assertEquals("Great workout", domain.notes)
        assertEquals(42, domain.xpAwarded)

        val backToEntity = domain.toEntity()
        assertEquals(entity, backToEntity)
    }

    @Test
    fun `session with null endTime and notes round-trips`() {
        val entity = ExerciseSessionEntity(
            id = "session-2",
            startTime = now.toEpochMilli(),
            endTime = null,
            notes = null,
            xpAwarded = 0,
        )
        val domain = entity.toDomain()
        assertNull(domain.endTime)
        assertNull(domain.notes)
        assertEquals(0, domain.xpAwarded)

        val backToEntity = domain.toEntity()
        assertEquals(entity, backToEntity)
    }

    @Test
    fun `session toDomain includes provided sets`() {
        val entity = ExerciseSessionEntity(
            id = "session-3",
            startTime = now.toEpochMilli(),
        )
        val sets = listOf(
            ExerciseSet(
                id = "set-1", sessionId = "session-3", exerciseId = "bw_push_up",
                setNumber = 1, reps = 10, completedAt = now,
            ),
        )
        val domain = entity.toDomain(sets)
        assertEquals(1, domain.sets.size)
        assertEquals("set-1", domain.sets[0].id)
    }

    // --- Set mapping ---

    @Test
    fun `set entity to domain round-trip preserves all fields`() {
        val entity = ExerciseSetEntity(
            id = "set-1",
            sessionId = "session-1",
            exerciseId = "db_bench_press",
            setNumber = 3,
            reps = 12,
            weightLbs = 45.0,
            durationSeconds = 30,
            isWarmup = true,
            completedAt = now.toEpochMilli(),
        )
        val domain = entity.toDomain()
        assertEquals("set-1", domain.id)
        assertEquals("session-1", domain.sessionId)
        assertEquals("db_bench_press", domain.exerciseId)
        assertEquals(3, domain.setNumber)
        assertEquals(12, domain.reps)
        assertEquals(45.0, domain.weightLbs!!, 0.01)
        assertEquals(30, domain.durationSeconds)
        assertEquals(true, domain.isWarmup)
        assertEquals(now, domain.completedAt)

        val backToEntity = domain.toEntity()
        assertEquals(entity, backToEntity)
    }

    @Test
    fun `set with null weightLbs and durationSeconds round-trips`() {
        val entity = ExerciseSetEntity(
            id = "set-2",
            sessionId = "session-1",
            exerciseId = "bw_push_up",
            setNumber = 1,
            reps = 20,
            weightLbs = null,
            durationSeconds = null,
            isWarmup = false,
            completedAt = now.toEpochMilli(),
        )
        val domain = entity.toDomain()
        assertNull(domain.weightLbs)
        assertNull(domain.durationSeconds)
        assertEquals(false, domain.isWarmup)

        val backToEntity = domain.toEntity()
        assertEquals(entity, backToEntity)
    }
}
