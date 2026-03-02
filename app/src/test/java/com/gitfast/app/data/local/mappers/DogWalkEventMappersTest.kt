package com.gitfast.app.data.local.mappers

import com.gitfast.app.data.local.entity.DogWalkEventEntity
import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.DogWalkEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class DogWalkEventMappersTest {

    // =========================================================================
    // entity → domain
    // =========================================================================

    @Test
    fun `entity to domain maps all fields correctly`() {
        val entity = DogWalkEventEntity(
            id = "evt-1",
            workoutId = "w-1",
            eventType = DogWalkEventType.POOP,
            timestamp = 1_700_000_000_000L,
            latitude = 40.7128,
            longitude = -74.0060,
        )

        val domain = entity.toDomain()

        assertEquals("evt-1", domain.id)
        assertEquals("w-1", domain.workoutId)
        assertEquals(DogWalkEventType.POOP, domain.eventType)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), domain.timestamp)
        assertEquals(40.7128, domain.latitude!!, 0.0001)
        assertEquals(-74.0060, domain.longitude!!, 0.0001)
    }

    @Test
    fun `entity to domain handles null coordinates`() {
        val entity = DogWalkEventEntity(
            id = "evt-2",
            workoutId = "w-1",
            eventType = DogWalkEventType.BARK_REACT,
            timestamp = 1_700_000_000_000L,
            latitude = null,
            longitude = null,
        )

        val domain = entity.toDomain()

        assertNull(domain.latitude)
        assertNull(domain.longitude)
    }

    // =========================================================================
    // domain → entity
    // =========================================================================

    @Test
    fun `domain to entity maps all fields correctly`() {
        val ts = Instant.ofEpochMilli(1_700_000_000_000L)
        val domain = DogWalkEvent(
            id = "evt-3",
            workoutId = "w-2",
            eventType = DogWalkEventType.SQUIRREL_CHASE,
            timestamp = ts,
            latitude = 34.0522,
            longitude = -118.2437,
        )

        val entity = domain.toEntity()

        assertEquals("evt-3", entity.id)
        assertEquals("w-2", entity.workoutId)
        assertEquals(DogWalkEventType.SQUIRREL_CHASE, entity.eventType)
        assertEquals(1_700_000_000_000L, entity.timestamp)
        assertEquals(34.0522, entity.latitude!!, 0.0001)
        assertEquals(-118.2437, entity.longitude!!, 0.0001)
    }

    @Test
    fun `domain to entity handles null coordinates`() {
        val domain = DogWalkEvent(
            id = "evt-4",
            workoutId = "w-2",
            eventType = DogWalkEventType.WATER_BREAK,
            timestamp = Instant.ofEpochMilli(1_700_000_000_000L),
            latitude = null,
            longitude = null,
        )

        val entity = domain.toEntity()

        assertNull(entity.latitude)
        assertNull(entity.longitude)
    }

    // =========================================================================
    // Round-trips
    // =========================================================================

    @Test
    fun `entity to domain to entity round-trip`() {
        val original = DogWalkEventEntity(
            id = "evt-rt-1",
            workoutId = "w-rt",
            eventType = DogWalkEventType.FRIENDLY_DOG,
            timestamp = 1_700_000_000_000L,
            latitude = 51.5074,
            longitude = -0.1278,
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `domain to entity to domain round-trip`() {
        val original = DogWalkEvent(
            id = "evt-rt-2",
            workoutId = "w-rt",
            eventType = DogWalkEventType.DEEP_SNIFF,
            timestamp = Instant.ofEpochMilli(1_700_000_000_000L),
            latitude = 48.8566,
            longitude = 2.3522,
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `all DogWalkEventType values round-trip correctly`() {
        DogWalkEventType.entries.forEach { eventType ->
            val entity = DogWalkEventEntity(
                id = "evt-${eventType.name}",
                workoutId = "w-enum",
                eventType = eventType,
                timestamp = 1_700_000_000_000L,
                latitude = 0.0,
                longitude = 0.0,
            )

            val roundTripped = entity.toDomain().toEntity()

            assertEquals("Round-trip failed for $eventType", entity, roundTripped)
        }
    }

    @Test
    fun `round-trip with null coordinates preserves nulls`() {
        val original = DogWalkEventEntity(
            id = "evt-null-rt",
            workoutId = "w-null",
            eventType = DogWalkEventType.LEASH_PULL,
            timestamp = 1_700_000_000_000L,
            latitude = null,
            longitude = null,
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
        assertNull(roundTripped.latitude)
        assertNull(roundTripped.longitude)
    }
}
