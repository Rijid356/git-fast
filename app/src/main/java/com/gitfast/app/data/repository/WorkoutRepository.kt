package com.gitfast.app.data.repository

import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WalkPhotoEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.local.mappers.toDomain
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.Workout
import com.gitfast.app.analysis.RouteAutoDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) {
    /**
     * Batch-load phases for a list of workouts in a single query,
     * then map entities to domain models. Eliminates N+1 queries.
     */
    private suspend fun List<WorkoutEntity>.toDomainWithPhases(): List<Workout> {
        if (isEmpty()) return emptyList()
        val allPhases = workoutDao.getPhasesForWorkouts(map { it.id })
        val phasesByWorkout = allPhases.groupBy { it.workoutId }
        return map { entity ->
            val phases = phasesByWorkout[entity.id]?.map { it.toDomain(emptyList()) } ?: emptyList()
            entity.toDomain(phases, emptyList())
        }
    }

    private fun Flow<List<WorkoutEntity>>.toDomainWithPhases(): Flow<List<Workout>> {
        return map { entities -> entities.toDomainWithPhases() }
    }

    fun getCompletedWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllCompletedWorkouts().toDomainWithPhases()
    }

    fun getCompletedWorkoutsByType(activityType: ActivityType): Flow<List<Workout>> {
        return workoutDao.getCompletedWorkoutsByType(activityType.name).toDomainWithPhases()
    }

    fun getCompletedDogActivityWorkouts(): Flow<List<Workout>> {
        return workoutDao.getCompletedDogActivityWorkouts().toDomainWithPhases()
    }

    fun getDogWalksByRoute(routeTag: String): Flow<List<Workout>> {
        return workoutDao.getDogWalksByRoute(routeTag).toDomainWithPhases()
    }

    suspend fun getDogWalksByRouteOnce(routeTag: String): List<Workout> {
        return workoutDao.getDogWalksByRoute(routeTag).first().toDomainWithPhases()
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
        workoutDao.insertWorkout(workout)
        if (phases.isNotEmpty()) workoutDao.insertPhases(phases)
        if (laps.isNotEmpty()) workoutDao.insertLaps(laps)
        if (gpsPoints.isNotEmpty()) workoutDao.insertGpsPoints(gpsPoints)
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

    suspend fun deleteLap(lapId: String) {
        workoutDao.deleteLap(lapId)
    }

    suspend fun getCompletedWorkoutCount(): Int {
        return workoutDao.getCompletedWorkoutCount()
    }

    suspend fun getAllRouteTags(): List<RouteTagEntity> {
        return workoutDao.getAllRouteTags()
    }

    suspend fun getAllRouteTagNames(): List<String> {
        val dbTags = workoutDao.getAllRouteTags()
            .sortedByDescending { it.lastUsed }
            .map { it.name }
        val workoutTags = workoutDao.getDistinctRouteTags()
        return (dbTags + workoutTags).distinct()
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

    suspend fun getTotalDistanceMeters(): Double {
        return workoutDao.getTotalDistanceMeters()
    }

    suspend fun getTotalDurationMillis(): Long {
        return workoutDao.getTotalDurationMillis()
    }

    suspend fun getRouteCandidatesForAutoDetect(): List<RouteAutoDetector.RouteCandidate> {
        val routeWorkouts = workoutDao.getMostRecentWorkoutIdPerRouteTag()
        return routeWorkouts.mapNotNull { (workoutId, routeTag) ->
            val gpsPoints = workoutDao.getFirstGpsPointsForWorkout(workoutId, 20)
            if (gpsPoints.size < 2) return@mapNotNull null
            RouteAutoDetector.RouteCandidate(
                routeTag = routeTag,
                referencePoints = gpsPoints.map { it.toDomain() },
            )
        }
    }

    suspend fun getWorkoutsWithGpsForRouteTag(routeTag: String): List<Workout> {
        return workoutDao.getDogWalksByRoute(routeTag).first()
            .take(5)
            .map { entity ->
                val gpsPoints = workoutDao.getGpsPointsForWorkout(entity.id).map { it.toDomain() }
                entity.toDomain(emptyList(), gpsPoints)
            }
    }

    suspend fun getAllRunsWithLaps(): List<Workout> {
        val entities = workoutDao.getAllCompletedRunsOnce()
        return entities.toDomainWithPhasesAndLaps()
    }

    /**
     * Batch-load phases AND laps for a list of workouts in 3 queries total
     * (workouts, phases, laps) instead of 1+N+M.
     */
    private suspend fun List<WorkoutEntity>.toDomainWithPhasesAndLaps(): List<Workout> {
        if (isEmpty()) return emptyList()
        val allPhases = workoutDao.getPhasesForWorkouts(map { it.id })
        val phaseIds = allPhases.map { it.id }
        val allLaps = if (phaseIds.isNotEmpty()) {
            workoutDao.getLapsForPhases(phaseIds)
        } else emptyList()
        val lapsByPhase = allLaps.groupBy { it.phaseId }
        val phasesByWorkout = allPhases.groupBy { it.workoutId }
        return map { entity ->
            val phases = phasesByWorkout[entity.id]?.map { phase ->
                val laps = lapsByPhase[phase.id]?.map { it.toDomain() } ?: emptyList()
                phase.toDomain(laps)
            } ?: emptyList()
            entity.toDomain(phases, emptyList())
        }
    }

    // --- Daily/weekly activity goals ---

    fun getTodaysActiveMillis(): Flow<Long> {
        val (start, end) = todayRange()
        return workoutDao.getActiveMillisBetween(start, end)
    }

    fun getTodaysDistanceMeters(): Flow<Double> {
        val (start, end) = todayRange()
        return workoutDao.getDistanceMetersBetween(start, end)
    }

    fun getWeeklyActiveDayCount(): Flow<Int> {
        val (start, end) = weekRange()
        return workoutDao.getActiveDayCountBetween(start, end)
    }

    private fun todayRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    private fun weekRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monday = today.with(DayOfWeek.MONDAY)
        val start = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    private fun previousWeekRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val thisMonday = today.with(DayOfWeek.MONDAY)
        val prevMonday = thisMonday.minusWeeks(1)
        val start = prevMonday.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = thisMonday.atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    // --- Weekly summary ---

    fun getWeeklyActiveMillis(): Flow<Long> {
        val (start, end) = weekRange()
        return workoutDao.getActiveMillisBetween(start, end)
    }

    fun getWeeklyDistanceMeters(): Flow<Double> {
        val (start, end) = weekRange()
        return workoutDao.getDistanceMetersBetween(start, end)
    }

    fun getWeeklyWorkoutCount(): Flow<Int> {
        val (start, end) = weekRange()
        return workoutDao.getCompletedWorkoutCountBetween(start, end)
    }

    fun getPreviousWeekActiveMillis(): Flow<Long> {
        val (start, end) = previousWeekRange()
        return workoutDao.getActiveMillisBetween(start, end)
    }

    fun getPreviousWeekDistanceMeters(): Flow<Double> {
        val (start, end) = previousWeekRange()
        return workoutDao.getDistanceMetersBetween(start, end)
    }

    fun getPreviousWeekWorkoutCount(): Flow<Int> {
        val (start, end) = previousWeekRange()
        return workoutDao.getCompletedWorkoutCountBetween(start, end)
    }

    suspend fun getRecentWorkoutsWithLaps(limit: Int): List<Workout> {
        return workoutDao.getRecentWorkoutsWithLaps(limit).toDomainWithPhasesAndLaps()
    }

    // --- Dog Walk Events ---

    suspend fun getDogWalkEventsForWorkout(workoutId: String): List<DogWalkEvent> {
        return workoutDao.getDogWalkEventsForWorkout(workoutId).map { it.toDomain() }
    }

    suspend fun getTotalDogWalkEventCount(): Int {
        return workoutDao.getTotalDogWalkEventCount()
    }

    suspend fun getTotalEventCountByType(eventType: String): Int {
        return workoutDao.getTotalEventCountByType(eventType)
    }

    // --- Walk Photos ---

    suspend fun insertWalkPhoto(photo: WalkPhotoEntity) {
        workoutDao.insertWalkPhoto(photo)
    }

    suspend fun getPhotosForWorkout(workoutId: String): List<WalkPhotoEntity> {
        return workoutDao.getPhotosForWorkout(workoutId)
    }

    suspend fun deleteWalkPhoto(id: String) {
        workoutDao.deleteWalkPhoto(id)
    }

    suspend fun deletePhotosForWorkout(workoutId: String) {
        workoutDao.deletePhotosForWorkout(workoutId)
    }
}
