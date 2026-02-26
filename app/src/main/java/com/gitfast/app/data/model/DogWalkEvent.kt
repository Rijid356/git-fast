package com.gitfast.app.data.model

import java.time.Instant

data class DogWalkEvent(
    val id: String,
    val workoutId: String,
    val eventType: DogWalkEventType,
    val timestamp: Instant,
    val latitude: Double?,
    val longitude: Double?,
)
