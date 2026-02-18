package com.gitfast.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementDefTest {

    @Test
    fun `all achievement ids are unique`() {
        val ids = AchievementDef.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all achievements have positive xp reward`() {
        for (def in AchievementDef.entries) {
            assertTrue("${def.id} has non-positive xpReward", def.xpReward > 0)
        }
    }

    @Test
    fun `findById returns correct achievement`() {
        val found = AchievementDef.findById("dist_first_mile")
        assertNotNull(found)
        assertEquals(AchievementDef.FIRST_MILE, found)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val found = AchievementDef.findById("nonexistent")
        assertNull(found)
    }

    @Test
    fun `byCategory groups all achievements`() {
        val grouped = AchievementDef.byCategory()
        val totalInGroups = grouped.values.sumOf { it.size }
        assertEquals(AchievementDef.entries.size, totalInGroups)
    }

    @Test
    fun `every category has at least one achievement`() {
        val grouped = AchievementDef.byCategory()
        for (category in AchievementCategory.entries) {
            assertTrue(
                "Category $category has no achievements",
                grouped.containsKey(category) && grouped[category]!!.isNotEmpty()
            )
        }
    }
}
