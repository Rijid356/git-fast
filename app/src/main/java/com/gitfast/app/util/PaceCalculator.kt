package com.gitfast.app.util

import com.gitfast.app.data.model.GpsPoint
import java.time.Instant

object PaceCalculator {

    /**
     * Calculate average pace in seconds per mile.
     *
     * @param elapsedSeconds Total active workout time in seconds
     * @param distanceMeters Total distance covered in meters
     * @return Seconds per mile, or null if distance is too small
     */
    fun averagePace(elapsedSeconds: Int, distanceMeters: Double): Int? {
        val miles = DistanceCalculator.metersToMiles(distanceMeters)
        // Don't calculate pace until we've covered at least 0.01 miles (~16 meters)
        // to avoid wildly inflated numbers from GPS noise at the start
        if (miles < 0.01) return null
        return (elapsedSeconds / miles).toInt()
    }

    /**
     * Calculate current pace from a rolling window of recent GPS points.
     *
     * Uses the last [windowSize] points to determine how fast you're
     * moving right now. More points = smoother but less responsive.
     * Fewer points = responsive but jittery.
     *
     * @param recentPoints The last N GPS points (should be [windowSize] or fewer)
     * @param windowSize Number of points to consider (default 10, ~20 seconds of data)
     * @return Seconds per mile, or null if not enough data
     */
    fun currentPace(
        recentPoints: List<GpsPoint>,
        windowSize: Int = 10
    ): Int? {
        // Need at least 2 points to calculate pace
        val window = recentPoints.takeLast(windowSize)
        if (window.size < 2) return null

        val first = window.first()
        val last = window.last()

        val distanceMeters = DistanceCalculator.haversineMeters(
            first.latitude, first.longitude,
            last.latitude, last.longitude
        )
        val miles = DistanceCalculator.metersToMiles(distanceMeters)

        // Need meaningful distance to calculate pace
        if (miles < 0.005) return null

        val timeSeconds = (last.timestamp.toEpochMilli() - first.timestamp.toEpochMilli()) / 1000.0

        // Sanity check: if calculated pace is slower than 30:00 /mi,
        // the person is probably standing still and GPS is drifting.
        // Return null to indicate "no meaningful pace".
        val paceSecondsPerMile = (timeSeconds / miles).toInt()
        return if (paceSecondsPerMile > 1800) null else paceSecondsPerMile
    }

    /**
     * Calculate the pace for a specific segment (e.g., a single lap).
     *
     * @param startTime Start of the segment
     * @param endTime End of the segment
     * @param distanceMeters Distance covered in the segment
     * @return Seconds per mile, or null if distance is too small
     */
    fun segmentPace(
        startTime: Instant,
        endTime: Instant,
        distanceMeters: Double
    ): Int? {
        val miles = DistanceCalculator.metersToMiles(distanceMeters)
        if (miles < 0.01) return null
        val seconds = (endTime.toEpochMilli() - startTime.toEpochMilli()) / 1000.0
        return (seconds / miles).toInt()
    }
}
