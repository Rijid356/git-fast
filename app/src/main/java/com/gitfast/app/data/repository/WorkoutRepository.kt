package com.gitfast.app.data.repository

import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.local.mappers.toDomain
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) {
    fun getCompletedWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllCompletedWorkouts().map { entities ->
            entities.map { entity ->
                val phases = workoutDao.getPhasesForWorkout(entity.id)
                    .map { it.toDomain(emptyList()) }
                entity.toDomain(phases, emptyList())
            }
        }
    }

    fun getCompletedWorkoutsByType(activityType: ActivityType): Flow<List<Workout>> {
        return workoutDao.getCompletedWorkoutsByType(activityType.name).map { entities ->
            entities.map { entity ->
                val phases = workoutDao.getPhasesForWorkout(entity.id)
                    .map { it.toDomain(emptyList()) }
                entity.toDomain(phases, emptyList())
            }
        }
    }

    fun getDogWalksByRoute(routeTag: String): Flow<List<Workout>> {
        return workoutDao.getDogWalksByRoute(routeTag).map { entities ->
            entities.map { entity ->
                val phases = workoutDao.getPhasesForWorkout(entity.id)
                    .map { it.toDomain(emptyList()) }
                entity.toDomain(phases, emptyList())
            }
        }
    }

    suspend fun getDogWalksByRouteOnce(routeTag: String): List<Workout> {
        return workoutDao.getDogWalksByRoute(routeTag).first().map { entity ->
            val phases = workoutDao.getPhasesForWorkout(entity.id).map { it.toDomain(emptyList()) }
            entity.toDomain(phases, emptyList())
        }
    }

    suspend fun getWorkoutWithDetails(workoutId: String): Workout? {
        val entity = workoutDao.getWorkoutById(workoutId) ?: return null
        val gpsPoints = workoutDao.getGpsPointsForWorkout(workoutId)
            .map { it.toDomain() }
        val phases = workoutDao.getPhasesForWorkout(workoutId).map { phase ->
            val laps = workoutDao.getLapsForPhase(phase.id)
                .map { it.toDomain() }
            phase.toDomain(laps)
        }
        return entity.toDomain(phases, gpsPoints)
    }

    suspend fun saveWorkout(
        workout: WorkoutEntity,
        phases: List<WorkoutPhaseEntity>,
        laps: List<LapEntity>,
        gpsPoints: List<GpsPointEntity>
    ) {
        workout.let { workoutDao.insertWorkout(it) }
        phases.forEach { workoutDao.insertPhase(it) }
        laps.forEach { workoutDao.insertLap(it) }
        workoutDao.insertGpsPoints(gpsPoints)
    }

    suspend fun updateWorkout(workout: WorkoutEntity) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun getActiveWorkout(): WorkoutEntity? {
        return workoutDao.getActiveWorkout()
    }

    suspend fun deleteWorkout(workoutId: String) {
        workoutDao.deleteWorkout(workoutId)
    }

    suspend fun getCompletedWorkoutCount(): Int {
        return workoutDao.getCompletedWorkoutCount()
    }

    suspend fun getAllRouteTags(): List<RouteTagEntity> {
        return workoutDao.getAllRouteTags()
    }

    suspend fun saveRouteTag(tag: RouteTagEntity) {
        workoutDao.insertRouteTag(tag)
    }

    suspend fun touchRouteTag(name: String) {
        workoutDao.updateRouteTagLastUsed(name, System.currentTimeMillis())
    }

    suspend fun saveGpsPoints(points: List<GpsPointEntity>) {
        workoutDao.insertGpsPoints(points)
    }

    suspend fun savePhase(phase: WorkoutPhaseEntity) {
        workoutDao.insertPhase(phase)
    }

    suspend fun getAllCompletedWorkoutsOnce(): List<Workout> {
        return workoutDao.getAllCompletedWorkoutsOnce().map { entity ->
            entity.toDomain(emptyList(), emptyList())
        }
    }

    suspend fun getRecentCompletedRuns(limit: Int): List<Workout> {
        return workoutDao.getRecentCompletedRuns(limit).map { entity ->
            entity.toDomain(emptyList(), emptyList())
        }
    }

    suspend fun getTotalLapCount(): Int {
        return workoutDao.getTotalLapCount()
    }

    suspend fun getCompletedDogWalkCount(): Int {
        return workoutDao.getCompletedDogWalkCount()
    }

    suspend fun getCompletedDogWalks(): List<Workout> {
        return workoutDao.getCompletedDogWalksOnce().map { entity ->
            entity.toDomain(emptyList(), emptyList())
        }
    }

    suspend fun getTotalDogWalkDistanceMeters(): Double {
        return workoutDao.getTotalDogWalkDistanceMeters()
    }

    suspend fun getRecentWorkoutsWithLaps(limit: Int): List<Workout> {
        return workoutDao.getRecentWorkoutsWithLaps(limit).map { entity ->
            val phases = workoutDao.getPhasesForWorkout(entity.id).map { phase ->
                val laps = workoutDao.getLapsForPhase(phase.id).map { it.toDomain() }
                phase.toDomain(laps)
            }
            entity.toDomain(phases, emptyList())
        }
    }
}
