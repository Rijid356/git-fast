package com.gitfast.app

import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.service.WorkoutStateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class WorkoutStateManagerPhaseTest {

    private lateinit var manager: WorkoutStateManager

    @Before
    fun setUp() {
        manager = WorkoutStateManager()
    }

    @Test
    fun `startWorkout sets phase to WARMUP`() {
        manager.startWorkout()

        assertEquals(PhaseType.WARMUP, manager.workoutState.value.phase)
    }

    @Test
    fun `startLaps transitions phase to LAPS`() {
        manager.startWorkout()
        Thread.sleep(10)

        manager.startLaps()

        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)
    }

    @Test
    fun `startLaps closes warmup phase with correct distance`() {
        manager.startWorkout()
        // Distance stays at 0.0 since we don't add GPS points
        Thread.sleep(10)

        manager.startLaps()
        Thread.sleep(10)

        // Now stop to get the snapshot with completed phases
        manager.endLaps()
        Thread.sleep(10)
        val snapshot = manager.stopWorkout()

        // First phase should be WARMUP with 0.0 distance (no GPS points added)
        val warmupPhase = snapshot.phases.find { it.type == PhaseType.WARMUP }
        assertNotNull(warmupPhase)
        assertEquals(0.0, warmupPhase!!.distanceMeters, 0.001)
    }

    @Test
    fun `endLaps transitions phase to COOLDOWN`() {
        manager.startWorkout()
        Thread.sleep(10)
        manager.startLaps()
        Thread.sleep(10)

        manager.endLaps()

        assertEquals(PhaseType.COOLDOWN, manager.workoutState.value.phase)
    }

    @Test
    fun `endLaps auto-completes in-progress lap`() {
        manager.startWorkout()
        Thread.sleep(10)
        manager.startLaps()
        // A lap is in-progress (lap 1 started automatically)
        Thread.sleep(50)

        manager.endLaps()
        Thread.sleep(10)
        val snapshot = manager.stopWorkout()

        // The LAPS phase should have the auto-completed lap
        val lapsPhase = snapshot.phases.find { it.type == PhaseType.LAPS }
        assertNotNull(lapsPhase)
        assertEquals(1, lapsPhase!!.laps.size)
        assertEquals(1, lapsPhase.laps[0].lapNumber)
    }

    @Test
    fun `stopWorkout from WARMUP produces single WARMUP phase in snapshot`() {
        manager.startWorkout()
        Thread.sleep(10)

        val snapshot = manager.stopWorkout()

        assertEquals(1, snapshot.phases.size)
        assertEquals(PhaseType.WARMUP, snapshot.phases[0].type)
    }

    @Test
    fun `stopWorkout from COOLDOWN produces three phases in snapshot`() {
        manager.startWorkout()
        Thread.sleep(10)
        manager.startLaps()
        Thread.sleep(10)
        manager.markLap()
        Thread.sleep(10)
        manager.endLaps()
        Thread.sleep(10)

        val snapshot = manager.stopWorkout()

        assertEquals(3, snapshot.phases.size)
        assertEquals(PhaseType.WARMUP, snapshot.phases[0].type)
        assertEquals(PhaseType.LAPS, snapshot.phases[1].type)
        assertEquals(PhaseType.COOLDOWN, snapshot.phases[2].type)
    }

    @Test
    fun `stopWorkout from LAPS produces two phases in snapshot`() {
        manager.startWorkout()
        Thread.sleep(10)
        manager.startLaps()
        Thread.sleep(10)
        manager.markLap()
        Thread.sleep(10)

        // Stop directly from LAPS (skip cooldown)
        val snapshot = manager.stopWorkout()

        assertEquals(2, snapshot.phases.size)
        assertEquals(PhaseType.WARMUP, snapshot.phases[0].type)
        assertEquals(PhaseType.LAPS, snapshot.phases[1].type)
    }

    @Test
    fun `phase distances sum to total workout distance`() {
        manager.startWorkout()
        // Without GPS points, all distances are 0.0 -- still sum should match
        Thread.sleep(10)
        manager.startLaps()
        Thread.sleep(10)
        manager.markLap()
        Thread.sleep(10)
        manager.endLaps()
        Thread.sleep(10)

        val snapshot = manager.stopWorkout()

        val phaseDistanceSum = snapshot.phases.sumOf { it.distanceMeters }
        assertEquals(snapshot.totalDistanceMeters, phaseDistanceSum, 0.001)
    }
}
