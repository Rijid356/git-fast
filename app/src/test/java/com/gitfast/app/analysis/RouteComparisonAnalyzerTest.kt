package com.gitfast.app.analysis

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RouteComparisonAnalyzerTest {

    private fun makeWalk(
        id: String = "walk-1",
        startMillis: Long = 1000000L,
        endMillis: Long = 1000000L + 25 * 60 * 1000L,  // 25 min
        distanceMeters: Double = 1800.0,
        routeTag: String? = "neighborhood"
    ): Workout {
        return Workout(
            id = id,
            startTime = Instant.ofEpochMilli(startMillis),
            endTime = Instant.ofEpochMilli(endMillis),
            totalSteps = 0,
            distanceMeters = distanceMeters,
            status = WorkoutStatus.COMPLETED,
            activityType = ActivityType.DOG_WALK,
            phases = emptyList(),
            gpsPoints = emptyList(),
            dogName = "Juniper",
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = routeTag
        )
    }

    @Test
    fun `compare with no previous walks returns only current walk item`() {
        val current = makeWalk(id = "current")
        val result = RouteComparisonAnalyzer.compare(current, emptyList())

        assertEquals(1, result.size)
        assertTrue(result[0].isCurrentWalk)
        assertEquals("current", result[0].workoutId)
        assertNull(result[0].deltaFormatted)
        assertNull(result[0].deltaMillis)
    }

    @Test
    fun `compare current faster than previous shows positive delta`() {
        val current = makeWalk(
            id = "current",
            endMillis = 1000000L + 25 * 60 * 1000L  // 25 min
        )
        val previous = makeWalk(
            id = "prev",
            startMillis = 500000L,
            endMillis = 500000L + 27 * 60 * 1000L  // 27 min
        )

        val result = RouteComparisonAnalyzer.compare(current, listOf(previous))

        assertEquals(2, result.size)
        // previous took 27min, current took 25min → delta = 27min - 25min = +2min
        val prevItem = result[1]
        assertEquals("+2:00", prevItem.deltaFormatted)
        assertEquals(2 * 60 * 1000L, prevItem.deltaMillis)
    }

    @Test
    fun `compare current slower than previous shows negative delta`() {
        val current = makeWalk(
            id = "current",
            endMillis = 1000000L + 27 * 60 * 1000L  // 27 min
        )
        val previous = makeWalk(
            id = "prev",
            startMillis = 500000L,
            endMillis = 500000L + 25 * 60 * 1000L  // 25 min
        )

        val result = RouteComparisonAnalyzer.compare(current, listOf(previous))

        assertEquals(2, result.size)
        // previous took 25min, current took 27min → delta = 25min - 27min = -2min
        val prevItem = result[1]
        assertEquals("-2:00", prevItem.deltaFormatted)
        assertEquals(-2 * 60 * 1000L, prevItem.deltaMillis)
    }

    @Test
    fun `compare respects maxComparisons limit`() {
        val current = makeWalk(id = "current")
        val previousWalks = (1..10).map { i ->
            makeWalk(
                id = "prev-$i",
                startMillis = 500000L + i * 100000L,
                endMillis = 500000L + i * 100000L + 26 * 60 * 1000L
            )
        }

        val result = RouteComparisonAnalyzer.compare(current, previousWalks, maxComparisons = 3)

        assertEquals(4, result.size) // 1 current + 3 previous
    }

    @Test
    fun `compare first item is always current walk`() {
        val current = makeWalk(id = "current")
        val previous = makeWalk(
            id = "prev",
            startMillis = 500000L,
            endMillis = 500000L + 26 * 60 * 1000L
        )

        val result = RouteComparisonAnalyzer.compare(current, listOf(previous))

        assertTrue(result[0].isCurrentWalk)
        assertEquals("current", result[0].workoutId)
        assertEquals("Today", result[0].dateFormatted)
    }

    @Test
    fun `compare empty previous walks returns only current`() {
        val current = makeWalk(id = "solo-walk")

        val result = RouteComparisonAnalyzer.compare(current, emptyList())

        assertEquals(1, result.size)
        assertTrue(result[0].isCurrentWalk)
        assertEquals("solo-walk", result[0].workoutId)
        assertNull(result[0].deltaFormatted)
        assertNull(result[0].deltaMillis)
    }
}
