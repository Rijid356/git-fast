package com.gitfast.app.util

import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.DogWalkEventType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DogWalkNarrativeGeneratorTest {

    private fun event(
        type: DogWalkEventType,
        offsetMs: Long = 0,
    ) = DogWalkEvent(
        id = "e-$offsetMs",
        workoutId = "w1",
        eventType = type,
        timestamp = Instant.ofEpochMilli(1000000 + offsetMs),
        latitude = 40.0,
        longitude = -74.0,
    )

    @Test
    fun `zero events returns a narrative mentioning duration`() {
        val narrative = DogWalkNarrativeGenerator.generateNarrative(emptyList(), 30)
        assertTrue("Should mention 30 minutes, got: $narrative", narrative.contains("30"))
    }

    @Test
    fun `zero events returns non-blank narrative`() {
        val narrative = DogWalkNarrativeGenerator.generateNarrative(emptyList(), 15)
        assertTrue(narrative.isNotBlank())
    }

    @Test
    fun `single SNACK_FOUND event generates foraging narrative`() {
        val events = listOf(event(DogWalkEventType.SNACK_FOUND))
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 20)
        assertTrue("Should mention snack, got: $narrative", narrative.contains("snack"))
        assertTrue("Should mention 20 minutes, got: $narrative", narrative.contains("20"))
    }

    @Test
    fun `single DEEP_SNIFF type generates sniff narrative`() {
        val events = listOf(event(DogWalkEventType.DEEP_SNIFF))
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 25)
        assertTrue("Should mention sniff, got: $narrative", narrative.contains("sniff"))
    }

    @Test
    fun `single SQUIRREL_CHASE generates squirrel narrative`() {
        val events = listOf(event(DogWalkEventType.SQUIRREL_CHASE))
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 10)
        assertTrue(
            "Should mention squirrel, got: $narrative",
            narrative.lowercase().contains("squirrel")
        )
    }

    @Test
    fun `single ZOOMIES generates energy narrative`() {
        val events = listOf(event(DogWalkEventType.ZOOMIES))
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 15)
        assertTrue(
            "Should mention zoomie, got: $narrative",
            narrative.lowercase().contains("zoomie")
        )
    }

    @Test
    fun `single FRIENDLY_DOG generates social narrative`() {
        val events = listOf(event(DogWalkEventType.FRIENDLY_DOG))
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 30)
        assertTrue(
            "Should mention friend or dog, got: $narrative",
            narrative.lowercase().contains("friend") || narrative.lowercase().contains("dog")
        )
    }

    @Test
    fun `single POOP generates bathroom narrative`() {
        val events = listOf(event(DogWalkEventType.POOP))
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 20)
        assertTrue(
            "Should mention pit stop, got: $narrative",
            narrative.lowercase().contains("pit stop")
        )
    }

    @Test
    fun `single PEE generates territory narrative`() {
        val events = listOf(event(DogWalkEventType.PEE))
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 20)
        assertTrue(
            "Should mention territory, got: $narrative",
            narrative.lowercase().contains("territor")
        )
    }

    @Test
    fun `multiple snacks uses plural form`() {
        val events = listOf(
            event(DogWalkEventType.SNACK_FOUND, 0),
            event(DogWalkEventType.SNACK_FOUND, 1000),
            event(DogWalkEventType.SNACK_FOUND, 2000),
        )
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 30)
        assertTrue("Should mention 3 snacks, got: $narrative", narrative.contains("3 snacks"))
    }

    @Test
    fun `squirrel chase plus zoomies triggers combo`() {
        val events = listOf(
            event(DogWalkEventType.SQUIRREL_CHASE, 0),
            event(DogWalkEventType.ZOOMIES, 1000),
        )
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 20)
        assertTrue(
            "Should mention squirrel and zoomie combo, got: $narrative",
            narrative.contains("squirrel") && narrative.contains("zoomie")
        )
    }

    @Test
    fun `mixed events include Juniper and duration`() {
        val events = listOf(
            event(DogWalkEventType.SNACK_FOUND, 0),
            event(DogWalkEventType.POOP, 5000),
            event(DogWalkEventType.FRIENDLY_DOG, 10000),
        )
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 45)
        assertTrue("Should mention Juniper, got: $narrative", narrative.contains("Juniper"))
        assertTrue("Should mention 45 minutes, got: $narrative", narrative.contains("45"))
    }

    @Test
    fun `bathroom combo with poop and pee mentions bathroom break`() {
        val events = listOf(
            event(DogWalkEventType.POOP, 0),
            event(DogWalkEventType.PEE, 5000),
            event(DogWalkEventType.SNACK_FOUND, 10000),
        )
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 30)
        assertTrue(
            "Should mention bathroom break, got: $narrative",
            narrative.lowercase().contains("bathroom break")
        )
    }

    @Test
    fun `single WATER_BREAK generates hydration narrative`() {
        val events = listOf(event(DogWalkEventType.WATER_BREAK))
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 20)
        assertTrue(
            "Should mention water break, got: $narrative",
            narrative.lowercase().contains("water break")
        )
    }

    @Test
    fun `single BARK_REACT generates bark narrative`() {
        val events = listOf(event(DogWalkEventType.BARK_REACT))
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 20)
        assertTrue(
            "Should mention barked, got: $narrative",
            narrative.lowercase().contains("bark")
        )
    }

    @Test
    fun `mixed events with WATER_BREAK includes water break`() {
        val events = listOf(
            event(DogWalkEventType.SNACK_FOUND, 0),
            event(DogWalkEventType.WATER_BREAK, 5000),
        )
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 30)
        assertTrue(
            "Should mention water break, got: $narrative",
            narrative.lowercase().contains("water break")
        )
    }

    @Test
    fun `mixed events with BARK_REACT includes barked`() {
        val events = listOf(
            event(DogWalkEventType.FRIENDLY_DOG, 0),
            event(DogWalkEventType.BARK_REACT, 5000),
        )
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 30)
        assertTrue(
            "Should mention barked, got: $narrative",
            narrative.lowercase().contains("barked")
        )
    }

    @Test
    fun `many events from all categories produces valid narrative`() {
        val events = listOf(
            event(DogWalkEventType.SNACK_FOUND, 0),
            event(DogWalkEventType.DEEP_SNIFF, 1000),
            event(DogWalkEventType.POOP, 2000),
            event(DogWalkEventType.PEE, 3000),
            event(DogWalkEventType.SQUIRREL_CHASE, 4000),
            event(DogWalkEventType.ZOOMIES, 5000),
            event(DogWalkEventType.FRIENDLY_DOG, 6000),
        )
        val narrative = DogWalkNarrativeGenerator.generateNarrative(events, 60)
        assertTrue(narrative.isNotBlank())
        assertTrue("Should mention Juniper", narrative.contains("Juniper"))
        assertTrue("Should end with !", narrative.endsWith("!"))
    }
}
