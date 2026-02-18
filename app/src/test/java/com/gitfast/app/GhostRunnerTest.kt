package com.gitfast.app

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.service.WorkoutStateManager
import com.gitfast.app.service.WorkoutTrackingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GhostRunnerTest {

    private lateinit var manager: WorkoutStateManager

    @Before
    fun setUp() {
        manager = WorkoutStateManager()
    }

    // --- setGhostLap ---

    @Test
    fun `setGhostLap sets ghost duration in state`() {
        manager.startWorkout()
        manager.setGhostLap(120)

        val state = manager.workoutState.value
        assertEquals(120, state.ghostLapDurationSeconds)
    }

    @Test
    fun `setGhostLap with null clears ghost`() {
        manager.startWorkout()
        manager.setGhostLap(120)
        manager.setGhostLap(null)

        val state = manager.workoutState.value
        assertNull(state.ghostLapDurationSeconds)
    }

    // --- Ghost delta is null before laps ---

    @Test
    fun `ghost delta is null during warmup`() {
        manager.startWorkout()
        manager.setGhostLap(120)
        manager.updateElapsedTime()

        val state = manager.workoutState.value
        assertEquals(PhaseType.WARMUP, state.phase)
        assertNull(state.ghostDeltaSeconds)
    }

    // --- Ghost delta computed during laps ---

    @Test
    fun `ghost delta computed during laps phase`() {
        manager.startWorkout()
        manager.setGhostLap(120)
        manager.startLaps()

        // Simulate time passing
        manager.updateElapsedTime()

        val state = manager.workoutState.value
        assertEquals(PhaseType.LAPS, state.phase)
        // Delta should be computed (currentLapElapsed - ghostDuration)
        assertNotNull(state.ghostDeltaSeconds)
    }

    @Test
    fun `ghost delta is negative when ahead of ghost`() {
        manager.startWorkout()
        manager.setGhostLap(300) // 5 minute ghost
        manager.startLaps()

        // Right after starting laps, lap elapsed is ~0s, ghost is 300s
        // So delta should be negative (ahead)
        manager.updateElapsedTime()

        val state = manager.workoutState.value
        assertTrue("Delta should be negative (ahead of ghost)", state.ghostDeltaSeconds!! < 0)
    }

    // --- Auto-ghost updates after lap completion ---

    @Test
    fun `auto ghost updates to best lap after first lap`() {
        manager.startWorkout()
        manager.startLaps()

        // No ghost should be set before first lap
        assertNull(manager.workoutState.value.ghostLapDurationSeconds)

        // Complete first lap
        Thread.sleep(50) // ensure non-zero duration
        manager.markLap()

        // Ghost should now be set to best lap (the only lap)
        val state = manager.workoutState.value
        assertNotNull(state.ghostLapDurationSeconds)
    }

    @Test
    fun `auto ghost tracks best lap across multiple laps`() {
        manager.startWorkout()
        manager.startLaps()

        // Lap 1 - short
        Thread.sleep(50)
        manager.markLap()
        val ghostAfterLap1 = manager.workoutState.value.ghostLapDurationSeconds

        // Lap 2 - longer
        Thread.sleep(100)
        manager.markLap()
        val ghostAfterLap2 = manager.workoutState.value.ghostLapDurationSeconds

        // Ghost should still be the best (shortest) lap
        assertEquals(ghostAfterLap1, ghostAfterLap2)
    }

    // --- External ghost overrides auto-ghost ---

    @Test
    fun `external ghost is not overridden by markLap`() {
        manager.startWorkout()
        manager.setGhostLap(999) // External ghost
        manager.startLaps()

        Thread.sleep(50)
        manager.markLap()

        // External ghost should persist, not be replaced by auto-ghost
        assertEquals(999, manager.workoutState.value.ghostLapDurationSeconds)
    }

    // --- Ghost resets ---

    @Test
    fun `ghost resets on stopWorkout`() {
        manager.startWorkout()
        manager.setGhostLap(120)
        manager.stopWorkout()

        val state = manager.workoutState.value
        assertNull(state.ghostLapDurationSeconds)
        assertNull(state.ghostDeltaSeconds)
    }

    @Test
    fun `ghost delta is null when no ghost is set`() {
        manager.startWorkout()
        manager.startLaps()
        manager.updateElapsedTime()

        val state = manager.workoutState.value
        assertNull(state.ghostDeltaSeconds)
    }

    // --- Ghost with no laps completed stays null for auto mode ---

    @Test
    fun `auto ghost is null before any lap completes`() {
        manager.startWorkout()
        manager.startLaps()

        assertNull(manager.workoutState.value.ghostLapDurationSeconds)
    }
}
