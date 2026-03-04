package com.gitfast.app.analysis

import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.util.DistanceCalculator
import java.time.Instant

/**
 * A cumulative distance-time profile built from GPS points of a historical walk.
 * Used for route ghost comparison: at any cumulative distance D, interpolate the
 * elapsed time from this historical profile.
 *
 * Both arrays are parallel and monotonically increasing. Index 0 = first GPS point
 * (distance 0.0, elapsed 0).
 */
data class DistanceTimeProfile(
    val distances: DoubleArray,
    val elapsedSeconds: IntArray,
    val totalDistanceMeters: Double,
    val totalSeconds: Int,
) {
    /**
     * Binary search + linear interpolation to find elapsed time at a given distance.
     * Returns null if [distanceMeters] exceeds this profile's total distance.
     */
    fun interpolateTimeAtDistance(distanceMeters: Double): Int? {
        if (distances.isEmpty()) return null
        if (distanceMeters <= 0.0) return 0
        if (distanceMeters >= totalDistanceMeters) return null

        // Binary search for the insertion point
        var lo = 0
        var hi = distances.size - 1
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (distances[mid] < distanceMeters) lo = mid + 1 else hi = mid
        }

        // Exact match
        if (distances[lo] == distanceMeters) return elapsedSeconds[lo]

        // lo is the first index where distances[lo] >= distanceMeters
        if (lo == 0) return 0

        // Linear interpolation between lo-1 and lo
        val d0 = distances[lo - 1]
        val d1 = distances[lo]
        val t0 = elapsedSeconds[lo - 1]
        val t1 = elapsedSeconds[lo]
        val fraction = (distanceMeters - d0) / (d1 - d0)
        return (t0 + fraction * (t1 - t0)).toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DistanceTimeProfile) return false
        return distances.contentEquals(other.distances) &&
            elapsedSeconds.contentEquals(other.elapsedSeconds) &&
            totalDistanceMeters == other.totalDistanceMeters &&
            totalSeconds == other.totalSeconds
    }

    override fun hashCode(): Int {
        var result = distances.contentHashCode()
        result = 31 * result + elapsedSeconds.contentHashCode()
        result = 31 * result + totalDistanceMeters.hashCode()
        result = 31 * result + totalSeconds.hashCode()
        return result
    }

    companion object {
        /**
         * Build a distance-time profile from GPS points and a workout start time.
         * Uses haversine distance between consecutive points for cumulative distance,
         * and point timestamp minus [workoutStartTime] for elapsed time.
         */
        fun fromGpsPoints(points: List<GpsPoint>, workoutStartTime: Instant): DistanceTimeProfile {
            if (points.isEmpty()) {
                return DistanceTimeProfile(
                    distances = doubleArrayOf(),
                    elapsedSeconds = intArrayOf(),
                    totalDistanceMeters = 0.0,
                    totalSeconds = 0,
                )
            }

            val distances = DoubleArray(points.size)
            val elapsed = IntArray(points.size)
            val startMillis = workoutStartTime.toEpochMilli()

            distances[0] = 0.0
            elapsed[0] = ((points[0].timestamp.toEpochMilli() - startMillis) / 1000).toInt()
                .coerceAtLeast(0)

            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val segmentDist = DistanceCalculator.haversineMeters(
                    prev.latitude, prev.longitude,
                    curr.latitude, curr.longitude,
                )
                distances[i] = distances[i - 1] + segmentDist
                elapsed[i] = ((curr.timestamp.toEpochMilli() - startMillis) / 1000).toInt()
                    .coerceAtLeast(elapsed[i - 1])
            }

            return DistanceTimeProfile(
                distances = distances,
                elapsedSeconds = elapsed,
                totalDistanceMeters = distances.last(),
                totalSeconds = elapsed.last(),
            )
        }
    }
}
