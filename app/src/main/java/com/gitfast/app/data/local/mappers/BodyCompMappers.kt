package com.gitfast.app.data.local.mappers

import com.gitfast.app.data.local.entity.BodyCompEntry
import com.gitfast.app.data.model.BodyCompReading
import java.time.Instant

private const val KG_TO_LBS = 2.20462

fun BodyCompEntry.toDomain(): BodyCompReading {
    val wKg = weightKg
    val hM = heightMeters
    return BodyCompReading(
        id = id,
        timestamp = Instant.ofEpochMilli(timestamp),
        weightKg = wKg,
        weightLbs = wKg?.times(KG_TO_LBS),
        bodyFatPercent = bodyFatPercent,
        leanBodyMassKg = leanBodyMassKg,
        leanBodyMassLbs = leanBodyMassKg?.times(KG_TO_LBS),
        boneMassKg = boneMassKg,
        boneMassLbs = boneMassKg?.times(KG_TO_LBS),
        bmrKcalPerDay = bmrKcalPerDay,
        heightMeters = hM,
        bmi = if (wKg != null && hM != null && hM > 0) wKg / (hM * hM) else null,
        source = source,
    )
}

fun BodyCompReading.toEntity(): BodyCompEntry {
    return BodyCompEntry(
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
}
