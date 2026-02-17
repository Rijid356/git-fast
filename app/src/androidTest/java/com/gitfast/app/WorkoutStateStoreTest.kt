package com.gitfast.app

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gitfast.app.data.local.WorkoutStateStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutStateStoreTest {

    private lateinit var store: WorkoutStateStore

    @Before
    fun setUp() {
        store = WorkoutStateStore(ApplicationProvider.getApplicationContext())
        store.clearActiveWorkout()
    }

    @Test
    fun hasActiveWorkoutReturnsFalseOnFreshState() {
        assertFalse(store.hasActiveWorkout())
    }

    @Test
    fun getActiveWorkoutIdReturnsNullOnFreshState() {
        assertNull(store.getActiveWorkoutId())
    }

    @Test
    fun getActiveWorkoutStartTimeReturnsNullOnFreshState() {
        assertNull(store.getActiveWorkoutStartTime())
    }

    @Test
    fun setActiveWorkoutStoresWorkoutId() {
        store.setActiveWorkout("w-abc", 1000L)

        assertEquals("w-abc", store.getActiveWorkoutId())
    }

    @Test
    fun setActiveWorkoutStoresStartTime() {
        store.setActiveWorkout("w-abc", 5000L)

        assertEquals(5000L, store.getActiveWorkoutStartTime())
    }

    @Test
    fun hasActiveWorkoutReturnsTrueWhenSet() {
        store.setActiveWorkout("w-abc", 1000L)

        assertTrue(store.hasActiveWorkout())
    }

    @Test
    fun clearActiveWorkoutRemovesWorkoutId() {
        store.setActiveWorkout("w-abc", 1000L)
        store.clearActiveWorkout()

        assertNull(store.getActiveWorkoutId())
    }

    @Test
    fun clearActiveWorkoutRemovesStartTime() {
        store.setActiveWorkout("w-abc", 1000L)
        store.clearActiveWorkout()

        assertNull(store.getActiveWorkoutStartTime())
    }

    @Test
    fun hasActiveWorkoutReturnsFalseAfterClear() {
        store.setActiveWorkout("w-abc", 1000L)
        store.clearActiveWorkout()

        assertFalse(store.hasActiveWorkout())
    }

    @Test
    fun setActiveWorkoutOverwritesPreviousValues() {
        store.setActiveWorkout("w-first", 1000L)
        store.setActiveWorkout("w-second", 2000L)

        assertEquals("w-second", store.getActiveWorkoutId())
        assertEquals(2000L, store.getActiveWorkoutStartTime())
    }
}
