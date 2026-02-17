package com.gitfast.app

import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.util.DistanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class DistanceCalculatorTest {

    @Test
    fun `haversineMeters known distance Statue of Liberty to Empire State Building`() {
        // Statue of Liberty: 40.6892, -74.0445
        // Empire State Building: 40.7484, -73.9856
        // Expected ~8,200m
        val distance = DistanceCalculator.haversineMeters(
            40.6892, -74.0445,
            40.7484, -73.9856
        )
        assertEquals(8200.0, distance, 8200.0 * 0.05) // 5% tolerance
    }

    @Test
    fun `haversineMeters same point returns zero`() {
        val distance = DistanceCalculator.haversineMeters(
            38.9139, -94.3821,
            38.9139, -94.3821
        )
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `haversineMeters antipodal points approximately half Earth circumference`() {
        // North Pole to South Pole (antipodal on a sphere)
        // Half Earth circumference ~20,015 km
        val distance = DistanceCalculator.haversineMeters(
            90.0, 0.0,
            -90.0, 0.0
        )
        val halfCircumference = 20_015_000.0
        assertEquals(halfCircumference, distance, halfCircumference * 0.01) // 1% tolerance
    }

    @Test
    fun `totalDistanceMeters empty list returns zero`() {
        val distance = DistanceCalculator.totalDistanceMeters(emptyList())
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `totalDistanceMeters single point returns zero`() {
        val points = listOf(
            GpsPoint(38.9139, -94.3821, Instant.now(), 5f)
        )
        val distance = DistanceCalculator.totalDistanceMeters(points)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun `totalDistanceMeters two points matches haversineMeters`() {
        val lat1 = 38.9139
        val lon1 = -94.3821
        val lat2 = 38.9149
        val lon2 = -94.3811

        val expected = DistanceCalculator.haversineMeters(lat1, lon1, lat2, lon2)

        val points = listOf(
            GpsPoint(lat1, lon1, Instant.now(), 5f),
            GpsPoint(lat2, lon2, Instant.now(), 5f)
        )
        val distance = DistanceCalculator.totalDistanceMeters(points)
        assertEquals(expected, distance, 0.001)
    }

    @Test
    fun `totalDistanceMeters three points sums both segments`() {
        val lat1 = 38.9139
        val lon1 = -94.3821
        val lat2 = 38.9149
        val lon2 = -94.3811
        val lat3 = 38.9159
        val lon3 = -94.3801

        val seg1 = DistanceCalculator.haversineMeters(lat1, lon1, lat2, lon2)
        val seg2 = DistanceCalculator.haversineMeters(lat2, lon2, lat3, lon3)
        val expected = seg1 + seg2

        val points = listOf(
            GpsPoint(lat1, lon1, Instant.now(), 5f),
            GpsPoint(lat2, lon2, Instant.now(), 5f),
            GpsPoint(lat3, lon3, Instant.now(), 5f)
        )
        val distance = DistanceCalculator.totalDistanceMeters(points)
        assertEquals(expected, distance, 0.001)
    }

    @Test
    fun `metersToMiles 1609 point 34 meters is approximately 1 mile`() {
        val miles = DistanceCalculator.metersToMiles(1609.34)
        assertEquals(1.0, miles, 0.01)
    }

    @Test
    fun `metersToMiles 0 meters returns 0 miles`() {
        val miles = DistanceCalculator.metersToMiles(0.0)
        assertEquals(0.0, miles, 0.001)
    }
}
