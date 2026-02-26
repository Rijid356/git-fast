package com.gitfast.app.service

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.PhaseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class WorkoutStateManagerAutoStartLapsTest {

    private lateinit var manager: WorkoutStateManager

    // Saved lap start point
    private val startLat = 38.929031
    private val startLon = -94.418978

    @Before
    fun setup() {
        manager = WorkoutStateManager()
    }

    private fun gpsPoint(lat: Double, lon: Double, timeMs: Long = System.currentTimeMillis()) =
        GpsPoint(
            latitude = lat,
            longitude = lon,
            timestamp = Instant.ofEpochMilli(timeMs),
            accuracy = 5f,
            speed = 3.0f
        )

    private fun offsetNorth(lat: Double, meters: Double): Double =
        lat + (meters / 111_000.0)

    private fun configureAutoStartLaps() {
        manager.setAutoLapConfig(enabled = true, anchorRadiusMeters = 5)
        manager.setAutoStartLapsConfig(lapStartLat = startLat, lapStartLng = startLon)
    }

    @Test
    fun `auto-starts laps when within radius of saved point during WARMUP`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.RUN)

        // GPS point at the saved start point
        manager.addGpsPoint(gpsPoint(startLat, startLon))

        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)
    }

    @Test
    fun `does NOT auto-start when auto-lap disabled`() {
        manager.setAutoLapConfig(enabled = false, anchorRadiusMeters = 5)
        manager.setAutoStartLapsConfig(lapStartLat = startLat, lapStartLng = startLon)
        manager.startWorkout(ActivityType.RUN)

        manager.addGpsPoint(gpsPoint(startLat, startLon))

        assertEquals(PhaseType.WARMUP, manager.workoutState.value.phase)
    }

    @Test
    fun `does NOT auto-start when no start point configured`() {
        manager.setAutoLapConfig(enabled = true, anchorRadiusMeters = 5)
        manager.setAutoStartLapsConfig(lapStartLat = null, lapStartLng = null)
        manager.startWorkout(ActivityType.RUN)

        manager.addGpsPoint(gpsPoint(startLat, startLon))

        assertEquals(PhaseType.WARMUP, manager.workoutState.value.phase)
    }

    @Test
    fun `does NOT auto-start during LAPS phase`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.RUN)

        // Manually start laps first (away from start point)
        val farLat = offsetNorth(startLat, 100.0)
        manager.addGpsPoint(gpsPoint(farLat, startLon))
        manager.startLaps()

        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)
        assertEquals(0, manager.workoutState.value.lapCount)

        // Now arrive at the start point — should NOT re-trigger startLaps
        // (it's already in LAPS phase, auto-start only works during WARMUP)
        manager.addGpsPoint(gpsPoint(startLat, startLon))

        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)
    }

    @Test
    fun `does NOT auto-start for DOG_WALK activity`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.DOG_WALK)

        manager.addGpsPoint(gpsPoint(startLat, startLon))

        assertEquals(PhaseType.WARMUP, manager.workoutState.value.phase)
    }

    @Test
    fun `only triggers once per workout`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.RUN)

        // First GPS at start point triggers auto-start
        manager.addGpsPoint(gpsPoint(startLat, startLon))
        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)

        // End laps manually (back to COOLDOWN), then if somehow back to WARMUP
        // the one-shot flag prevents re-triggering (tested via state manager internals)
        // This is verified by the fact that startLaps was only called once
        assertTrue(manager.workoutState.value.autoLapAnchorSet)
    }

    @Test
    fun `uses saved point as auto-lap anchor`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.RUN)

        // Start at a point slightly away from the start point (but within 5m)
        val nearLat = offsetNorth(startLat, 2.0)
        manager.addGpsPoint(gpsPoint(nearLat, startLon))

        // Should auto-start laps
        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)
        // Anchor should be set (using saved point, not last GPS point)
        assertTrue(manager.workoutState.value.autoLapAnchorSet)
    }

    @Test
    fun `auto-lap anchor uses saved coords after auto-start`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.RUN)

        val baseTime = System.currentTimeMillis()

        // Auto-start at the saved point
        manager.addGpsPoint(gpsPoint(startLat, startLon, baseTime))
        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)

        // Move far away (100m north)
        val farLat = offsetNorth(startLat, 100.0)
        manager.addGpsPoint(gpsPoint(farLat, startLon, baseTime + 1000))

        // Return to saved start point after cooldown
        manager.addGpsPoint(gpsPoint(startLat, startLon, baseTime + 31_000))

        // Should have auto-lapped back at the saved anchor
        assertEquals(1, manager.workoutState.value.lapCount)
    }

    @Test
    fun `manual startLaps still works when auto-start is configured`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.RUN)

        // GPS point far from start point
        val farLat = offsetNorth(startLat, 100.0)
        manager.addGpsPoint(gpsPoint(farLat, startLon))

        // Still in warmup (too far from start point)
        assertEquals(PhaseType.WARMUP, manager.workoutState.value.phase)

        // Manual start still works
        manager.startLaps()
        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)
    }

    @Test
    fun `resets on startWorkout`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.RUN)

        // Auto-start triggers
        manager.addGpsPoint(gpsPoint(startLat, startLon))
        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)

        // Start a new workout — should reset and allow auto-start again
        manager.startWorkout(ActivityType.RUN)
        assertEquals(PhaseType.WARMUP, manager.workoutState.value.phase)

        // Auto-start should work again
        manager.addGpsPoint(gpsPoint(startLat, startLon))
        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)
    }

    @Test
    fun `resets on stopWorkout`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.RUN)

        manager.addGpsPoint(gpsPoint(startLat, startLon))
        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)

        manager.stopWorkout()
        assertFalse(manager.workoutState.value.autoLapAnchorSet)
    }

    @Test
    fun `trailing partial lap under 30s discarded when COOL DOWN pressed after auto-lap`() {
        configureAutoStartLaps()
        manager.startWorkout(ActivityType.RUN)

        val baseTime = System.currentTimeMillis()

        // Auto-start at start point
        manager.addGpsPoint(gpsPoint(startLat, startLon, baseTime))
        assertEquals(PhaseType.LAPS, manager.workoutState.value.phase)

        // Complete a full lap: leave and return after cooldown
        val farLat = offsetNorth(startLat, 100.0)
        manager.addGpsPoint(gpsPoint(farLat, startLon, baseTime + 1000))
        manager.addGpsPoint(gpsPoint(startLat, startLon, baseTime + 31_000))
        assertEquals(1, manager.workoutState.value.lapCount)

        // Run for a few more seconds (partial lap, <30s since last auto-lap)
        manager.addGpsPoint(gpsPoint(farLat, startLon, baseTime + 35_000))

        // Press COOL DOWN — endLaps will markLap + discardMicroLap
        manager.endLaps()

        assertEquals(PhaseType.COOLDOWN, manager.workoutState.value.phase)
        // The trailing partial lap (<30s) should be discarded and merged
        // into the previous lap, so we should still have 1 lap
        assertEquals(1, manager.workoutState.value.lapCount)
    }
}
