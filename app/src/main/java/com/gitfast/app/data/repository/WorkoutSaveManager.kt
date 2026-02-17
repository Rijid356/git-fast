package com.gitfast.app.data.repository

import android.util.Log
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.service.WorkoutSnapshot
import com.gitfast.app.util.StatsCalculator
import com.gitfast.app.util.XpCalculator
import java.util.UUID
import javax.inject.Inject

data class SaveResult(
    val workoutId: String,
    val xpEarned: Int,
)

class WorkoutSaveManager @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val characterRepository: CharacterRepository,
    private val workoutRepository: WorkoutRepository,
) {

    suspend fun saveCompletedWorkout(snapshot: WorkoutSnapshot): SaveResult? {
        return try {
            val phases = snapshot.phases.map { phaseData ->
                WorkoutPhaseEntity(
                    id = UUID.randomUUID().toString(),
                    workoutId = snapshot.workoutId,
                    type = phaseData.type,
                    startTime = phaseData.startTime.toEpochMilli(),
                    endTime = phaseData.endTime.toEpochMilli(),
                    distanceMeters = phaseData.distanceMeters,
                    steps = phaseData.steps
                )
            }

            // Build lap entities, linking each to its parent phase
            val lapsPhaseEntity = phases.find { it.type == PhaseType.LAPS }
            val lapEntities = if (lapsPhaseEntity != null) {
                val lapsPhaseData = snapshot.phases.find { it.type == PhaseType.LAPS }
                lapsPhaseData?.laps?.map { lapData ->
                    LapEntity(
                        id = UUID.randomUUID().toString(),
                        phaseId = lapsPhaseEntity.id,
                        lapNumber = lapData.lapNumber,
                        startTime = lapData.startTime.toEpochMilli(),
                        endTime = lapData.endTime.toEpochMilli(),
                        distanceMeters = lapData.distanceMeters,
                        steps = lapData.steps
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }

            workoutDao.saveWorkoutTransaction(
                workout = buildWorkoutEntity(snapshot),
                phases = phases,
                laps = lapEntities,
                gpsPoints = buildGpsPointEntities(snapshot)
            )

            // Calculate and award XP
            val xpResult = XpCalculator.calculateXp(snapshot)
            val xpAwarded = characterRepository.awardXp(
                workoutId = snapshot.workoutId,
                xpAmount = xpResult.totalXp,
                reason = xpResult.breakdown.joinToString("; "),
            )

            recalculateStats()

            Log.d("WorkoutSaveManager", "Saved workout ${snapshot.workoutId}, awarded $xpAwarded XP")
            SaveResult(workoutId = snapshot.workoutId, xpEarned = xpAwarded)
        } catch (e: Exception) {
            Log.e("WorkoutSaveManager", "Failed to save workout", e)
            null
        }
    }

    private fun buildWorkoutEntity(snapshot: WorkoutSnapshot): WorkoutEntity {
        return WorkoutEntity(
            id = snapshot.workoutId,
            startTime = snapshot.startTime.toEpochMilli(),
            endTime = snapshot.endTime.toEpochMilli(),
            totalSteps = 0,
            distanceMeters = snapshot.totalDistanceMeters,
            status = WorkoutStatus.COMPLETED,
            activityType = snapshot.activityType,
            dogName = null,
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = null
        )
    }

    private fun buildGpsPointEntities(snapshot: WorkoutSnapshot): List<GpsPointEntity> {
        return snapshot.gpsPoints.mapIndexed { index, point ->
            GpsPointEntity(
                workoutId = snapshot.workoutId,
                latitude = point.latitude,
                longitude = point.longitude,
                timestamp = point.timestamp.toEpochMilli(),
                accuracy = point.accuracy,
                sortIndex = index
            )
        }
    }

    private suspend fun recalculateStats() {
        try {
            val allWorkouts = workoutRepository.getAllCompletedWorkoutsOnce()
            val recentRuns = workoutRepository.getRecentCompletedRuns(20)
            val stats = StatsCalculator.calculateAll(allWorkouts, recentRuns)
            characterRepository.updateStats(stats)
        } catch (e: Exception) {
            Log.e("WorkoutSaveManager", "Failed to recalculate stats", e)
        }
    }

    suspend fun updateDogWalkMetadata(
        workoutId: String,
        dogName: String?,
        routeTag: String?,
        weatherCondition: WeatherCondition?,
        weatherTemp: WeatherTemp?,
        energyLevel: EnergyLevel?,
        notes: String?
    ) {
        val existing = workoutDao.getWorkoutById(workoutId) ?: return
        workoutDao.updateWorkout(
            existing.copy(
                dogName = dogName,
                routeTag = routeTag,
                weatherCondition = weatherCondition,
                weatherTemp = weatherTemp,
                energyLevel = energyLevel,
                notes = notes
            )
        )
    }
}
