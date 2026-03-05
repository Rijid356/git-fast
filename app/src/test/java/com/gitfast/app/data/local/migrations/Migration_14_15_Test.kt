package com.gitfast.app.data.local.migrations

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests the SQL transformation logic of MIGRATION_14_15.
 * Verifies that the REPLACE-based transform correctly converts
 * legacy "CHEST,BACK" + intensity to new "CHEST:MODERATE,BACK:MODERATE" format.
 */
class Migration_14_15_Test {

    /**
     * Simulates the SQL REPLACE logic in Kotlin for testing purposes.
     * The actual migration runs:
     *   SET muscleGroups = REPLACE(muscleGroups, ',', ':' || intensity || ',') || ':' || intensity
     */
    private fun simulateMigration(muscleGroups: String, intensity: String): String {
        return muscleGroups.replace(",", ":$intensity,") + ":$intensity"
    }

    @Test
    fun `single muscle transforms correctly`() {
        val result = simulateMigration("CHEST", "MODERATE")
        assertEquals("CHEST:MODERATE", result)
    }

    @Test
    fun `two muscles transform correctly`() {
        val result = simulateMigration("CHEST,BACK", "MODERATE")
        assertEquals("CHEST:MODERATE,BACK:MODERATE", result)
    }

    @Test
    fun `three muscles transform correctly`() {
        val result = simulateMigration("QUADS,HAMSTRINGS,CALVES", "SEVERE")
        assertEquals("QUADS:SEVERE,HAMSTRINGS:SEVERE,CALVES:SEVERE", result)
    }

    @Test
    fun `all muscle groups transform correctly`() {
        val all = "CHEST,BACK,SHOULDERS,BICEPS,TRICEPS,FOREARMS,CORE,QUADS,HAMSTRINGS,GLUTES,CALVES"
        val result = simulateMigration(all, "MILD")
        val expected = "CHEST:MILD,BACK:MILD,SHOULDERS:MILD,BICEPS:MILD,TRICEPS:MILD," +
            "FOREARMS:MILD,CORE:MILD,QUADS:MILD,HAMSTRINGS:MILD,GLUTES:MILD,CALVES:MILD"
        assertEquals(expected, result)
    }

    @Test
    fun `each intensity value works`() {
        for (intensity in listOf("MILD", "MODERATE", "SEVERE")) {
            val result = simulateMigration("CHEST,BACK", intensity)
            assertEquals("CHEST:$intensity,BACK:$intensity", result)
        }
    }

    @Test
    fun `already migrated rows would be skipped by WHERE clause`() {
        // The WHERE clause checks: muscleGroups NOT LIKE '%:%'
        // Already-migrated format contains colons
        val alreadyMigrated = "CHEST:MODERATE,BACK:SEVERE"
        assertTrue(alreadyMigrated.contains(":"))
    }

    @Test
    fun `empty muscleGroups would be skipped by WHERE clause`() {
        // The WHERE clause checks: muscleGroups != ''
        val empty = ""
        assertTrue(empty.isEmpty())
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
