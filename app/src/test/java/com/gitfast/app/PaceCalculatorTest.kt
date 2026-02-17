package com.gitfast.app

import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.util.DistanceCalculator
import com.gitfast.app.util.PaceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class PaceCalculatorTest {

    // --- averagePace tests ---

    @Test
    fun `averagePace 600 seconds and 1 mile returns 600`() {
        val distanceMeters = DistanceCalculator.milesToMeters(1.0)
        val pace = PaceCalculator.averagePace(600, distanceMeters)
        assertEquals(600, pace)
    }

    @Test
    fun `averagePace 1800 seconds and 3 miles returns 600`() {
        val distanceMeters = DistanceCalculator.milesToMeters(3.0)
        val pace = PaceCalculator.averagePace(1800, distanceMeters)
        assertEquals(600, pace)
    }

    @Test
    fun `averagePace returns null when distance less than 0 point 01 miles`() {
        // 0.005 miles in meters
        val distanceMeters = DistanceCalculator.milesToMeters(0.005)
        val pace = PaceCalculator.averagePace(60, distanceMeters)
        assertNull(pace)
    }

    @Test
    fun `averagePace returns null when distance is zero`() {
        val pace = PaceCalculator.averagePace(600, 0.0)
        assertNull(pace)
    }

    // --- currentPace tests ---

    @Test
    fun `currentPace reasonable pace with good GPS points`() {
        // Generate 10 points moving north, each ~5 meters apart, 2 seconds apart
        // Total: ~45m in 18s. 45m = ~0.028 miles.
        // Pace = 18 / 0.028 ~= 643 sec/mi (~10:43/mi)
        val points = generateLinearGpsPoints(count = 10, stepMeters = 5.0, intervalMs = 2000)
        val pace = PaceCalculator.currentPace(points)
        assertNotNull(pace)
        // Should be a reasonable running pace: between 3:00/mi (180s) and 30:00/mi (1800s)
        assert(pace!! in 180..1800) { "Pace $pace should be between 180 and 1800 sec/mi" }
    }

    @Test
    fun `currentPace returns null with fewer than 2 points`() {
        val singlePoint = listOf(
            GpsPoint(38.9139, -94.3821, Instant.ofEpochMilli(1000), 5f)
        )
        assertNull(PaceCalculator.currentPace(singlePoint))
        assertNull(PaceCalculator.currentPace(emptyList()))
    }

    @Test
    fun `currentPace returns null when distance is negligible`() {
        // Two points at effectively the same location
        val baseTime = Instant.ofEpochMilli(1_000_000)
        val points = listOf(
            GpsPoint(38.9139, -94.3821, baseTime, 5f),
            GpsPoint(38.9139, -94.3821, baseTime.plusMillis(2000), 5f)
        )
        assertNull(PaceCalculator.currentPace(points))
    }

    @Test
    fun `currentPace returns null when pace exceeds 30 minutes per mile`() {
        // Two points very close together (small distance) but far apart in time
        // This simulates GPS drift while standing still
        val baseTime = Instant.ofEpochMilli(1_000_000)
        // ~10 meters apart but 600 seconds (10 min) gap
        // 10m = ~0.0062 miles, pace = 600 / 0.0062 ~= 96,774 sec/mi >> 1800
        val points = listOf(
            GpsPoint(38.91390, -94.38210, baseTime, 5f),
            GpsPoint(38.91399, -94.38210, baseTime.plusMillis(600_000), 5f)
        )
        assertNull(PaceCalculator.currentPace(points))
    }

    @Test
    fun `currentPace respects windowSize parameter`() {
        // Generate 20 points but use windowSize=5
        // Only the last 5 points should be used
        val points = generateLinearGpsPoints(count = 20, stepMeters = 10.0, intervalMs = 2000)
        val paceWindow5 = PaceCalculator.currentPace(points, windowSize = 5)
        val paceWindow20 = PaceCalculator.currentPace(points, windowSize = 20)

        // Both should be non-null since there's enough movement
        assertNotNull(paceWindow5)
        assertNotNull(paceWindow20)

        // With a smaller window, the pace uses fewer points (last 5 vs last 20)
        // For linear constant-speed points, both paces should be similar
        // but we mainly verify the function respects the parameter
        // The window5 uses only 4 segments (8s), window20 uses all 19 segments (38s)
        // For uniform linear movement, pace should be approximately equal
        val diff = kotlin.math.abs(paceWindow5!! - paceWindow20!!)
        assert(diff < 100) { "Pace difference $diff should be small for uniform movement" }
    }

    // --- segmentPace tests ---

    @Test
    fun `segmentPace 120 seconds over 0 point 25 miles returns 480`() {
        val startTime = Instant.ofEpochMilli(1_000_000)
        val endTime = startTime.plusMillis(120_000) // 120 seconds
        val distanceMeters = DistanceCalculator.milesToMeters(0.25)
        val pace = PaceCalculator.segmentPace(startTime, endTime, distanceMeters)
        assertEquals(480, pace)
    }

    @Test
    fun `segmentPace returns null for tiny distances`() {
        val startTime = Instant.ofEpochMilli(1_000_000)
        val endTime = startTime.plusMillis(60_000) // 60 seconds
        // Less than 0.01 miles
        val distanceMeters = DistanceCalculator.milesToMeters(0.005)
        val pace = PaceCalculator.segmentPace(startTime, endTime, distanceMeters)
        assertNull(pace)
    }

    // --- Helper function ---

    companion object {
        /**
         * Generate a list of GPS points along a straight line heading north.
         *
         * @param startLat Starting latitude
         * @param startLon Starting longitude
         * @param stepMeters Distance between each point in meters
         * @param count Number of points to generate
         * @param intervalMs Time between each point in milliseconds
         */
        fun generateLinearGpsPoints(
            startLat: Double = 38.9139,
            startLon: Double = -94.3821,
            stepMeters: Double = 5.0,
            count: Int = 10,
            intervalMs: Long = 2000
        ): List<GpsPoint> {
            val baseTime = Instant.ofEpochMilli(1_000_000)
            // One degree of latitude is approximately 111,320 meters
            val latStepDegrees = stepMeters / 111_320.0

            return (0 until count).map { i ->
                GpsPoint(
                    latitude = startLat + (i * latStepDegrees),
                    longitude = startLon,
                    timestamp = baseTime.plusMillis(i * intervalMs),
                    accuracy = 5f
                )
            }
        }
    }
}
