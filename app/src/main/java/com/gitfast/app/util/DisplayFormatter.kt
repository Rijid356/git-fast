package com.gitfast.app.util

import com.gitfast.app.data.model.DistanceUnit

fun formatDistance(meters: Double): String {
    val miles = DistanceCalculator.metersToMiles(meters)
    return "%.2f mi".format(miles)
}

fun formatDistance(meters: Double, unit: DistanceUnit): String {
    return when (unit) {
        DistanceUnit.MILES -> {
            val miles = DistanceCalculator.metersToMiles(meters)
            "%.2f mi".format(miles)
        }
        DistanceUnit.KILOMETERS -> {
            val km = DistanceCalculator.metersToKm(meters)
            "%.2f km".format(km)
        }
    }
}
