package com.gitfast.app.analysis

import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.util.DistanceCalculator

/**
 * Automatically detects which saved route the user is walking based on their
 * GPS trajectory. Uses a 3-phase filter: start proximity, trajectory matching,
 * and confidence threshold.
 */
object RouteAutoDetector {

    /** A saved route's reference GPS points for comparison. */
    data class RouteCandidate(
        val routeTag: String,
        val referencePoints: List<GpsPoint>,
    )

    /** Result of route detection. */
    data class DetectionResult(
        val routeTag: String?,
        val avgDeviationMeters: Double,
    )

    /** Minimum GPS points needed before attempting detection. */
    const val MIN_POINTS_FOR_DETECTION = 8

    /** Maximum distance (meters) between start points for a route to be considered. */
    const val START_PROXIMITY_METERS = 150.0

    /** Maximum average deviation (meters) for a route to be considered a match. */
    const val MAX_DEVIATION_METERS = 75.0

    /**
     * Detect which route the user is most likely walking.
     *
     * @param currentPoints The GPS points collected so far in the current walk.
     * @param candidates Reference points for each saved route tag.
     * @return A [DetectionResult] with the matched route tag (or null if no match).
     */
    fun detect(
        currentPoints: List<GpsPoint>,
        candidates: List<RouteCandidate>,
    ): DetectionResult {
        if (currentPoints.size < MIN_POINTS_FOR_DETECTION || candidates.isEmpty()) {
            return DetectionResult(routeTag = null, avgDeviationMeters = Double.MAX_VALUE)
        }

        val currentStart = currentPoints.first()

        // Phase 1: Filter by start proximity
        val nearCandidates = candidates.filter { candidate ->
            if (candidate.referencePoints.isEmpty()) return@filter false
            val refStart = candidate.referencePoints.first()
            val startDist = DistanceCalculator.haversineMeters(
                currentStart.latitude, currentStart.longitude,
                refStart.latitude, refStart.longitude,
            )
            startDist <= START_PROXIMITY_METERS
        }

        if (nearCandidates.isEmpty()) {
            return DetectionResult(routeTag = null, avgDeviationMeters = Double.MAX_VALUE)
        }

        // Phase 2: Trajectory matching — average nearest-point distance
        var bestTag: String? = null
        var bestDeviation = Double.MAX_VALUE

        for (candidate in nearCandidates) {
            val deviation = averageNearestPointDistance(currentPoints, candidate.referencePoints)
            if (deviation < bestDeviation) {
                bestDeviation = deviation
                bestTag = candidate.routeTag
            }
        }

        // Phase 3: Confidence threshold
        if (bestDeviation > MAX_DEVIATION_METERS) {
            return DetectionResult(routeTag = null, avgDeviationMeters = bestDeviation)
        }

        return DetectionResult(routeTag = bestTag, avgDeviationMeters = bestDeviation)
    }

    /**
     * For each point in [current], find the nearest point in [reference] and
     * return the average of those distances. This is tolerant of GPS drift
     * and slight path variations (e.g., walking on different sides of the street).
     */
    internal fun averageNearestPointDistance(
        current: List<GpsPoint>,
        reference: List<GpsPoint>,
    ): Double {
        if (current.isEmpty() || reference.isEmpty()) return Double.MAX_VALUE

        var totalDistance = 0.0
        for (cp in current) {
            var minDist = Double.MAX_VALUE
            for (rp in reference) {
                val dist = DistanceCalculator.haversineMeters(
                    cp.latitude, cp.longitude,
                    rp.latitude, rp.longitude,
                )
                if (dist < minDist) minDist = dist
            }
            totalDistance += minDist
        }
        return totalDistance / current.size
    }
}
