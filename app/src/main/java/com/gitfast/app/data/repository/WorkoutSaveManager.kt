package com.gitfast.app.data.repository

import android.util.Log
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
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
            val workout = buildWorkoutEntity(snapshot)
            val phase = buildDefaultPhase(snapshot)
            val gpsPoints = buildGpsPointEntities(snapshot)

            workoutDao.saveWorkoutTransaction(
                workout = workout,
                phases = listOf(phase),
                laps = emptyList(),
                gpsPoints = gpsPoints
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

    private fun buildDefaultPhase(snapshot: WorkoutSnapshot): WorkoutPhaseEntity {
        return WorkoutPhaseEntity(
            id = UUID.randomUUID().toString(),
            workoutId = snapshot.workoutId,
            type = PhaseType.WARMUP,
            startTime = snapshot.startTime.toEpochMilli(),
            endTime = snapshot.endTime.toEpochMilli(),
            distanceMeters = snapshot.totalDistanceMeters,
            steps = 0
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
