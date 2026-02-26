package com.gitfast.app.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DogWalkEventTypeTest {

    @Test
    fun `all event types have non-blank displayName`() {
        for (type in DogWalkEventType.entries) {
            assertTrue(
                "${type.name} has blank displayName",
                type.displayName.isNotBlank()
            )
        }
    }

    @Test
    fun `all event types have non-blank icon`() {
        for (type in DogWalkEventType.entries) {
            assertTrue(
                "${type.name} has blank icon",
                type.icon.isNotBlank()
            )
        }
    }

    @Test
    fun `all event types have a category`() {
        for (type in DogWalkEventType.entries) {
            assertNotNull("${type.name} has null category", type.category)
        }
    }

    @Test
    fun `there are exactly 9 event types`() {
        assertTrue(DogWalkEventType.entries.size == 9)
    }

    @Test
    fun `display names are unique`() {
        val names = DogWalkEventType.entries.map { it.displayName }
        assertTrue(
            "Duplicate display names found",
            names.size == names.distinct().size
        )
    }

    @Test
    fun `icons are unique`() {
        val icons = DogWalkEventType.entries.map { it.icon }
        assertTrue(
            "Duplicate icons found",
            icons.size == icons.distinct().size
        )
    }

    @Test
    fun `FORAGING category has SNACK_FOUND, DEEP_SNIFF, and WATER_BREAK`() {
        val foraging = DogWalkEventType.entries.filter { it.category == EventCategory.FORAGING }
        assertTrue(foraging.contains(DogWalkEventType.SNACK_FOUND))
        assertTrue(foraging.contains(DogWalkEventType.DEEP_SNIFF))
        assertTrue(foraging.contains(DogWalkEventType.WATER_BREAK))
    }

    @Test
    fun `BATHROOM category has POOP and PEE`() {
        val bathroom = DogWalkEventType.entries.filter { it.category == EventCategory.BATHROOM }
        assertTrue(bathroom.contains(DogWalkEventType.POOP))
        assertTrue(bathroom.contains(DogWalkEventType.PEE))
    }

    @Test
    fun `ENERGY category has SQUIRREL_CHASE and ZOOMIES`() {
        val energy = DogWalkEventType.entries.filter { it.category == EventCategory.ENERGY }
        assertTrue(energy.contains(DogWalkEventType.SQUIRREL_CHASE))
        assertTrue(energy.contains(DogWalkEventType.ZOOMIES))
    }

    @Test
    fun `SOCIAL category has FRIENDLY_DOG and BARK_REACT`() {
        val social = DogWalkEventType.entries.filter { it.category == EventCategory.SOCIAL }
        assertTrue(social.contains(DogWalkEventType.FRIENDLY_DOG))
        assertTrue(social.contains(DogWalkEventType.BARK_REACT))
    }

    @Test
    fun `all 4 event categories are covered`() {
        val categories = DogWalkEventType.entries.map { it.category }.toSet()
        assertTrue(categories.contains(EventCategory.FORAGING))
        assertTrue(categories.contains(EventCategory.BATHROOM))
        assertTrue(categories.contains(EventCategory.ENERGY))
        assertTrue(categories.contains(EventCategory.SOCIAL))
    }
}
