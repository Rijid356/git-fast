package com.gitfast.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseCatalogTest {

    @Test
    fun `catalog has at least 75 exercises`() {
        assertTrue("Expected 75+ exercises, got ${ExerciseCatalog.all.size}", ExerciseCatalog.all.size >= 75)
    }

    @Test
    fun `all exercise IDs are unique`() {
        val ids = ExerciseCatalog.all.map { it.id }
        assertEquals("Duplicate IDs found", ids.size, ids.distinct().size)
    }

    @Test
    fun `all exercises have non-empty name and description`() {
        for (exercise in ExerciseCatalog.all) {
            assertTrue("Exercise ${exercise.id} has empty name", exercise.name.isNotBlank())
            assertTrue("Exercise ${exercise.id} has empty description", exercise.description.isNotBlank())
        }
    }

    @Test
    fun `all exercises have at least one primary muscle`() {
        for (exercise in ExerciseCatalog.all) {
            assertTrue("Exercise ${exercise.id} has no primary muscles", exercise.primaryMuscles.isNotEmpty())
        }
    }

    @Test
    fun `bodyweight list has at least 35 exercises`() {
        assertTrue(ExerciseCatalog.bodyweight.size >= 35)
    }

    @Test
    fun `pull-up bar list has at least 13 exercises`() {
        assertTrue(ExerciseCatalog.pullUpBar.size >= 13)
    }

    @Test
    fun `dumbbell list has at least 22 exercises`() {
        assertTrue(ExerciseCatalog.dumbbell.size >= 22)
    }

    @Test
    fun `getById returns correct exercise`() {
        val exercise = ExerciseCatalog.getById("bw_push_up")
        assertNotNull(exercise)
        assertEquals("Push-Up", exercise!!.name)
        assertEquals(Equipment.BODYWEIGHT, exercise.equipment)
    }

    @Test
    fun `getById returns null for unknown ID`() {
        assertNull(ExerciseCatalog.getById("nonexistent_exercise"))
    }

    @Test
    fun `getByEquipment returns only matching equipment`() {
        val pullUpExercises = ExerciseCatalog.getByEquipment(Equipment.PULL_UP_BAR)
        assertTrue(pullUpExercises.isNotEmpty())
        assertTrue(pullUpExercises.all { it.equipment == Equipment.PULL_UP_BAR })
    }

    @Test
    fun `getByEquipment returns same count as category list`() {
        assertEquals(ExerciseCatalog.bodyweight.size, ExerciseCatalog.getByEquipment(Equipment.BODYWEIGHT).size)
        assertEquals(ExerciseCatalog.pullUpBar.size, ExerciseCatalog.getByEquipment(Equipment.PULL_UP_BAR).size)
        assertEquals(ExerciseCatalog.dumbbell.size, ExerciseCatalog.getByEquipment(Equipment.DUMBBELL).size)
    }

    @Test
    fun `getByMuscleGroup returns exercises with matching primary or secondary`() {
        val chestExercises = ExerciseCatalog.getByMuscleGroup(MuscleGroup.CHEST)
        assertTrue(chestExercises.isNotEmpty())
        assertTrue(chestExercises.all {
            MuscleGroup.CHEST in it.primaryMuscles || MuscleGroup.CHEST in it.secondaryMuscles
        })
    }

    @Test
    fun `getByCategory returns only matching category`() {
        val coreExercises = ExerciseCatalog.getByCategory(ExerciseCategory.CORE)
        assertTrue(coreExercises.isNotEmpty())
        assertTrue(coreExercises.all { it.category == ExerciseCategory.CORE })
    }

    @Test
    fun `all dumbbell exercises have hasWeight true`() {
        assertTrue(ExerciseCatalog.dumbbell.all { it.hasWeight })
    }

    @Test
    fun `bodyweight exercises do not have hasWeight`() {
        assertTrue(ExerciseCatalog.bodyweight.none { it.hasWeight })
    }

    @Test
    fun `all difficulty levels are represented`() {
        val difficulties = ExerciseCatalog.all.map { it.difficulty }.toSet()
        assertEquals(Difficulty.entries.toSet(), difficulties)
    }

    @Test
    fun `all categories are represented`() {
        val categories = ExerciseCatalog.all.map { it.category }.toSet()
        assertEquals(ExerciseCategory.entries.toSet(), categories)
    }

    @Test
    fun `all equipment types are represented`() {
        val equipment = ExerciseCatalog.all.map { it.equipment }.toSet()
        assertEquals(Equipment.entries.toSet(), equipment)
    }
}
