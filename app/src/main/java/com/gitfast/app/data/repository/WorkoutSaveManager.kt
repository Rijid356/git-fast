package com.gitfast.app.data.repository

import android.util.Log
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.service.WorkoutSnapshot
import java.util.UUID
import javax.inject.Inject

class WorkoutSaveManager @Inject constructor(
    private val workoutDao: WorkoutDao
) {

    suspend fun saveCompletedWorkout(snapshot: WorkoutSnapshot): String? {
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

            Log.d("WorkoutSaveManager", "Saved workout ${snapshot.workoutId}")
            snapshot.workoutId
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
            activityType = ActivityType.RUN,
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
}
