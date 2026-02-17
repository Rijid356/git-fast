package com.gitfast.app.data.model

import java.time.Instant

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant,
    val accuracy: Float
)
