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

class WorkoutStateManagerAutoLapTest {

    private lateinit var manager: WorkoutStateManager

    // Reference anchor point (Central Park, NYC)
    private val anchorLat = 40.785091
    private val anchorLon = -73.968285

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

    /**
     * Offset a lat/lon by approximately `meters` to the north.
     * 1 degree latitude ~ 111,000 meters.
     */
    private fun offsetNorth(lat: Double, meters: Double): Double =
        lat + (meters / 111_000.0)

    private fun startWorkoutAndLaps() {
        manager.startWorkout(ActivityType.RUN)
        // Add initial GPS point at anchor location before starting laps
        manager.addGpsPoint(gpsPoint(anchorLat, anchorLon))
        manager.startLaps()
    }

    @Test
    fun `anchor captured on startLaps when auto-lap enabled`() {
        manager.setAutoLapConfig(enabled = true, anchorRadiusMeters = 15)
        startWorkoutAndLaps()

        val state = manager.workoutState.value
        assertTrue("Anchor should be set", state.autoLapAnchorSet)
        assertEquals(PhaseType.LAPS, state.phase)
    }

    @Test
    fun `no anchor captured when auto-lap disabled`() {
        manager.setAutoLapConfig(enabled = false, anchorRadiusMeters = 15)
        startWorkoutAndLaps()

        val state = manager.workoutState.value
        assertFalse("Anchor should NOT be set when disabled", state.autoLapAnchorSet)
    }

    @Test
    fun `lap triggered when returning to anchor after leaving radius`() {
        manager.setAutoLapConfig(enabled = true, anchorRadiusMeters = 15)
        startWorkoutAndLaps()

        val baseTime = System.currentTimeMillis()

        // Move far away from anchor (100m north)
        val farLat = offsetNorth(anchorLat, 100.0)
        manager.addGpsPoint(gpsPoint(farLat, anchorLon, baseTime + 1000))

        // Move further away to ensure hasLeftAnchorRadius is true
        val furtherLat = offsetNorth(anchorLat, 200.0)
        manager.addGpsPoint(gpsPoint(furtherLat, anchorLon, baseTime + 2000))

        // Verify no laps yet
        assertEquals(0, manager.workoutState.value.lapCount)

        // Wait past cooldown (simulate 31 seconds later) and return to anchor
        manager.addGpsPoint(gpsPoint(anchorLat, anchorLon, baseTime + 31_000))

        // Should have auto-triggered a lap
        assertEquals("Lap should be triggered on return to anchor", 1, manager.workoutState.value.lapCount)
    }

    @Test
    fun `no lap triggered when still within radius after startLaps`() {
        manager.setAutoLapConfig(enabled = true, anchorRadiusMeters = 15)
        startWorkoutAndLaps()

        val baseTime = System.currentTimeMillis()

        // Move slightly (5m north - still within 15m radius)
        val nearLat = offsetNorth(anchorLat, 5.0)
        manager.addGpsPoint(gpsPoint(nearLat, anchorLon, baseTime + 31_000))

        // Should NOT trigger a lap (haven't left the radius yet)
        assertEquals("No lap should trigger without leaving radius first", 0, manager.workoutState.value.lapCount)
    }

    @Test
    fun `no lap triggered during 30s cooldown`() {
        manager.setAutoLapConfig(enabled = true, anchorRadiusMeters = 15)
        startWorkoutAndLaps()

        val baseTime = System.currentTimeMillis()

        // Leave the radius
        val farLat = offsetNorth(anchorLat, 100.0)
        manager.addGpsPoint(gpsPoint(farLat, anchorLon, baseTime + 1000))

        // Return to anchor but only 10 seconds after startLaps (within 30s cooldown)
        // The lastAutoLapTime is set to the time of startLaps
        manager.addGpsPoint(gpsPoint(anchorLat, anchorLon, baseTime + 10_000))

        // Should NOT trigger because cooldown hasn't elapsed
        assertEquals("No lap during cooldown period", 0, manager.workoutState.value.lapCount)
    }

    @Test
    fun `hasLeftAnchorRadius resets after lap trigger`() {
        manager.setAutoLapConfig(enabled = true, anchorRadiusMeters = 15)
        startWorkoutAndLaps()

        val baseTime = System.currentTimeMillis()

        // First lap: leave and return
        val farLat = offsetNorth(anchorLat, 100.0)
        manager.addGpsPoint(gpsPoint(farLat, anchorLon, baseTime + 1000))
        manager.addGpsPoint(gpsPoint(anchorLat, anchorLon, baseTime + 31_000))
        assertEquals(1, manager.workoutState.value.lapCount)

        // Immediately add another point at anchor (hasn't left again)
        manager.addGpsPoint(gpsPoint(anchorLat, anchorLon, baseTime + 62_000))
        assertEquals("Should not trigger second lap without leaving first", 1, manager.workoutState.value.lapCount)

        // Leave and return again for second lap
        manager.addGpsPoint(gpsPoint(farLat, anchorLon, baseTime + 63_000))
        manager.addGpsPoint(gpsPoint(anchorLat, anchorLon, baseTime + 94_000))
        assertEquals("Second lap should trigger after leaving and returning", 2, manager.workoutState.value.lapCount)
    }

    @Test
    fun `no lap triggered when auto-lap disabled`() {
        manager.setAutoLapConfig(enabled = false, anchorRadiusMeters = 15)
        startWorkoutAndLaps()

        val baseTime = System.currentTimeMillis()

        // Leave and return
        val farLat = offsetNorth(anchorLat, 100.0)
        manager.addGpsPoint(gpsPoint(farLat, anchorLon, baseTime + 1000))
        manager.addGpsPoint(gpsPoint(anchorLat, anchorLon, baseTime + 31_000))

        assertEquals("No auto-lap when disabled", 0, manager.workoutState.value.lapCount)
    }

    @Test
    fun `anchor state reset on stopWorkout`() {
        manager.setAutoLapConfig(enabled = true, anchorRadiusMeters = 15)
        startWorkoutAndLaps()

        assertTrue(manager.workoutState.value.autoLapAnchorSet)

        manager.stopWorkout()

        assertFalse("Anchor should be cleared after stop", manager.workoutState.value.autoLapAnchorSet)
    }

    @Test
    fun `manual markLap still works during auto-lap mode`() {
        manager.setAutoLapConfig(enabled = true, anchorRadiusMeters = 15)
        startWorkoutAndLaps()

        // Manually mark a lap
        manager.markLap()
        assertEquals("Manual lap should work", 1, manager.workoutState.value.lapCount)
    }
}
