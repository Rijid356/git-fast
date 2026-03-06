package com.gitfast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gitfast.app.data.local.entity.DogWalkEventEntity
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WalkPhotoEntity
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
    suspend fun insertPhases(phases: List<WorkoutPhaseEntity>)

    @Insert
    suspend fun insertLaps(laps: List<LapEntity>)

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

    @Query("SELECT * FROM workout_phases WHERE workoutId IN (:workoutIds) ORDER BY startTime ASC")
    suspend fun getPhasesForWorkouts(workoutIds: List<String>): List<WorkoutPhaseEntity>

    @Query("SELECT * FROM laps WHERE phaseId = :phaseId ORDER BY lapNumber ASC")
    suspend fun getLapsForPhase(phaseId: String): List<LapEntity>

    @Query("SELECT * FROM laps WHERE phaseId IN (:phaseIds) ORDER BY lapNumber ASC")
    suspend fun getLapsForPhases(phaseIds: List<String>): List<LapEntity>

    @Query("SELECT * FROM gps_points WHERE workoutId = :workoutId ORDER BY sortIndex ASC")
    suspend fun getGpsPointsForWorkout(workoutId: String): List<GpsPointEntity>

    @Query("SELECT * FROM gps_points WHERE workoutId = :workoutId AND sortIndex <= :maxIndex ORDER BY sortIndex ASC")
    suspend fun getFirstGpsPointsForWorkout(workoutId: String, maxIndex: Int): List<GpsPointEntity>

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

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' AND activityType IN ('DOG_WALK', 'DOG_RUN') ORDER BY startTime DESC")
    fun getCompletedDogActivityWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' AND activityType IN ('DOG_WALK', 'DOG_RUN') AND routeTag = :routeTag ORDER BY startTime DESC")
    fun getDogWalksByRoute(routeTag: String): Flow<List<WorkoutEntity>>

    @Query("SELECT COUNT(*) FROM workouts WHERE status = 'COMPLETED'")
    suspend fun getCompletedWorkoutCount(): Int

    @Query("SELECT COUNT(*) FROM laps")
    suspend fun getTotalLapCount(): Int

    @Query("SELECT COUNT(*) FROM workouts WHERE status = 'COMPLETED' AND activityType IN ('DOG_WALK', 'DOG_RUN')")
    suspend fun getCompletedDogWalkCount(): Int

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' AND activityType IN ('DOG_WALK', 'DOG_RUN') ORDER BY startTime DESC")
    suspend fun getCompletedDogWalksOnce(): List<WorkoutEntity>

    @Query("SELECT COALESCE(SUM(distanceMeters), 0.0) FROM workouts WHERE status = 'COMPLETED' AND activityType IN ('DOG_WALK', 'DOG_RUN')")
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

    // --- Queries: Daily/weekly activity goals ---

    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' AND startTime >= :startMillis AND startTime < :endMillis ORDER BY startTime DESC")
    fun getCompletedWorkoutsBetween(startMillis: Long, endMillis: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT COALESCE(SUM(endTime - startTime), 0) FROM workouts WHERE status = 'COMPLETED' AND endTime IS NOT NULL AND startTime >= :startMillis AND startTime < :endMillis")
    fun getActiveMillisBetween(startMillis: Long, endMillis: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(distanceMeters), 0.0) FROM workouts WHERE status = 'COMPLETED' AND startTime >= :startMillis AND startTime < :endMillis")
    fun getDistanceMetersBetween(startMillis: Long, endMillis: Long): Flow<Double>

    @Query("SELECT COUNT(DISTINCT date(startTime / 1000, 'unixepoch', 'localtime')) FROM workouts WHERE status = 'COMPLETED' AND startTime >= :startMillis AND startTime < :endMillis")
    fun getActiveDayCountBetween(startMillis: Long, endMillis: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM workouts WHERE status = 'COMPLETED' AND startTime >= :startMillis AND startTime < :endMillis")
    fun getCompletedWorkoutCountBetween(startMillis: Long, endMillis: Long): Flow<Int>

    // --- Queries: Active workout recovery ---

    @Query("SELECT * FROM workouts WHERE status IN ('ACTIVE', 'PAUSED') LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    // --- Route Tags ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteTag(tag: RouteTagEntity)

    @Query("SELECT * FROM route_tags ORDER BY lastUsed DESC")
    suspend fun getAllRouteTags(): List<RouteTagEntity>

    @Query("SELECT DISTINCT routeTag FROM workouts WHERE status = 'COMPLETED' AND routeTag IS NOT NULL AND routeTag != '' ORDER BY routeTag ASC")
    suspend fun getDistinctRouteTags(): List<String>

    @Query("UPDATE route_tags SET lastUsed = :timestamp WHERE name = :name")
    suspend fun updateRouteTagLastUsed(name: String, timestamp: Long)

    @Query("""
        SELECT id, routeTag FROM workouts
        WHERE status = 'COMPLETED'
          AND activityType IN ('DOG_WALK', 'DOG_RUN')
          AND routeTag IS NOT NULL AND routeTag != ''
        GROUP BY routeTag
        HAVING startTime = MAX(startTime)
        ORDER BY routeTag ASC
    """)
    suspend fun getMostRecentWorkoutIdPerRouteTag(): List<RouteTagWorkoutId>

    data class RouteTagWorkoutId(
        val id: String,
        val routeTag: String,
    )

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
        if (phases.isNotEmpty()) insertPhases(phases)
        if (laps.isNotEmpty()) insertLaps(laps)
        if (gpsPoints.isNotEmpty()) {
            insertGpsPoints(gpsPoints)
        }
    }

    // --- Dog Walk Events ---

    @Insert
    suspend fun insertDogWalkEvent(event: DogWalkEventEntity)

    @Insert
    suspend fun insertDogWalkEvents(events: List<DogWalkEventEntity>)

    @Query("SELECT * FROM dog_walk_events WHERE workoutId = :workoutId ORDER BY timestamp ASC")
    suspend fun getDogWalkEventsForWorkout(workoutId: String): List<DogWalkEventEntity>

    @Query("SELECT COUNT(*) FROM dog_walk_events WHERE eventType = :eventType")
    suspend fun getTotalEventCountByType(eventType: String): Int

    @Query("SELECT COUNT(*) FROM dog_walk_events")
    suspend fun getTotalDogWalkEventCount(): Int

    @Query("SELECT COUNT(DISTINCT eventType) FROM dog_walk_events WHERE workoutId = :workoutId")
    suspend fun getDistinctEventTypeCountForWorkout(workoutId: String): Int

    // --- Walk Photos ---

    @Insert
    suspend fun insertWalkPhoto(photo: WalkPhotoEntity)

    @Query("SELECT * FROM walk_photos WHERE workoutId = :workoutId ORDER BY createdAt ASC")
    suspend fun getPhotosForWorkout(workoutId: String): List<WalkPhotoEntity>

    @Query("DELETE FROM walk_photos WHERE id = :id")
    suspend fun deleteWalkPhoto(id: String)

    @Query("DELETE FROM walk_photos WHERE workoutId = :workoutId")
    suspend fun deletePhotosForWorkout(workoutId: String)

    // --- Deletes ---

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: String)

    @Query("DELETE FROM laps WHERE id = :lapId")
    suspend fun deleteLap(lapId: String)
}
