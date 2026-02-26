package com.gitfast.app.data.local.mappers

import com.gitfast.app.data.local.entity.DogWalkEventEntity
import com.gitfast.app.data.model.DogWalkEvent
import java.time.Instant

fun DogWalkEventEntity.toDomain(): DogWalkEvent = DogWalkEvent(
    id = id,
    workoutId = workoutId,
    eventType = eventType,
    timestamp = Instant.ofEpochMilli(timestamp),
    latitude = latitude,
    longitude = longitude,
)

fun DogWalkEvent.toEntity(): DogWalkEventEntity = DogWalkEventEntity(
    id = id,
    workoutId = workoutId,
    eventType = eventType,
    timestamp = timestamp.toEpochMilli(),
    latitude = latitude,
    longitude = longitude,
)
