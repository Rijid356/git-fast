package com.gitfast.app

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.ui.history.toHistoryItem
import com.gitfast.app.util.DateFormatter
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class WorkoutHistoryItemMappingTest {

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
    ) = Workout(
        id = id,
        startTime = startTime,
        endTime = endTime,
        totalSteps = totalSteps,
        distanceMeters = distanceMeters,
        status = status,
        activityType = activityType,
        phases = emptyList(),
        gpsPoints = emptyList(),
        dogName = null,
        notes = null,
        weatherCondition = null,
        weatherTemp = null,
        energyLevel = null,
        routeTag = null,
    )

    @Test
    fun `toHistoryItem maps all fields correctly`() {
        val workout = makeWorkout()
        val item = workout.toHistoryItem()

        assertEquals("w1", item.workoutId)
        assertEquals(workout.startTime, item.startTime)
        assertEquals(DateFormatter.shortDate(workout.startTime), item.dateFormatted)
        assertEquals(DateFormatter.timeOfDay(workout.startTime), item.timeFormatted)
        assertEquals(DateFormatter.relativeDate(workout.startTime), item.relativeDateFormatted)
        assertEquals(formatDistance(3218.69), item.distanceFormatted)

        // 30 minutes = 1800 seconds
        val durationSeconds = (workout.durationMillis!! / 1000).toInt()
        assertEquals(formatElapsedTime(durationSeconds), item.durationFormatted)

        val paceSeconds = workout.averagePaceSecondsPerMile!!.toInt()
        assertEquals(formatPace(paceSeconds), item.avgPaceFormatted)
    }

    @Test
    fun `null endTime shows dashes for duration`() {
        val workout = makeWorkout(endTime = null)
        val item = workout.toHistoryItem()

        assertEquals("--:--", item.durationFormatted)
    }

    @Test
    fun `zero distance shows 0 point 00 mi`() {
        val workout = makeWorkout(distanceMeters = 0.0)
        val item = workout.toHistoryItem()

        assertEquals("0.00 mi", item.distanceFormatted)
    }

    @Test
    fun `null endTime shows dashes for pace`() {
        // When endTime is null, durationMillis is null, so averagePaceSecondsPerMile is null
        val workout = makeWorkout(endTime = null)
        val item = workout.toHistoryItem()

        assertEquals("-- /mi", item.avgPaceFormatted)
    }

    @Test
    fun `averagePaceSecondsPerMile value is cast to Int and formatted`() {
        // 2 miles in 30 min = 1800s, pace = 900 s/mi = 15:00 /mi
        val workout = makeWorkout(
            distanceMeters = 3218.69, // ~2 miles
            endTime = Instant.parse("2024-03-15T10:30:00Z"),
        )
        val item = workout.toHistoryItem()

        val paceSeconds = workout.averagePaceSecondsPerMile!!.toInt()
        assertEquals(formatPace(paceSeconds), item.avgPaceFormatted)
    }
}
