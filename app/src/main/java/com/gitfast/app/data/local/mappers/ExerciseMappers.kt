package com.gitfast.app.data.local.mappers

import com.gitfast.app.data.local.entity.ExerciseSessionEntity
import com.gitfast.app.data.local.entity.ExerciseSetEntity
import com.gitfast.app.data.model.ExerciseSession
import com.gitfast.app.data.model.ExerciseSet
import java.time.Instant

fun ExerciseSessionEntity.toDomain(sets: List<ExerciseSet> = emptyList()): ExerciseSession {
    return ExerciseSession(
        id = id,
        startTime = Instant.ofEpochMilli(startTime),
        endTime = endTime?.let { Instant.ofEpochMilli(it) },
        sets = sets,
        notes = notes,
        xpAwarded = xpAwarded,
    )
}

fun ExerciseSession.toEntity(): ExerciseSessionEntity {
    return ExerciseSessionEntity(
        id = id,
        startTime = startTime.toEpochMilli(),
        endTime = endTime?.toEpochMilli(),
        notes = notes,
        xpAwarded = xpAwarded,
    )
}

fun ExerciseSetEntity.toDomain(): ExerciseSet {
    return ExerciseSet(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        reps = reps,
        weightLbs = weightLbs,
        durationSeconds = durationSeconds,
        isWarmup = isWarmup,
        completedAt = Instant.ofEpochMilli(completedAt),
    )
}

fun ExerciseSet.toEntity(): ExerciseSetEntity {
    return ExerciseSetEntity(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        reps = reps,
        weightLbs = weightLbs,
        durationSeconds = durationSeconds,
        isWarmup = isWarmup,
        completedAt = completedAt.toEpochMilli(),
    )
}
