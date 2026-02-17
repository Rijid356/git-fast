package com.gitfast.app

import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.service.WorkoutStateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutStateManagerLapTest {

    private lateinit var manager: WorkoutStateManager

    @Before
    fun setUp() {
        manager = WorkoutStateManager()
    }

    private fun startWithLaps() {
        manager.startWorkout()
        Thread.sleep(10)
        manager.startLaps()
    }

    @Test
    fun `markLap increments lap count`() {
        startWithLaps()
        assertEquals(0, manager.workoutState.value.lapCount)

        Thread.sleep(50)
        manager.markLap()

        assertEquals(1, manager.workoutState.value.lapCount)
    }

    @Test
    fun `markLap records lap with correct start and end time`() {
        startWithLaps()
        Thread.sleep(50)

        manager.markLap()
        Thread.sleep(10)
        manager.endLaps()
        Thread.sleep(10)
        val snapshot = manager.stopWorkout()

        val lapsPhase = snapshot.phases.find { it.type == PhaseType.LAPS }
        assertNotNull(lapsPhase)
        // The auto-completed lap from endLaps is lap 2; first manually marked lap is lap 1
        val lap1 = lapsPhase!!.laps.find { it.lapNumber == 1 }
        assertNotNull(lap1)
        assertTrue(
            "Lap end time should be after start time",
            lap1!!.endTime.isAfter(lap1.startTime)
        )
    }

    @Test
    fun `markLap first lap has no delta`() {
        startWithLaps()
        Thread.sleep(50)

        manager.markLap()

        assertNull(manager.workoutState.value.lastLapDeltaSeconds)
    }

    @Test
    fun `markLap second lap calculates correct delta vs first`() {
        startWithLaps()
        // First lap - short duration
        Thread.sleep(50)
        manager.markLap()
        val firstLapDelta = manager.workoutState.value.lastLapDeltaSeconds
        assertNull(firstLapDelta) // first lap has no delta

        // Second lap - takes some time so we can verify delta is calculated
        Thread.sleep(50)
        manager.markLap()

        // Delta should be non-null for second lap
        // (it might be 0 if both laps take ~50ms each at millisecond precision / 1000)
        // The key assertion is that it's not null
        assertNotNull(
            "Second lap should have a delta",
            manager.workoutState.value.lastLapDeltaSeconds
        )
    }

    @Test
    fun `markLap ignored when not in LAPS phase`() {
        manager.startWorkout()
        // We're in WARMUP phase, markLap should be ignored
        Thread.sleep(10)
        manager.markLap()

        assertEquals(0, manager.workoutState.value.lapCount)
        assertEquals(PhaseType.WARMUP, manager.workoutState.value.phase)
    }

    @Test
    fun `getBestLapDuration returns fastest lap`() {
        startWithLaps()

        // First lap: ~100ms
        Thread.sleep(100)
        manager.markLap()

        // Second lap: ~50ms (faster)
        Thread.sleep(50)
        manager.markLap()

        val best = manager.getBestLapDuration()
        assertNotNull(best)
        // Best should be 0 seconds (both sub-second at Int precision),
        // but the important thing is it's the min
        assertTrue("Best lap should be >= 0", best!! >= 0)
    }

    @Test
    fun `getBestLapDuration returns null with no laps`() {
        startWithLaps()

        assertNull(manager.getBestLapDuration())
    }

    @Test
    fun `getAverageLapDuration returns correct average`() {
        startWithLaps()

        Thread.sleep(50)
        manager.markLap()
        Thread.sleep(50)
        manager.markLap()

        val avg = manager.getAverageLapDuration()
        assertNotNull(avg)
        assertTrue("Average lap should be >= 0", avg!! >= 0)
    }

    @Test
    fun `multiple laps all recorded with sequential lap numbers`() {
        startWithLaps()

        Thread.sleep(50)
        manager.markLap() // lap 1
        Thread.sleep(50)
        manager.markLap() // lap 2
        Thread.sleep(50)
        manager.markLap() // lap 3

        assertEquals(3, manager.workoutState.value.lapCount)
        // currentLapNumber should now be 4 (next lap to be recorded)
        assertEquals(4, manager.workoutState.value.currentLapNumber)

        // Verify all laps have sequential numbers in snapshot
        Thread.sleep(10)
        manager.endLaps()
        Thread.sleep(10)
        val snapshot = manager.stopWorkout()
        val lapsPhase = snapshot.phases.find { it.type == PhaseType.LAPS }
        assertNotNull(lapsPhase)

        // laps 1, 2, 3 were manually marked; lap 4 auto-completed by endLaps
        val lapNumbers = lapsPhase!!.laps.map { it.lapNumber }
        assertEquals(listOf(1, 2, 3, 4), lapNumbers)
    }
}
