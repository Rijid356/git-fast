package com.gitfast.app.data.repository

import com.gitfast.app.data.local.SorenessDao
import com.gitfast.app.data.local.mappers.toDomain
import com.gitfast.app.data.local.mappers.toEntity
import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.data.model.SorenessLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class SorenessRepository @Inject constructor(
    private val sorenessDao: SorenessDao,
) {

    fun observeTodayLog(): Flow<SorenessLog?> {
        return sorenessDao.observeByDate(todayEpoch()).map { it?.toDomain() }
    }

    suspend fun getTodayLog(): SorenessLog? {
        return sorenessDao.getByDate(todayEpoch())?.toDomain()
    }

    suspend fun logSoreness(
        muscleGroups: Set<MuscleGroup>,
        intensity: SorenessIntensity,
        notes: String?,
        xpAwarded: Int,
    ): SorenessLog {
        val existing = sorenessDao.getByDate(todayEpoch())
        val log = if (existing != null) {
            existing.toDomain().copy(
                muscleGroups = muscleGroups,
                intensity = intensity,
                notes = notes,
                xpAwarded = xpAwarded,
            )
        } else {
            SorenessLog(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now(),
                muscleGroups = muscleGroups,
                intensity = intensity,
                notes = notes,
                xpAwarded = xpAwarded,
            )
        }
        if (existing != null) {
            sorenessDao.update(log.toEntity())
        } else {
            sorenessDao.insert(log.toEntity())
        }
        return log
    }

    suspend fun getTotalCount(): Int = sorenessDao.getTotalCount()

    suspend fun getLast30DaysLogs(): List<SorenessLog> {
        val thirtyDaysAgo = LocalDate.now().minusDays(30)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return sorenessDao.getLogsSince(thirtyDaysAgo).map { it.toDomain() }
    }

    private fun todayEpoch(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
