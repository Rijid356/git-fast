package com.gitfast.app

import com.gitfast.app.ui.workout.WorkoutUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutUiStateTest {

    @Test
    fun `default WorkoutUiState has expected placeholder values`() {
        val state = WorkoutUiState()

        assertFalse(state.isActive)
        assertFalse(state.isPaused)
        assertNull(state.workoutId)
        assertEquals("00:00", state.elapsedTimeFormatted)
        assertEquals("0.00 mi", state.distanceFormatted)
        assertNull(state.currentPaceFormatted)
        assertNull(state.averagePaceFormatted)
        assertEquals(0, state.stepCount)
        assertFalse(state.isWorkoutComplete)
    }

    @Test
    fun `copy with updated values works correctly`() {
        val original = WorkoutUiState()
        val updated = original.copy(
            isActive = true,
            isPaused = false,
            workoutId = "workout-123",
            elapsedTimeFormatted = "05:30",
            distanceFormatted = "1.25 mi",
            currentPaceFormatted = "8:15 /mi",
            averagePaceFormatted = "8:45 /mi",
            stepCount = 42,
            isWorkoutComplete = false,
        )

        assertTrue(updated.isActive)
        assertFalse(updated.isPaused)
        assertEquals("workout-123", updated.workoutId)
        assertEquals("05:30", updated.elapsedTimeFormatted)
        assertEquals("1.25 mi", updated.distanceFormatted)
        assertEquals("8:15 /mi", updated.currentPaceFormatted)
        assertEquals("8:45 /mi", updated.averagePaceFormatted)
        assertEquals(42, updated.stepCount)
        assertFalse(updated.isWorkoutComplete)

        // original should be unchanged
        assertFalse(original.isActive)
        assertEquals("00:00", original.elapsedTimeFormatted)
        assertEquals(0, original.stepCount)
    }
}
