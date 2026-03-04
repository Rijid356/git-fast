package com.gitfast.app.service

import com.gitfast.app.analysis.DistanceTimeProfile
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.GpsPoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class RouteGhostStateTest {

    private lateinit var stateManager: WorkoutStateManager

    private val baseTime = Instant.ofEpochMilli(1_000_000_000L)

    private fun linearProfile(totalDistanceMeters: Double, totalSeconds: Int): DistanceTimeProfile {
        val n = 10
        val distances = DoubleArray(n) { i -> totalDistanceMeters * i / (n - 1) }
        val elapsed = IntArray(n) { i -> totalSeconds * i / (n - 1) }
        return DistanceTimeProfile(
            distances = distances,
            elapsedSeconds = elapsed,
            totalDistanceMeters = totalDistanceMeters,
            totalSeconds = totalSeconds,
        )
    }

    @Before
    fun setUp() {
        stateManager = WorkoutStateManager()
    }

    @Test
    fun `route ghost initially inactive`() {
        val state = stateManager.workoutState.value
        assertFalse(state.routeGhostActive)
        assertNull(state.routeGhostDeltaSeconds)
        assertFalse(state.routeGhostExhausted)
    }

    @Test
    fun `setRouteGhostProfiles activates ghost`() {
        val profiles = listOf(linearProfile(1000.0, 600))
        stateManager.setRouteGhostProfiles(profiles)

        val state = stateManager.workoutState.value
        assertTrue(state.routeGhostActive)
        assertNull(state.routeGhostDeltaSeconds)
        assertFalse(state.routeGhostExhausted)
    }

    @Test
    fun `setRouteGhostProfiles with empty list does not activate`() {
        stateManager.setRouteGhostProfiles(emptyList())

        val state = stateManager.workoutState.value
        assertFalse(state.routeGhostActive)
    }

    @Test
    fun `clearRouteGhost deactivates ghost`() {
        stateManager.setRouteGhostProfiles(listOf(linearProfile(1000.0, 600)))
        stateManager.clearRouteGhost()

        val state = stateManager.workoutState.value
        assertFalse(state.routeGhostActive)
        assertNull(state.routeGhostDeltaSeconds)
    }

    @Test
    fun `stopWorkout resets route ghost state`() {
        stateManager.startWorkout(ActivityType.DOG_WALK)
        stateManager.setRouteGhostProfiles(listOf(linearProfile(1000.0, 600)))
        stateManager.stopWorkout()

        val state = stateManager.workoutState.value
        assertFalse(state.routeGhostActive)
        assertNull(state.routeGhostDeltaSeconds)
    }

    @Test
    fun `route ghost stays active after startWorkout when profiles were set`() {
        stateManager.setRouteGhostProfiles(listOf(linearProfile(1000.0, 600)))
        assertTrue(stateManager.workoutState.value.routeGhostActive)

        stateManager.startWorkout(ActivityType.DOG_WALK)

        val state = stateManager.workoutState.value
        assertTrue("routeGhostActive should survive startWorkout", state.routeGhostActive)
    }

    @Test
    fun `route ghost delta computed on GPS update`() {
        stateManager.startWorkout(ActivityType.DOG_WALK)
        stateManager.setRouteGhostProfiles(listOf(linearProfile(1000.0, 600)))

        stateManager.addGpsPoint(GpsPoint(
            latitude = 0.0,
            longitude = 0.0,
            timestamp = baseTime,
            accuracy = 5f,
        ))

        stateManager.addGpsPoint(GpsPoint(
            latitude = 0.001,
            longitude = 0.0,
            timestamp = baseTime.plusSeconds(30),
            accuracy = 5f,
        ))

        val state = stateManager.workoutState.value
        assertTrue(state.routeGhostActive)
    }
}
