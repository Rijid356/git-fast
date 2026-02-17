package com.gitfast.app.data.local.mappers

import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.Lap
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutPhase
import java.time.Instant

fun WorkoutEntity.toDomain(
    phases: List<WorkoutPhase>,
    gpsPoints: List<GpsPoint>
) = Workout(
    id = id,
    startTime = Instant.ofEpochMilli(startTime),
    endTime = endTime?.let { Instant.ofEpochMilli(it) },
    totalSteps = totalSteps,
    distanceMeters = distanceMeters,
    status = status,
    phases = phases,
    gpsPoints = gpsPoints
)

fun Workout.toEntity() = WorkoutEntity(
    id = id,
    startTime = startTime.toEpochMilli(),
    endTime = endTime?.toEpochMilli(),
    totalSteps = totalSteps,
    distanceMeters = distanceMeters,
    status = status
)

fun WorkoutPhaseEntity.toDomain(laps: List<Lap>) = WorkoutPhase(
    id = id,
    type = type,
    startTime = Instant.ofEpochMilli(startTime),
    endTime = endTime?.let { Instant.ofEpochMilli(it) },
    distanceMeters = distanceMeters,
    steps = steps,
    laps = laps
)

fun WorkoutPhase.toEntity(workoutId: String) = WorkoutPhaseEntity(
    id = id,
    workoutId = workoutId,
    type = type,
    startTime = startTime.toEpochMilli(),
    endTime = endTime?.toEpochMilli(),
    distanceMeters = distanceMeters,
    steps = steps
)

fun LapEntity.toDomain() = Lap(
    id = id,
    lapNumber = lapNumber,
    startTime = Instant.ofEpochMilli(startTime),
    endTime = endTime?.let { Instant.ofEpochMilli(it) },
    distanceMeters = distanceMeters,
    steps = steps
)

fun Lap.toEntity(phaseId: String) = LapEntity(
    id = id,
    phaseId = phaseId,
    lapNumber = lapNumber,
    startTime = startTime.toEpochMilli(),
    endTime = endTime?.toEpochMilli(),
    distanceMeters = distanceMeters,
    steps = steps
)

fun GpsPointEntity.toDomain() = GpsPoint(
    latitude = latitude,
    longitude = longitude,
    timestamp = Instant.ofEpochMilli(timestamp),
    accuracy = accuracy
)

fun GpsPoint.toEntity(workoutId: String, sortIndex: Int) = GpsPointEntity(
    workoutId = workoutId,
    latitude = latitude,
    longitude = longitude,
    timestamp = timestamp.toEpochMilli(),
    accuracy = accuracy,
    sortIndex = sortIndex
)
