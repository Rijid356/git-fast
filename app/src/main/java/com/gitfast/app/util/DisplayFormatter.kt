package com.gitfast.app.util

fun formatDistance(meters: Double): String {
    val miles = DistanceCalculator.metersToMiles(meters)
    return "%.2f mi".format(miles)
}
