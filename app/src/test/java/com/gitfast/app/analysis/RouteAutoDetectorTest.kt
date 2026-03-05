package com.gitfast.app.analysis

import com.gitfast.app.data.model.GpsPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class RouteAutoDetectorTest {

    private val baseTime = Instant.parse("2026-03-05T10:00:00Z")

    /** Create a GPS point at a given lat/lon with a sequential timestamp. */
    private fun gps(lat: Double, lon: Double, offsetSeconds: Long = 0) = GpsPoint(
        latitude = lat,
        longitude = lon,
        timestamp = baseTime.plusSeconds(offsetSeconds),
        accuracy = 5f,
        speed = null,
    )

    /** Generate a line of GPS points heading north from a start point. */
    private fun northboundRoute(startLat: Double, startLon: Double, count: Int): List<GpsPoint> {
        return (0 until count).map { i ->
            gps(startLat + i * 0.00005, startLon, i * 2L) // ~5.5m per step northward
        }
    }

    /** Generate a line of GPS points heading east from a start point. */
    private fun eastboundRoute(startLat: Double, startLon: Double, count: Int): List<GpsPoint> {
        return (0 until count).map { i ->
            gps(startLat, startLon + i * 0.00020, i * 2L) // ~17.7m per step eastward
        }
    }

    @Test
    fun `exact same route returns match`() {
        val route = northboundRoute(38.926, -94.418, 15)
        val candidate = RouteAutoDetector.RouteCandidate("Park", route)

        val result = RouteAutoDetector.detect(route, listOf(candidate))

        assertEquals("Park", result.routeTag)
        assertEquals(0.0, result.avgDeviationMeters, 0.1)
    }

    @Test
    fun `similar route with GPS drift still matches`() {
        val reference = northboundRoute(38.926, -94.418, 15)
        // Shift ~15m east (well within 75m threshold)
        val current = northboundRoute(38.926, -94.41785, 10)
        val candidate = RouteAutoDetector.RouteCandidate("Park", reference)

        val result = RouteAutoDetector.detect(current, listOf(candidate))

        assertEquals("Park", result.routeTag)
        assert(result.avgDeviationMeters < RouteAutoDetector.MAX_DEVIATION_METERS)
    }

    @Test
    fun `completely different direction returns no match`() {
        val northRoute = northboundRoute(38.926, -94.418, 15)
        val eastRoute = eastboundRoute(38.926, -94.418, 10)
        val candidate = RouteAutoDetector.RouteCandidate("Park", northRoute)

        val result = RouteAutoDetector.detect(eastRoute, listOf(candidate))

        // East route diverges enough from north route to exceed threshold
        // At 10 points going east, the later points are far from the north reference
        assertNull(result.routeTag)
    }

    @Test
    fun `no candidates returns no match`() {
        val current = northboundRoute(38.926, -94.418, 10)

        val result = RouteAutoDetector.detect(current, emptyList())

        assertNull(result.routeTag)
    }

    @Test
    fun `multiple candidates picks closest match`() {
        val parkRoute = northboundRoute(38.926, -94.418, 15)
        // City route: same start but shifted slightly east — closer to current
        val cityRoute = northboundRoute(38.926, -94.41795, 15)
        // Current walk: very close to city route
        val current = northboundRoute(38.926, -94.41793, 10)

        val candidates = listOf(
            RouteAutoDetector.RouteCandidate("Park", parkRoute),
            RouteAutoDetector.RouteCandidate("City", cityRoute),
        )

        val result = RouteAutoDetector.detect(current, candidates)

        assertEquals("City", result.routeTag)
    }

    @Test
    fun `start proximity filter eliminates distant routes`() {
        // Route starts 500m away
        val distantRoute = northboundRoute(38.930, -94.418, 15)
        val current = northboundRoute(38.926, -94.418, 10)
        val candidate = RouteAutoDetector.RouteCandidate("Distant", distantRoute)

        val result = RouteAutoDetector.detect(current, listOf(candidate))

        assertNull(result.routeTag)
    }

    @Test
    fun `below MIN_POINTS threshold returns no match`() {
        val route = northboundRoute(38.926, -94.418, 15)
        val current = northboundRoute(38.926, -94.418, 5) // Only 5 points
        val candidate = RouteAutoDetector.RouteCandidate("Park", route)

        val result = RouteAutoDetector.detect(current, listOf(candidate))

        assertNull(result.routeTag)
    }

    @Test
    fun `candidate with empty reference points is skipped`() {
        val current = northboundRoute(38.926, -94.418, 10)
        val candidate = RouteAutoDetector.RouteCandidate("Empty", emptyList())

        val result = RouteAutoDetector.detect(current, listOf(candidate))

        assertNull(result.routeTag)
    }

    @Test
    fun `averageNearestPointDistance with identical points returns zero`() {
        val points = northboundRoute(38.926, -94.418, 5)
        val distance = RouteAutoDetector.averageNearestPointDistance(points, points)
        assertEquals(0.0, distance, 0.1)
    }

    @Test
    fun `averageNearestPointDistance with offset returns positive distance`() {
        val reference = northboundRoute(38.926, -94.418, 10)
        val shifted = northboundRoute(38.926, -94.41785, 10) // ~15m east
        val distance = RouteAutoDetector.averageNearestPointDistance(shifted, reference)
        assert(distance > 0.0)
        assert(distance < 30.0) // Should be roughly 13-15m
    }

    @Test
    fun `detection result has meaningful avgDeviationMeters on match`() {
        val reference = northboundRoute(38.926, -94.418, 15)
        val current = northboundRoute(38.926, -94.41790, 10) // ~9m east shift
        val candidate = RouteAutoDetector.RouteCandidate("Park", reference)

        val result = RouteAutoDetector.detect(current, listOf(candidate))

        assertNotNull(result.routeTag)
        assert(result.avgDeviationMeters > 0.0)
        assert(result.avgDeviationMeters < 20.0)
    }
}
