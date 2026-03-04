package com.gitfast.app.analysis

/**
 * Result of a route ghost comparison at a given distance.
 *
 * @param deltaSeconds Difference between current elapsed time and historical average.
 *   Positive = behind (slower), negative = ahead (faster). Null if no profiles have data.
 * @param isExhausted True when ALL historical profiles have been exceeded (current walk
 *   is farther than any historical walk for this route).
 */
data class RouteGhostResult(
    val deltaSeconds: Int?,
    val isExhausted: Boolean,
)

/**
 * Calculates the route ghost delta by comparing the current walk's elapsed time
 * against historical distance-time profiles for the same route.
 *
 * Uses distance-time interpolation: at the current cumulative distance, look up
 * each historical profile's elapsed time at that distance, average them, and
 * compute the delta.
 */
object RouteGhostCalculator {

    /**
     * @param currentDistanceMeters Cumulative distance walked so far
     * @param currentElapsedSeconds Active elapsed time of current walk
     * @param profiles Historical distance-time profiles for the same route
     * @return [RouteGhostResult] with delta and exhaustion status
     */
    fun calculateDelta(
        currentDistanceMeters: Double,
        currentElapsedSeconds: Int,
        profiles: List<DistanceTimeProfile>,
    ): RouteGhostResult {
        if (profiles.isEmpty()) {
            return RouteGhostResult(deltaSeconds = null, isExhausted = false)
        }

        val interpolatedTimes = profiles.mapNotNull { it.interpolateTimeAtDistance(currentDistanceMeters) }

        if (interpolatedTimes.isEmpty()) {
            // All profiles exhausted — walk exceeds all historical distances
            return RouteGhostResult(deltaSeconds = null, isExhausted = true)
        }

        val avgTime = interpolatedTimes.average().toInt()
        val delta = currentElapsedSeconds - avgTime

        val isExhausted = interpolatedTimes.size < profiles.size
        return RouteGhostResult(
            deltaSeconds = delta,
            isExhausted = isExhausted,
        )
    }
}
