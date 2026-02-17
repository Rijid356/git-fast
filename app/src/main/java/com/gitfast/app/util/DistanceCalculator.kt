package com.gitfast.app.util

import com.gitfast.app.data.model.GpsPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Calculate distance in meters between two GPS coordinates.
     *
     * @param lat1 Latitude of point 1 in degrees
     * @param lon1 Longitude of point 1 in degrees
     * @param lat2 Latitude of point 2 in degrees
     * @param lon2 Longitude of point 2 in degrees
     * @return Distance in meters
     */
    fun haversineMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) +
                cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculate total distance across a list of GPS points.
     * Sums the distance between each consecutive pair.
     */
    fun totalDistanceMeters(points: List<GpsPoint>): Double {
        if (points.size < 2) return 0.0

        return points.zipWithNext().sumOf { (a, b) ->
            haversineMeters(a.latitude, a.longitude, b.latitude, b.longitude)
        }
    }

    /**
     * Convert meters to miles.
     */
    fun metersToMiles(meters: Double): Double = meters * 0.000621371

    /**
     * Convert miles to meters.
     */
    fun milesToMeters(miles: Double): Double = miles / 0.000621371
}
