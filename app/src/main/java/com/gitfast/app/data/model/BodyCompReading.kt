package com.gitfast.app.data.model

import java.time.Instant

data class BodyCompReading(
    val id: String,
    val timestamp: Instant,
    val weightKg: Double?,
    val weightLbs: Double?,
    val bodyFatPercent: Double?,
    val leanBodyMassKg: Double?,
    val leanBodyMassLbs: Double?,
    val boneMassKg: Double?,
    val boneMassLbs: Double?,
    val bmrKcalPerDay: Double?,
    val heightMeters: Double?,
    val bmi: Double?,
    val source: String,
)
