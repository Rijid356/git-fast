package com.gitfast.app.data.healthconnect

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val client: HealthConnectClient? by lazy {
        try {
            if (isAvailable()) HealthConnectClient.getOrCreate(context) else null
        } catch (e: Exception) {
            null
        }
    }

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(LeanBodyMassRecord::class),
        HealthPermission.getReadPermission(BoneMassRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(HeightRecord::class),
    )

    fun isAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }
    }

    suspend fun hasPermissions(): Boolean {
        val hcClient = client ?: return false
        return try {
            val granted = hcClient.permissionController.getGrantedPermissions()
            requiredPermissions.all { it in granted }
        } catch (e: Exception) {
            false
        }
    }

    fun getPermissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

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
            emptyList()
        }
    }
}
