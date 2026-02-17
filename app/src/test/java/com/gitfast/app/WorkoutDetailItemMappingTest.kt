package com.gitfast.app

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.ui.detail.toDetailItem
import com.gitfast.app.util.DateFormatter
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class WorkoutDetailItemMappingTest {

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    private fun makeWorkout(
        id: String = "w1",
        startTime: Instant = Instant.parse("2024-03-15T10:00:00Z"),
        endTime: Instant? = Instant.parse("2024-03-15T10:30:00Z"),
        totalSteps: Int = 3000,
        distanceMeters: Double = 3218.69,
        status: WorkoutStatus = WorkoutStatus.COMPLETED,
        activityType: ActivityType = ActivityType.RUN,
        gpsPoints: List<GpsPoint> = emptyList(),
    ) = Workout(
        id = id,
        startTime = startTime,
        endTime = endTime,
        totalSteps = totalSteps,
        distanceMeters = distanceMeters,
        status = status,
        activityType = activityType,
        phases = emptyList(),
        gpsPoints = gpsPoints,
        dogName = null,
        notes = null,
        weatherCondition = null,
        weatherTemp = null,
        energyLevel = null,
        routeTag = null,
    )

    private fun makeGpsPoint(
        latitude: Double,
        longitude: Double,
        timestamp: Instant = Instant.parse("2024-03-15T10:00:00Z"),
        accuracy: Float = 5.0f,
    ) = GpsPoint(
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        accuracy = accuracy,
    )

    @Test
    fun `toDetailItem maps date and time correctly`() {
        val workout = makeWorkout()
        val item = workout.toDetailItem()

        assertEquals(DateFormatter.shortDate(workout.startTime), item.dateFormatted)
        assertEquals(DateFormatter.timeOfDay(workout.startTime), item.timeFormatted)
    }

    @Test
    fun `toDetailItem maps distance formatted correctly`() {
        val workout = makeWorkout(distanceMeters = 3218.69)
        val item = workout.toDetailItem()

        assertEquals(formatDistance(3218.69), item.distanceFormatted)
    }

    @Test
    fun `toDetailItem maps duration formatted correctly`() {
        val workout = makeWorkout()
        val item = workout.toDetailItem()

        // 30 minutes = 1800 seconds
        val durationSeconds = (workout.durationMillis!! / 1000).toInt()
        assertEquals(formatElapsedTime(durationSeconds), item.durationFormatted)
    }

    @Test
    fun `toDetailItem maps pace formatted correctly`() {
        val workout = makeWorkout()
        val item = workout.toDetailItem()

        val paceSeconds = workout.averagePaceSecondsPerMile!!.toInt()
        assertEquals(formatPace(paceSeconds), item.avgPaceFormatted)
    }

    @Test
    fun `null endTime shows dashes for duration`() {
        val workout = makeWorkout(endTime = null)
        val item = workout.toDetailItem()

        assertEquals("--:--", item.durationFormatted)
    }

    @Test
    fun `null endTime shows dashes for pace`() {
        val workout = makeWorkout(endTime = null)
        val item = workout.toDetailItem()

        assertEquals("-- /mi", item.avgPaceFormatted)
    }

    @Test
    fun `zero steps shows dashes`() {
        val workout = makeWorkout(totalSteps = 0)
        val item = workout.toDetailItem()

        assertEquals("--", item.stepsFormatted)
    }

    @Test
    fun `positive steps shows count as string`() {
        val workout = makeWorkout(totalSteps = 4500)
        val item = workout.toDetailItem()

        assertEquals("4500", item.stepsFormatted)
    }

    @Test
    fun `GPS points converted to LatLngPoints correctly`() {
        val points = listOf(
            makeGpsPoint(40.0, -74.0),
            makeGpsPoint(40.1, -74.1),
            makeGpsPoint(40.2, -74.2),
        )
        val workout = makeWorkout(gpsPoints = points)
        val item = workout.toDetailItem()

        assertEquals(3, item.routePoints.size)
        assertEquals(40.0, item.routePoints[0].latitude, 0.0001)
        assertEquals(-74.0, item.routePoints[0].longitude, 0.0001)
        assertEquals(40.1, item.routePoints[1].latitude, 0.0001)
        assertEquals(-74.1, item.routePoints[1].longitude, 0.0001)
        assertEquals(40.2, item.routePoints[2].latitude, 0.0001)
        assertEquals(-74.2, item.routePoints[2].longitude, 0.0001)
    }

    @Test
    fun `routeBounds calculated correctly from GPS points`() {
        val points = listOf(
            makeGpsPoint(40.0, -74.2),
            makeGpsPoint(40.2, -74.0),
            makeGpsPoint(40.1, -74.1),
        )
        val workout = makeWorkout(gpsPoints = points)
        val item = workout.toDetailItem()

        val bounds = item.routeBounds!!
        assertEquals(40.0, bounds.minLat, 0.0001)
        assertEquals(40.2, bounds.maxLat, 0.0001)
        assertEquals(-74.2, bounds.minLng, 0.0001)
        assertEquals(-74.0, bounds.maxLng, 0.0001)
    }

    @Test
    fun `routeBounds is null with fewer than 2 points`() {
        // Zero points
        val workout0 = makeWorkout(gpsPoints = emptyList())
        assertNull(workout0.toDetailItem().routeBounds)

        // One point
        val workout1 = makeWorkout(gpsPoints = listOf(makeGpsPoint(40.0, -74.0)))
        assertNull(workout1.toDetailItem().routeBounds)
    }

    @Test
    fun `avgGpsAccuracy calculated correctly`() {
        val points = listOf(
            makeGpsPoint(40.0, -74.0, accuracy = 4.0f),
            makeGpsPoint(40.1, -74.1, accuracy = 6.0f),
            makeGpsPoint(40.2, -74.2, accuracy = 8.0f),
        )
        val workout = makeWorkout(gpsPoints = points)
        val item = workout.toDetailItem()

        // Average of 4.0, 6.0, 8.0 = 6.0
        assertEquals(6.0f, item.avgGpsAccuracy!!, 0.001f)
    }

    @Test
    fun `avgGpsAccuracy is null with no GPS points`() {
        val workout = makeWorkout(gpsPoints = emptyList())
        val item = workout.toDetailItem()

        assertNull(item.avgGpsAccuracy)
    }
}
