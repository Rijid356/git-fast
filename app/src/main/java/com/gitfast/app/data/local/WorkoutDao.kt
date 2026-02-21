package com.gitfast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // --- Inserts ---

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Insert
    suspend fun insertPhase(phase: WorkoutPhaseEntity)

    @Insert
    suspend fun insertLap(lap: LapEntity)

    @Insert
    suspend fun insertGpsPoint(point: GpsPointEntity)

    @Insert
    suspend fun insertGpsPoints(points: List<GpsPointEntity>)

    // --- Updates ---

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updatePhase(phase: WorkoutPhaseEntity)

    @Update
    suspend fun updateLap(lap: LapEntity)

    // --- Queries: Single workout with related data ---

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: String): WorkoutEntity?

    @Query("SELECT * FROM workout_phases WHERE workoutId = :workoutId ORDER BY startTime ASC")
    suspend fun getPhasesForWorkout(workoutId: String): List<WorkoutPhaseEntity>

    @Query("SELECT * FROM laps WHERE phaseId = :phaseId ORDER BY lapNumber ASC")
    suspend fun getLapsForPhase(phaseId: String): List<LapEntity>

    @Query("SELECT * FROM gps_points WHERE workoutId = :workoutId ORDER BY sortIndex ASC")
    suspend fun getGpsPointsForWorkout(workoutId: String): List<GpsPointEntity>

    // --- Queries: Workout history ---

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    fun getAllCompletedWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    suspend fun getAllCompletedWorkoutsOnce(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' AND activityType = 'RUN' ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentCompletedRuns(limit: Int): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' AND activityType = 'RUN' ORDER BY startTime DESC")
    suspend fun getAllCompletedRunsOnce(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' AND activityType = :activityType ORDER BY startTime DESC")
    fun getCompletedWorkoutsByType(activityType: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' AND activityType = 'DOG_WALK' AND routeTag = :routeTag ORDER BY startTime DESC")
    fun getDogWalksByRoute(routeTag: String): Flow<List<WorkoutEntity>>

    @Query("SELECT COUNT(*) FROM workouts WHERE status = 'COMPLETED'")
    suspend fun getCompletedWorkoutCount(): Int

    @Query("SELECT COUNT(*) FROM laps")
    suspend fun getTotalLapCount(): Int

    @Query("SELECT COUNT(*) FROM workouts WHERE status = 'COMPLETED' AND activityType = 'DOG_WALK'")
    suspend fun getCompletedDogWalkCount(): Int

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' AND activityType = 'DOG_WALK' ORDER BY startTime DESC")
    suspend fun getCompletedDogWalksOnce(): List<WorkoutEntity>

    @Query("SELECT COALESCE(SUM(distanceMeters), 0.0) FROM workouts WHERE status = 'COMPLETED' AND activityType = 'DOG_WALK'")
    suspend fun getTotalDogWalkDistanceMeters(): Double

    @Query("SELECT COALESCE(SUM(distanceMeters), 0.0) FROM workouts WHERE status = 'COMPLETED'")
    suspend fun getTotalDistanceMeters(): Double

    @Query("SELECT COALESCE(SUM(endTime - startTime), 0) FROM workouts WHERE status = 'COMPLETED' AND endTime IS NOT NULL")
    suspend fun getTotalDurationMillis(): Long

    @Query("""
        SELECT DISTINCT w.* FROM workouts w
        INNER JOIN workout_phases p ON p.workoutId = w.id AND p.type = 'LAPS'
        INNER JOIN laps l ON l.phaseId = p.id
        WHERE w.status = 'COMPLETED' AND w.activityType = 'RUN'
        ORDER BY w.startTime DESC
        LIMIT :limit
    """)
    suspend fun getRecentWorkoutsWithLaps(limit: Int): List<WorkoutEntity>

    // --- Queries: Active workout recovery ---

    @Query("SELECT * FROM workouts WHERE status IN ('ACTIVE', 'PAUSED') LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    // --- Route Tags ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteTag(tag: RouteTagEntity)

    @Query("SELECT * FROM route_tags ORDER BY lastUsed DESC")
    suspend fun getAllRouteTags(): List<RouteTagEntity>

    @Query("UPDATE route_tags SET lastUsed = :timestamp WHERE name = :name")
    suspend fun updateRouteTagLastUsed(name: String, timestamp: Long)

    // --- Transactions ---

    @Transaction
    suspend fun saveWorkoutTransaction(
        workout: WorkoutEntity,
        phases: List<WorkoutPhaseEntity>,
        laps: List<LapEntity>,
        gpsPoints: List<GpsPointEntity>
    ) {
        val existing = getWorkoutById(workout.id)
        if (existing != null) {
            updateWorkout(workout)
        } else {
            insertWorkout(workout)
        }
        phases.forEach { insertPhase(it) }
        laps.forEach { insertLap(it) }
        if (gpsPoints.isNotEmpty()) {
            insertGpsPoints(gpsPoints)
        }
    }

    // --- Deletes ---

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: String)
}
