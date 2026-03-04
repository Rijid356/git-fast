package com.gitfast.app.data.local.mappers

import com.gitfast.app.data.local.entity.SorenessLogEntity
import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.data.model.SorenessLog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun SorenessLogEntity.toDomain(): SorenessLog {
    val intensityMap = if (muscleGroups.contains(":")) {
        // New format: "CHEST:MODERATE,BACK:SEVERE"
        muscleGroups
            .split(",")
            .filter { it.isNotBlank() }
            .associate { entry ->
                val parts = entry.trim().split(":")
                MuscleGroup.valueOf(parts[0]) to SorenessIntensity.valueOf(parts[1])
            }
    } else {
        // Legacy format: "CHEST,BACK" with global intensity column
        val globalIntensity = SorenessIntensity.valueOf(intensity)
        muscleGroups
            .split(",")
            .filter { it.isNotBlank() }
            .associate { MuscleGroup.valueOf(it.trim()) to globalIntensity }
    }

    return SorenessLog(
        id = id,
        date = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate(),
        muscleIntensities = intensityMap,
        notes = notes,
        xpAwarded = xpAwarded,
        createdAt = Instant.ofEpochMilli(createdAt),
    )
}

fun SorenessLog.toEntity(): SorenessLogEntity {
    return SorenessLogEntity(
        id = id,
        date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        muscleGroups = muscleIntensities.entries.joinToString(",") { "${it.key.name}:${it.value.name}" },
        intensity = (maxIntensity ?: SorenessIntensity.MILD).name,
        notes = notes,
        xpAwarded = xpAwarded,
        createdAt = createdAt.toEpochMilli(),
    )
}
