package com.gitfast.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gitfast.app.data.local.WorkoutStateStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WorkoutStateStoreTest {

    private lateinit var context: Context
    private lateinit var store: WorkoutStateStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        store = WorkoutStateStore(context)
        // Clear shared prefs between tests
        context.getSharedPreferences("workout_state", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    // --- Initial state ---

    @Test
    fun `hasActiveWorkout returns false when no workout set`() {
        assertFalse(store.hasActiveWorkout())
    }

    @Test
    fun `getActiveWorkoutId returns null when no workout set`() {
        assertNull(store.getActiveWorkoutId())
    }

    @Test
    fun `getActiveWorkoutStartTime returns null when no workout set`() {
        assertNull(store.getActiveWorkoutStartTime())
    }

    // --- setActiveWorkout ---

    @Test
    fun `setActiveWorkout stores workout ID`() {
        store.setActiveWorkout("workout-abc", 1000L)
        assertEquals("workout-abc", store.getActiveWorkoutId())
    }

    @Test
    fun `setActiveWorkout stores start time`() {
        store.setActiveWorkout("workout-abc", 98765L)
        assertEquals(98765L, store.getActiveWorkoutStartTime())
    }

    @Test
    fun `hasActiveWorkout returns true after setActiveWorkout`() {
        store.setActiveWorkout("w-1", 1000L)
        assertTrue(store.hasActiveWorkout())
    }

    @Test
    fun `setActiveWorkout overwrites previously stored workout`() {
        store.setActiveWorkout("w-first", 1000L)
        store.setActiveWorkout("w-second", 2000L)
        assertEquals("w-second", store.getActiveWorkoutId())
        assertEquals(2000L, store.getActiveWorkoutStartTime())
    }

    // --- clearActiveWorkout ---

    @Test
    fun `clearActiveWorkout removes workout ID`() {
        store.setActiveWorkout("w-1", 1000L)
        store.clearActiveWorkout()
        assertNull(store.getActiveWorkoutId())
    }

    @Test
    fun `clearActiveWorkout removes start time`() {
        store.setActiveWorkout("w-1", 1000L)
        store.clearActiveWorkout()
        assertNull(store.getActiveWorkoutStartTime())
    }

    @Test
    fun `hasActiveWorkout returns false after clearActiveWorkout`() {
        store.setActiveWorkout("w-1", 1000L)
        store.clearActiveWorkout()
        assertFalse(store.hasActiveWorkout())
    }

    @Test
    fun `clearActiveWorkout on empty store is safe`() {
        store.clearActiveWorkout() // should not throw
        assertFalse(store.hasActiveWorkout())
        assertNull(store.getActiveWorkoutId())
    }

    // --- Persistence ---

    @Test
    fun `state persists across new WorkoutStateStore instances`() {
        store.setActiveWorkout("w-persist", 55555L)

        val newStore = WorkoutStateStore(context)
        assertTrue(newStore.hasActiveWorkout())
        assertEquals("w-persist", newStore.getActiveWorkoutId())
        assertEquals(55555L, newStore.getActiveWorkoutStartTime())
    }

    @Test
    fun `clear persists across new WorkoutStateStore instances`() {
        store.setActiveWorkout("w-1", 1000L)
        store.clearActiveWorkout()

        val newStore = WorkoutStateStore(context)
        assertFalse(newStore.hasActiveWorkout())
    }
}
