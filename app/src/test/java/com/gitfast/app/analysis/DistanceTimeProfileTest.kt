package com.gitfast.app.analysis

import com.gitfast.app.data.model.GpsPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class DistanceTimeProfileTest {

    private val baseTime = Instant.ofEpochMilli(1_000_000L)

    /** Create a GpsPoint at a given lat/lon and seconds offset from baseTime. */
    private fun gps(lat: Double, lon: Double, offsetSeconds: Int): GpsPoint {
        return GpsPoint(
            latitude = lat,
            longitude = lon,
            timestamp = baseTime.plusSeconds(offsetSeconds.toLong()),
            accuracy = 5f,
        )
    }

    @Test
    fun `empty points returns empty profile`() {
        val profile = DistanceTimeProfile.fromGpsPoints(emptyList(), baseTime)
        assertEquals(0.0, profile.totalDistanceMeters, 0.001)
        assertEquals(0, profile.totalSeconds)
        assertNull(profile.interpolateTimeAtDistance(100.0))
    }

    @Test
    fun `single point returns zero-distance profile`() {
        val profile = DistanceTimeProfile.fromGpsPoints(
            listOf(gps(40.0, -74.0, 0)),
            baseTime,
        )
        assertEquals(0.0, profile.totalDistanceMeters, 0.001)
        assertEquals(0, profile.totalSeconds)
        assertNull(profile.interpolateTimeAtDistance(1.0))
    }

    @Test
    fun `interpolate at distance zero returns zero`() {
        val points = listOf(
            gps(40.0, -74.0, 0),
            gps(40.001, -74.0, 10),
        )
        val profile = DistanceTimeProfile.fromGpsPoints(points, baseTime)
        assertEquals(0, profile.interpolateTimeAtDistance(0.0))
    }

    @Test
    fun `interpolate beyond total distance returns null`() {
        val points = listOf(
            gps(40.0, -74.0, 0),
            gps(40.001, -74.0, 10),
        )
        val profile = DistanceTimeProfile.fromGpsPoints(points, baseTime)
        assertNull(profile.interpolateTimeAtDistance(profile.totalDistanceMeters + 1.0))
    }

    @Test
    fun `interpolate at exact total distance returns null`() {
        val points = listOf(
            gps(40.0, -74.0, 0),
            gps(40.001, -74.0, 10),
        )
        val profile = DistanceTimeProfile.fromGpsPoints(points, baseTime)
        assertNull(profile.interpolateTimeAtDistance(profile.totalDistanceMeters))
    }

    @Test
    fun `interpolate at midpoint gives interpolated time`() {
        val points = listOf(
            gps(0.0, 0.0, 0),
            gps(0.001, 0.0, 100),
        )
        val profile = DistanceTimeProfile.fromGpsPoints(points, baseTime)
        val halfDistance = profile.totalDistanceMeters / 2.0
        val result = profile.interpolateTimeAtDistance(halfDistance)
        assertNotNull(result)
        assertEquals(50, result!!, 2.0)
    }

    @Test
    fun `profile from multiple points has increasing distances`() {
        val points = listOf(
            gps(0.0, 0.0, 0),
            gps(0.001, 0.0, 30),
            gps(0.002, 0.0, 60),
            gps(0.003, 0.0, 90),
        )
        val profile = DistanceTimeProfile.fromGpsPoints(points, baseTime)

        assertEquals(4, profile.distances.size)
        assertEquals(0.0, profile.distances[0], 0.001)
        for (i in 1 until profile.distances.size) {
            assert(profile.distances[i] > profile.distances[i - 1]) {
                "distances should be monotonically increasing"
            }
        }
    }

    @Test
    fun `profile elapsed times are monotonically non-decreasing`() {
        val points = listOf(
            gps(0.0, 0.0, 0),
            gps(0.001, 0.0, 30),
            gps(0.002, 0.0, 60),
        )
        val profile = DistanceTimeProfile.fromGpsPoints(points, baseTime)

        for (i in 1 until profile.elapsedSeconds.size) {
            assert(profile.elapsedSeconds[i] >= profile.elapsedSeconds[i - 1]) {
                "elapsed seconds should be non-decreasing"
            }
        }
    }

    @Test
    fun `interpolation between two known points is linear`() {
        // Points at 0, ~111m, ~222m with times 0, 50, 100
        val points = listOf(
            gps(0.0, 0.0, 0),
            gps(0.001, 0.0, 50),
            gps(0.002, 0.0, 100),
        )
        val profile = DistanceTimeProfile.fromGpsPoints(points, baseTime)

        // Query at 25% of total distance (~55m) falls in first segment (0-111m, 0-50s)
        // fraction = 0.25 total / 0.5 segment = 0.5 of first segment -> time = 25s
        val quarterDist = profile.totalDistanceMeters * 0.25
        val result = profile.interpolateTimeAtDistance(quarterDist)
        assertNotNull(result)
        assertEquals(25, result!!, 2.0)
    }

    @Test
    fun `negative distance returns zero`() {
        val points = listOf(
            gps(0.0, 0.0, 0),
            gps(0.001, 0.0, 60),
        )
        val profile = DistanceTimeProfile.fromGpsPoints(points, baseTime)
        assertEquals(0, profile.interpolateTimeAtDistance(-10.0))
    }

    private fun assertEquals(expected: Int, actual: Int, tolerance: Double) {
        assert(kotlin.math.abs(expected - actual) <= tolerance) {
            "Expected $expected +/- $tolerance but got $actual"
        }
    }
}
