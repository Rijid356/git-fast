package com.gitfast.app.service

import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.util.DistanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class HomeArrivalDetectionTest {

    private lateinit var stateManager: WorkoutStateManager

    // Home location: 40.0, -74.0
    private val homeLat = 40.0
    private val homeLng = -74.0
    private val defaultRadius = 30

    @Before
    fun setup() {
        stateManager = WorkoutStateManager()
    }

    private fun point(lat: Double, lng: Double) = GpsPoint(
        latitude = lat,
        longitude = lng,
        timestamp = Instant.now(),
        accuracy = 5f,
        speed = 2.0f
    )

    @Test
    fun `homeArrivalPause sets isHomeArrivalPaused flag`() {
        stateManager.startWorkout()
        stateManager.homeArrivalPause()

        val state = stateManager.workoutState.value
        assertTrue("isPaused should be true", state.isPaused)
        assertTrue("isHomeArrivalPaused should be true", state.isHomeArrivalPaused)
        assertFalse("isAutoPaused should be false", state.isAutoPaused)
    }

    @Test
    fun `resumeWorkout clears isHomeArrivalPaused`() {
        stateManager.startWorkout()
        stateManager.homeArrivalPause()
        stateManager.resumeWorkout()

        val state = stateManager.workoutState.value
        assertFalse("isPaused should be false after resume", state.isPaused)
        assertFalse("isHomeArrivalPaused should be false after resume", state.isHomeArrivalPaused)
    }

    @Test
    fun `pauseWorkout clears isHomeArrivalPaused`() {
        stateManager.startWorkout()
        stateManager.homeArrivalPause()
        stateManager.pauseWorkout()

        val state = stateManager.workoutState.value
        assertTrue("isPaused should be true", state.isPaused)
        assertFalse("isHomeArrivalPaused should be false after manual pause", state.isHomeArrivalPaused)
    }

    @Test
    fun `autoResumeWorkout clears isHomeArrivalPaused`() {
        stateManager.startWorkout()
        stateManager.homeArrivalPause()
        stateManager.autoResumeWorkout()

        val state = stateManager.workoutState.value
        assertFalse("isPaused should be false", state.isPaused)
        assertFalse("isHomeArrivalPaused should be false", state.isHomeArrivalPaused)
    }

    @Test
    fun `distance check identifies point within home radius`() {
        // Point very close to home (same coordinates)
        val distance = DistanceCalculator.haversineMeters(
            homeLat, homeLng,
            homeLat, homeLng
        )
        assertTrue("Same point should be within radius", distance <= defaultRadius)
    }

    @Test
    fun `distance check identifies point outside home radius`() {
        // Point ~500m away from home
        val farLat = homeLat + 0.005
        val distance = DistanceCalculator.haversineMeters(
            homeLat, homeLng,
            farLat, homeLng
        )
        assertTrue("Point 500m away should be outside 30m radius", distance > defaultRadius)
    }

    @Test
    fun `home arrival detection - must leave home first pattern`() {
        // Simulates the "must leave home first" logic:
        // Starting at home should NOT trigger, only returning should
        var hasLeftHomeRadius = false
        var homeArrivalTriggered = false

        val points = listOf(
            // Start at home
            point(homeLat, homeLng),
            // Still at home
            point(homeLat + 0.0001, homeLng),
            // Leave home (500m away)
            point(homeLat + 0.005, homeLng),
            // Further away
            point(homeLat + 0.01, homeLng),
            // Return home
            point(homeLat + 0.0001, homeLng),
        )

        for (pt in points) {
            val distToHome = DistanceCalculator.haversineMeters(
                pt.latitude, pt.longitude,
                homeLat, homeLng
            )
            if (distToHome > defaultRadius) {
                hasLeftHomeRadius = true
            } else if (hasLeftHomeRadius && !homeArrivalTriggered) {
                homeArrivalTriggered = true
            }
        }

        assertTrue("Should have left home radius", hasLeftHomeRadius)
        assertTrue("Should have triggered home arrival", homeArrivalTriggered)
    }

    @Test
    fun `no trigger if workout starts at home and never leaves`() {
        var hasLeftHomeRadius = false
        var homeArrivalTriggered = false

        // All points near home
        val points = listOf(
            point(homeLat, homeLng),
            point(homeLat + 0.00005, homeLng),
            point(homeLat - 0.00005, homeLng),
        )

        for (pt in points) {
            val distToHome = DistanceCalculator.haversineMeters(
                pt.latitude, pt.longitude,
                homeLat, homeLng
            )
            if (distToHome > defaultRadius) {
                hasLeftHomeRadius = true
            } else if (hasLeftHomeRadius && !homeArrivalTriggered) {
                homeArrivalTriggered = true
            }
        }

        assertFalse("Should NOT have left home radius", hasLeftHomeRadius)
        assertFalse("Should NOT trigger home arrival", homeArrivalTriggered)
    }

    @Test
    fun `no re-trigger after first trigger`() {
        var hasLeftHomeRadius = false
        var homeArrivalTriggered = false
        var triggerCount = 0

        val points = listOf(
            // Start at home
            point(homeLat, homeLng),
            // Leave home
            point(homeLat + 0.005, homeLng),
            // Return home (first trigger)
            point(homeLat, homeLng),
            // Leave again
            point(homeLat + 0.005, homeLng),
            // Return again (should NOT trigger)
            point(homeLat, homeLng),
        )

        for (pt in points) {
            val distToHome = DistanceCalculator.haversineMeters(
                pt.latitude, pt.longitude,
                homeLat, homeLng
            )
            if (distToHome > defaultRadius) {
                hasLeftHomeRadius = true
            } else if (hasLeftHomeRadius && !homeArrivalTriggered) {
                homeArrivalTriggered = true
                triggerCount++
            }
        }

        assertEquals("Should only trigger once", 1, triggerCount)
    }

    @Test
    fun `various radius values affect detection`() {
        // Point ~20m from home
        val nearLat = homeLat + 0.00018 // ~20m
        val distance = DistanceCalculator.haversineMeters(
            homeLat, homeLng,
            nearLat, homeLng
        )

        // With 15m radius, point should be outside
        assertTrue("20m point should be outside 15m radius", distance > 15)
        // With 30m radius, point should be inside
        assertTrue("20m point should be inside 30m radius", distance <= 30)
    }

    @Test
    fun `isHomeArrivalPaused defaults to false`() {
        val state = WorkoutTrackingState()
        assertFalse("Default isHomeArrivalPaused should be false", state.isHomeArrivalPaused)
    }
}
