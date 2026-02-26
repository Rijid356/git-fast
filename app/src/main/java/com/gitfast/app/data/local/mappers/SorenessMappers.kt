package com.gitfast.app.data.local.mappers

import com.gitfast.app.data.local.entity.SorenessLogEntity
import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.data.model.SorenessLog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun SorenessLogEntity.toDomain(): SorenessLog {
    return SorenessLog(
        id = id,
        date = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate(),
        muscleGroups = muscleGroups
            .split(",")
            .filter { it.isNotBlank() }
            .map { MuscleGroup.valueOf(it.trim()) }
            .toSet(),
        intensity = SorenessIntensity.valueOf(intensity),
        notes = notes,
        xpAwarded = xpAwarded,
        createdAt = Instant.ofEpochMilli(createdAt),
    )
}

fun SorenessLog.toEntity(): SorenessLogEntity {
    return SorenessLogEntity(
        id = id,
        date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        muscleGroups = muscleGroups.joinToString(",") { it.name },
        intensity = intensity.name,
        notes = notes,
        xpAwarded = xpAwarded,
        createdAt = createdAt.toEpochMilli(),
    )
}
