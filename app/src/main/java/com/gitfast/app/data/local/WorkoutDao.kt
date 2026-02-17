package com.gitfast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
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

    @Query("SELECT COUNT(*) FROM workouts WHERE status = 'COMPLETED'")
    suspend fun getCompletedWorkoutCount(): Int

    // --- Queries: Active workout recovery ---

    @Query("SELECT * FROM workouts WHERE status IN ('ACTIVE', 'PAUSED') LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    // --- Deletes ---

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: String)
}
