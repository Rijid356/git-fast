package com.gitfast.app.data.repository

import com.gitfast.app.data.healthconnect.HealthConnectManager
import com.gitfast.app.data.local.BodyCompDao
import com.gitfast.app.data.local.entity.BodyCompEntry
import com.gitfast.app.data.local.mappers.toDomain
import com.gitfast.app.data.model.BodyCompReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyCompRepository @Inject constructor(
    private val bodyCompDao: BodyCompDao,
    private val healthConnectManager: HealthConnectManager,
) {
    suspend fun syncFromHealthConnect() {
        if (!healthConnectManager.hasPermissions()) return

        val end = Instant.now()
        val start = end.minus(90, ChronoUnit.DAYS)

        val weightRecords = healthConnectManager.readWeightRecords(start, end)
        val bodyFatRecords = healthConnectManager.readBodyFatRecords(start, end)
        val leanMassRecords = healthConnectManager.readLeanBodyMassRecords(start, end)
        val boneMassRecords = healthConnectManager.readBoneMassRecords(start, end)
        val bmrRecords = healthConnectManager.readBmrRecords(start, end)
        val heightRecords = healthConnectManager.readHeightRecords(start, end)

        // Index supplementary records by closest timestamp for merging
        val latestHeight = heightRecords.maxByOrNull { it.time.toEpochMilli() }

        val entries = weightRecords.map { weight ->
            val ts = weight.time.toEpochMilli()
            val bodyFat = bodyFatRecords.minByOrNull {
                kotlin.math.abs(it.time.toEpochMilli() - ts)
            }?.takeIf { kotlin.math.abs(it.time.toEpochMilli() - ts) < 3_600_000 }

            val leanMass = leanMassRecords.minByOrNull {
                kotlin.math.abs(it.time.toEpochMilli() - ts)
            }?.takeIf { kotlin.math.abs(it.time.toEpochMilli() - ts) < 3_600_000 }

            val boneMass = boneMassRecords.minByOrNull {
                kotlin.math.abs(it.time.toEpochMilli() - ts)
            }?.takeIf { kotlin.math.abs(it.time.toEpochMilli() - ts) < 3_600_000 }

            val bmr = bmrRecords.minByOrNull {
                kotlin.math.abs(it.time.toEpochMilli() - ts)
            }?.takeIf { kotlin.math.abs(it.time.toEpochMilli() - ts) < 3_600_000 }

            BodyCompEntry(
                id = weight.metadata.id,
                timestamp = ts,
                weightKg = weight.weight.inKilograms,
                bodyFatPercent = bodyFat?.percentage?.value,
                leanBodyMassKg = leanMass?.mass?.inKilograms,
                boneMassKg = boneMass?.mass?.inKilograms,
                bmrKcalPerDay = bmr?.basalMetabolicRate?.inKilocaloriesPerDay,
                heightMeters = latestHeight?.height?.inMeters,
                source = "health_connect",
            )
        }

        if (entries.isNotEmpty()) {
            bodyCompDao.insertAll(entries)
        }
    }

    fun getLatestReading(): Flow<BodyCompReading?> {
        return bodyCompDao.getLatest().map { it?.toDomain() }
    }

    fun getAllReadings(): Flow<List<BodyCompReading>> {
        return bodyCompDao.getAll().map { entries -> entries.map { it.toDomain() } }
    }

    fun getReadingsInRange(start: Instant, end: Instant): Flow<List<BodyCompReading>> {
        return bodyCompDao.getInRange(start.toEpochMilli(), end.toEpochMilli())
            .map { entries -> entries.map { it.toDomain() } }
    }
}
