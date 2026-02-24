package com.gitfast.app.data.healthconnect

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BoneMassRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.LeanBodyMassRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    private val context: Context,
) {

    companion object {
        private const val TAG = "HealthConnectManager"

        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(LeanBodyMassRecord::class),
            HealthPermission.getReadPermission(BoneMassRecord::class),
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
            HealthPermission.getReadPermission(HeightRecord::class),
        )
    }

    private val client: HealthConnectClient? by lazy {
        try {
            if (isAvailable()) HealthConnectClient.getOrCreate(context) else null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create HealthConnectClient", e)
            null
        }
    }

    fun isAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            Log.w(TAG, "Error checking Health Connect availability", e)
            false
        }
    }

    suspend fun hasPermissions(): Boolean {
        val hcClient = client ?: return false
        return try {
            val granted = hcClient.permissionController.getGrantedPermissions()
            PERMISSIONS.all { it in granted }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking permissions", e)
            false
        }
    }

    fun getPermissionContract() = PermissionController.createRequestPermissionResultContract()

    suspend fun readWeightRecords(start: Instant, end: Instant): List<WeightRecord> {
        val hcClient = client ?: return emptyList()
        return try {
            hcClient.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                )
            ).records
        } catch (e: Exception) {
            Log.w(TAG, "Error reading weight records", e)
            emptyList()
        }
    }

    suspend fun readBodyFatRecords(start: Instant, end: Instant): List<BodyFatRecord> {
        val hcClient = client ?: return emptyList()
        return try {
            hcClient.readRecords(
                ReadRecordsRequest(
                    recordType = BodyFatRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                )
            ).records
        } catch (e: Exception) {
            Log.w(TAG, "Error reading body fat records", e)
            emptyList()
        }
    }

    suspend fun readLeanBodyMassRecords(start: Instant, end: Instant): List<LeanBodyMassRecord> {
        val hcClient = client ?: return emptyList()
        return try {
            hcClient.readRecords(
                ReadRecordsRequest(
                    recordType = LeanBodyMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                )
            ).records
        } catch (e: Exception) {
            Log.w(TAG, "Error reading lean body mass records", e)
            emptyList()
        }
    }

    suspend fun readBoneMassRecords(start: Instant, end: Instant): List<BoneMassRecord> {
        val hcClient = client ?: return emptyList()
        return try {
            hcClient.readRecords(
                ReadRecordsRequest(
                    recordType = BoneMassRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                )
            ).records
        } catch (e: Exception) {
            Log.w(TAG, "Error reading bone mass records", e)
            emptyList()
        }
    }

    suspend fun readBmrRecords(start: Instant, end: Instant): List<BasalMetabolicRateRecord> {
        val hcClient = client ?: return emptyList()
        return try {
            hcClient.readRecords(
                ReadRecordsRequest(
                    recordType = BasalMetabolicRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                )
            ).records
        } catch (e: Exception) {
            Log.w(TAG, "Error reading BMR records", e)
            emptyList()
        }
    }

    suspend fun readHeightRecords(start: Instant, end: Instant): List<HeightRecord> {
        val hcClient = client ?: return emptyList()
        return try {
            hcClient.readRecords(
                ReadRecordsRequest(
                    recordType = HeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                )
            ).records
        } catch (e: Exception) {
            Log.w(TAG, "Error reading height records", e)
            emptyList()
        }
    }
}
