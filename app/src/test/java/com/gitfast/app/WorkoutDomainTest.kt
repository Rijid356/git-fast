package com.gitfast.app

import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class WorkoutDomainTest {

    private fun createWorkout(
        distanceMeters: Double = 1609.34,
        startTime: Instant = Instant.ofEpochMilli(0),
        endTime: Instant? = Instant.ofEpochMilli(600_000),
        status: WorkoutStatus = WorkoutStatus.COMPLETED
    ) = Workout(
        id = "w-1",
        startTime = startTime,
        endTime = endTime,
        totalSteps = 1000,
        distanceMeters = distanceMeters,
        status = status,
        phases = emptyList(),
        gpsPoints = emptyList()
    )

    @Test
    fun `distanceMiles converts correctly - 1609_34m equals approximately 1 mile`() {
        val workout = createWorkout(distanceMeters = 1609.34)

        assertEquals(1.0, workout.distanceMiles, 0.001)
    }

    @Test
    fun `distanceMiles converts zero meters to zero miles`() {
        val workout = createWorkout(distanceMeters = 0.0)

        assertEquals(0.0, workout.distanceMiles, 0.001)
    }

    @Test
    fun `durationMillis calculates correctly`() {
        val workout = createWorkout(
            startTime = Instant.ofEpochMilli(1000),
            endTime = Instant.ofEpochMilli(601_000)
        )

        assertEquals(600_000L, workout.durationMillis)
    }

    @Test
    fun `durationMillis returns null when endTime is null`() {
        val workout = createWorkout(endTime = null, status = WorkoutStatus.ACTIVE)

        assertNull(workout.durationMillis)
    }

    @Test
    fun `averagePaceSecondsPerMile calculates correctly`() {
        // 1 mile in 10 minutes (600 seconds)
        val workout = createWorkout(
            distanceMeters = 1609.34,
            startTime = Instant.ofEpochMilli(0),
            endTime = Instant.ofEpochMilli(600_000)
        )

        assertEquals(600.0, workout.averagePaceSecondsPerMile!!, 1.0)
    }

    @Test
    fun `averagePaceSecondsPerMile returns null for zero distance`() {
        val workout = createWorkout(
            distanceMeters = 0.0,
            startTime = Instant.ofEpochMilli(0),
            endTime = Instant.ofEpochMilli(600_000)
        )

        assertNull(workout.averagePaceSecondsPerMile)
    }

    @Test
    fun `averagePaceSecondsPerMile returns null when endTime is null`() {
        val workout = createWorkout(endTime = null, status = WorkoutStatus.ACTIVE)

        assertNull(workout.averagePaceSecondsPerMile)
    }

    @Test
    fun `averagePaceSecondsPerMile calculates correctly for 2 miles in 20 minutes`() {
        // 2 miles in 20 minutes = 600 seconds per mile
        val workout = createWorkout(
            distanceMeters = 3218.68, // ~2 miles
            startTime = Instant.ofEpochMilli(0),
            endTime = Instant.ofEpochMilli(1_200_000) // 20 minutes
        )

        assertEquals(600.0, workout.averagePaceSecondsPerMile!!, 1.0)
    }
}
