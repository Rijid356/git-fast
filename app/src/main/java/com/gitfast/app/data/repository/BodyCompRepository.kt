package com.gitfast.app.data.repository

import android.util.Log
import com.gitfast.app.data.healthconnect.HealthConnectManager
import com.gitfast.app.data.local.BodyCompDao
import com.gitfast.app.data.local.entity.BodyCompEntry
import com.gitfast.app.data.local.mappers.toDomain
import com.gitfast.app.data.model.BodyCompReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyCompRepository @Inject constructor(
    private val bodyCompDao: BodyCompDao,
    private val healthConnectManager: HealthConnectManager,
) {

    companion object {
        private const val TAG = "BodyCompRepository"
        private const val SYNC_DAYS = 90L
    }

    sealed class SyncResult {
        data class Success(val count: Int) : SyncResult()
        data object NoPermissions : SyncResult()
        data object NotAvailable : SyncResult()
        data object NoData : SyncResult()
        data class Error(val message: String) : SyncResult()
    }

    /**
     * Sync body composition data from Health Connect for the last 90 days.
     * Reads all 6 record types, merges by timestamp (nearest within 5 minutes),
     * and upserts locally using Health Connect record IDs.
     */
    suspend fun syncFromHealthConnect(): SyncResult {
        if (!healthConnectManager.isAvailable()) {
            Log.w(TAG, "Health Connect not available")
            return SyncResult.NotAvailable
        }

        if (!healthConnectManager.hasPermissions()) {
            Log.w(TAG, "Health Connect permissions not granted, skipping sync")
            return SyncResult.NoPermissions
        }

        return try {
            val end = Instant.now()
            val start = end.minus(SYNC_DAYS, ChronoUnit.DAYS)

            val weights = healthConnectManager.readWeightRecords(start, end)
            val bodyFats = healthConnectManager.readBodyFatRecords(start, end)
            val leanMasses = healthConnectManager.readLeanBodyMassRecords(start, end)
            val boneMasses = healthConnectManager.readBoneMassRecords(start, end)
            val bmrs = healthConnectManager.readBmrRecords(start, end)
            val heights = healthConnectManager.readHeightRecords(start, end)

            // Build entries keyed by weight record (primary anchor)
            val entries = weights.map { weight ->
                val ts = weight.time
                val windowStart = ts.minus(5, ChronoUnit.MINUTES)
                val windowEnd = ts.plus(5, ChronoUnit.MINUTES)

                val bodyFat = bodyFats.find { it.time in windowStart..windowEnd }
                val leanMass = leanMasses.find { it.time in windowStart..windowEnd }
                val boneMass = boneMasses.find { it.time in windowStart..windowEnd }
                val bmr = bmrs.find { it.time in windowStart..windowEnd }
                val height = heights.find { it.time in windowStart..windowEnd }

                BodyCompEntry(
                    id = weight.metadata.id,
                    timestamp = ts.toEpochMilli(),
                    weightKg = weight.weight.inKilograms,
                    bodyFatPercent = bodyFat?.percentage?.value,
                    leanBodyMassKg = leanMass?.mass?.inKilograms,
                    boneMassKg = boneMass?.mass?.inKilograms,
                    bmrKcalPerDay = bmr?.basalMetabolicRate?.inKilocaloriesPerDay,
                    heightMeters = height?.height?.inMeters,
                    source = "health_connect",
                )
            }

            if (entries.isNotEmpty()) {
                bodyCompDao.insertAll(entries)
                Log.d(TAG, "Synced ${entries.size} body comp entries from Health Connect")
                SyncResult.Success(entries.size)
            } else {
                Log.d(TAG, "No weight records found in Health Connect")
                SyncResult.NoData
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Health Connect", e)
            SyncResult.Error(e.message ?: "Unknown error")
        }
    }

    fun getLatestReading(): Flow<BodyCompReading?> {
        return bodyCompDao.getLatest().map { it?.toDomain() }
    }

    fun getAllReadings(): Flow<List<BodyCompReading>> {
        return bodyCompDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    fun getReadingsInRange(start: Instant, end: Instant): Flow<List<BodyCompReading>> {
        return bodyCompDao.getInRange(start.toEpochMilli(), end.toEpochMilli())
            .map { list -> list.map { it.toDomain() } }
    }

    /**
     * Calculate consecutive days with a weigh-in, counting backwards from today.
     * Today or yesterday count as the start (1-day grace, same as workout streak).
     */
    suspend fun getWeighInStreak(): Int {
        val days = bodyCompDao.getWeighInDays().first()
        if (days.isEmpty()) return 0

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val todayEpochDay = today.toEpochDay()

        // days are epoch millis / 86400000 (epoch days in UTC)
        val weighInDays = days.toSet()

        // Check if there's a weigh-in today or yesterday (grace period)
        val hasToday = todayEpochDay in weighInDays
        val hasYesterday = (todayEpochDay - 1) in weighInDays

        if (!hasToday && !hasYesterday) return 0

        val startDay = if (hasToday) todayEpochDay else todayEpochDay - 1
        var streak = 0
        var checkDay = startDay

        while (checkDay in weighInDays) {
            streak++
            checkDay--
        }

        return streak
    }

    /**
     * Count weigh-ins (days with weight readings) in the last N days.
     */
    suspend fun getWeighInCount(days: Int): Int {
        val end = Instant.now()
        val start = end.minus(days.toLong(), ChronoUnit.DAYS)
        val entries = bodyCompDao.getInRange(start.toEpochMilli(), end.toEpochMilli()).first()
        val zone = ZoneId.systemDefault()
        return entries
            .filter { it.weightKg != null }
            .map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
            .distinct()
            .size
    }
}
