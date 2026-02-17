package com.gitfast.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class WorkoutDomainDogWalkTest {

    private fun makeWorkout(
        activityType: ActivityType = ActivityType.RUN,
        dogName: String? = null,
        notes: String? = null,
        weatherCondition: WeatherCondition? = null,
        weatherTemp: WeatherTemp? = null,
        energyLevel: EnergyLevel? = null,
        routeTag: String? = null
    ): Workout {
        return Workout(
            id = "test-1",
            startTime = Instant.ofEpochMilli(1000000L),
            endTime = Instant.ofEpochMilli(2000000L),
            totalSteps = 0,
            distanceMeters = 1609.34,
            status = WorkoutStatus.COMPLETED,
            activityType = activityType,
            phases = emptyList(),
            gpsPoints = emptyList(),
            dogName = dogName,
            notes = notes,
            weatherCondition = weatherCondition,
            weatherTemp = weatherTemp,
            energyLevel = energyLevel,
            routeTag = routeTag
        )
    }

    @Test
    fun `activityLabel returns Run for RUN type`() {
        val workout = makeWorkout(activityType = ActivityType.RUN)
        assertEquals("Run", workout.activityLabel)
    }

    @Test
    fun `activityLabel returns Dog Walk for DOG_WALK type`() {
        val workout = makeWorkout(activityType = ActivityType.DOG_WALK)
        assertEquals("Dog Walk", workout.activityLabel)
    }

    @Test
    fun `weatherSummary returns null when both weather fields are null`() {
        val workout = makeWorkout()
        assertNull(workout.weatherSummary)
    }

    @Test
    fun `weatherSummary returns only temp when condition is null`() {
        val workout = makeWorkout(weatherTemp = WeatherTemp.MILD)
        assertEquals("Mild", workout.weatherSummary)
    }

    @Test
    fun `weatherSummary returns only condition when temp is null`() {
        val workout = makeWorkout(weatherCondition = WeatherCondition.SUNNY)
        assertEquals("Sunny", workout.weatherSummary)
    }

    @Test
    fun `weatherSummary returns both when both are set`() {
        val workout = makeWorkout(
            weatherTemp = WeatherTemp.MILD,
            weatherCondition = WeatherCondition.SUNNY
        )
        assertEquals("Mild, Sunny", workout.weatherSummary)
    }
}
