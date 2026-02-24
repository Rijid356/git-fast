package com.gitfast.app.data.local.mappers

import com.gitfast.app.data.local.entity.BodyCompEntry
import com.gitfast.app.data.model.BodyCompReading
import java.time.Instant

private const val KG_TO_LBS = 2.20462

fun BodyCompEntry.toDomain() = BodyCompReading(
    id = id,
    timestamp = Instant.ofEpochMilli(timestamp),
    weightKg = weightKg,
    weightLbs = weightKg?.let { it * KG_TO_LBS },
    bodyFatPercent = bodyFatPercent,
    leanBodyMassKg = leanBodyMassKg,
    leanBodyMassLbs = leanBodyMassKg?.let { it * KG_TO_LBS },
    boneMassKg = boneMassKg,
    boneMassLbs = boneMassKg?.let { it * KG_TO_LBS },
    bmrKcalPerDay = bmrKcalPerDay,
    heightMeters = heightMeters,
    bmi = if (weightKg != null && heightMeters != null && heightMeters > 0) {
        weightKg / (heightMeters * heightMeters)
    } else null,
    source = source,
)

fun BodyCompReading.toEntity() = BodyCompEntry(
    id = id,
    timestamp = timestamp.toEpochMilli(),
    weightKg = weightKg,
    bodyFatPercent = bodyFatPercent,
    leanBodyMassKg = leanBodyMassKg,
    boneMassKg = boneMassKg,
    bmrKcalPerDay = bmrKcalPerDay,
    heightMeters = heightMeters,
    source = source,
)
