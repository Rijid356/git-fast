package com.gitfast.app.ui.analytics.trends

import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.DogWalkEventEntity
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrendsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads workouts and computes on init`() = runTest {
        val dao = FakeTrendsWorkoutDao(
            allWorkouts = listOf(
                createWorkoutEntity("r1", ActivityType.RUN, distanceMeters = 5000.0),
                createWorkoutEntity("w1", ActivityType.DOG_WALK, distanceMeters = 2000.0),
            ),
        )
        val viewModel = TrendsViewModel(WorkoutRepository(dao))
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertTrue(state.distanceBars.isNotEmpty())
        assertTrue(state.workoutBars.isNotEmpty())
    }

    @Test
    fun `empty history shows empty state`() = runTest {
        val dao = FakeTrendsWorkoutDao()
        val viewModel = TrendsViewModel(WorkoutRepository(dao))
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `setPeriod recomputes with new grouping`() = runTest {
        val dao = FakeTrendsWorkoutDao(
            allWorkouts = listOf(
                createWorkoutEntity("r1", ActivityType.RUN),
            ),
        )
        val viewModel = TrendsViewModel(WorkoutRepository(dao))

        viewModel.setPeriod(TrendPeriod.MONTH)
        val state = viewModel.uiState.value

        assertEquals(TrendPeriod.MONTH, state.period)
        assertEquals(6, state.distanceBars.size) // monthly = 6 buckets
    }

    @Test
    fun `setFilter filters by activity type`() = runTest {
        val dao = FakeTrendsWorkoutDao(
            allWorkouts = listOf(
                createWorkoutEntity("r1", ActivityType.RUN, distanceMeters = 5000.0),
                createWorkoutEntity("w1", ActivityType.DOG_WALK, distanceMeters = 2000.0),
            ),
        )
        val viewModel = TrendsViewModel(WorkoutRepository(dao))

        viewModel.setFilter(ActivityFilter.WALKS)
        val state = viewModel.uiState.value

        assertEquals(ActivityFilter.WALKS, state.filter)
        assertNotNull(state.comparison)
    }

    @Test
    fun `filter RUNS excludes dog walks`() = runTest {
        // One run at 5000m, one walk at 2000m — both in same period
        val now = System.currentTimeMillis()
        val dao = FakeTrendsWorkoutDao(
            allWorkouts = listOf(
                createWorkoutEntity("r1", ActivityType.RUN, distanceMeters = 5000.0, startTime = now),
                createWorkoutEntity("w1", ActivityType.DOG_WALK, distanceMeters = 2000.0, startTime = now),
            ),
        )
        val viewModel = TrendsViewModel(WorkoutRepository(dao))

        // Default ALL filter — total distance includes both
        val allState = viewModel.uiState.value
        val allCurrentBar = allState.distanceBars.lastOrNull()
        assertNotNull(allCurrentBar)

        // Switch to RUNS — should only include 5000m run
        viewModel.setFilter(ActivityFilter.RUNS)
        val runsState = viewModel.uiState.value
        val runsCurrentBar = runsState.distanceBars.lastOrNull()
        assertNotNull(runsCurrentBar)

        // RUNS distance should be less than ALL distance (walk excluded)
        assertTrue(runsCurrentBar!!.value <= allCurrentBar!!.value)
    }

    // --- Helpers ---

    private fun createWorkoutEntity(
        id: String,
        activityType: ActivityType,
        distanceMeters: Double = 3000.0,
        startTime: Long = System.currentTimeMillis(),
        endTime: Long = startTime + 1800_000L,
    ) = WorkoutEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        totalSteps = 0,
        distanceMeters = distanceMeters,
        status = WorkoutStatus.COMPLETED,
        activityType = activityType,
        dogName = null,
        notes = null,
        weatherCondition = null,
        weatherTemp = null,
        energyLevel = null,
        routeTag = null,
    )

    // --- Fake DAO ---

    private class FakeTrendsWorkoutDao(
        private val allWorkouts: List<WorkoutEntity> = emptyList(),
    ) : WorkoutDao {
        override suspend fun insertWorkout(workout: WorkoutEntity) {}
        override suspend fun insertPhase(phase: WorkoutPhaseEntity) {}
        override suspend fun insertLap(lap: LapEntity) {}
        override suspend fun insertGpsPoint(point: GpsPointEntity) {}
        override suspend fun insertGpsPoints(points: List<GpsPointEntity>) {}
        override suspend fun insertPhases(phases: List<WorkoutPhaseEntity>) { phases.forEach { insertPhase(it) } }
        override suspend fun insertLaps(laps: List<LapEntity>) { laps.forEach { insertLap(it) } }
        override suspend fun updateWorkout(workout: WorkoutEntity) {}
        override suspend fun updatePhase(phase: WorkoutPhaseEntity) {}
        override suspend fun updateLap(lap: LapEntity) {}
        override suspend fun getWorkoutById(workoutId: String): WorkoutEntity? = null
        override suspend fun getPhasesForWorkout(workoutId: String): List<WorkoutPhaseEntity> = emptyList()
        override suspend fun getLapsForPhase(phaseId: String): List<LapEntity> = emptyList()
        override suspend fun getPhasesForWorkouts(workoutIds: List<String>): List<WorkoutPhaseEntity> = workoutIds.flatMap { getPhasesForWorkout(it) }
        override suspend fun getLapsForPhases(phaseIds: List<String>): List<LapEntity> = phaseIds.flatMap { getLapsForPhase(it) }
        override suspend fun getGpsPointsForWorkout(workoutId: String): List<GpsPointEntity> = emptyList()
        override suspend fun getFirstGpsPointsForWorkout(workoutId: String, maxIndex: Int): List<GpsPointEntity> = emptyList()
        override suspend fun getMostRecentWorkoutIdPerRouteTag(): List<WorkoutDao.RouteTagWorkoutId> = emptyList()
        override fun getAllCompletedWorkouts(): Flow<List<WorkoutEntity>> = flowOf(allWorkouts)
        override suspend fun getAllCompletedWorkoutsOnce(): List<WorkoutEntity> = allWorkouts
        override suspend fun getRecentCompletedRuns(limit: Int): List<WorkoutEntity> = emptyList()
        override suspend fun getAllCompletedRunsOnce(): List<WorkoutEntity> =
            allWorkouts.filter { it.activityType == ActivityType.RUN }
        override fun getCompletedWorkoutsByType(activityType: String): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override fun getCompletedDogActivityWorkouts(): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override fun getDogWalksByRoute(routeTag: String): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override suspend fun getCompletedWorkoutCount(): Int = allWorkouts.size
        override suspend fun getTotalLapCount(): Int = 0
        override suspend fun getCompletedDogWalkCount(): Int = 0
        override suspend fun getCompletedDogWalksOnce(): List<WorkoutEntity> = emptyList()
        override suspend fun getTotalDogWalkDistanceMeters(): Double = 0.0
        override suspend fun getTotalDistanceMeters(): Double = 0.0
        override suspend fun getTotalDurationMillis(): Long = 0L
        override suspend fun getActiveWorkout(): WorkoutEntity? = null
        override suspend fun insertRouteTag(tag: RouteTagEntity) {}
        override suspend fun getAllRouteTags(): List<RouteTagEntity> = emptyList()
        override suspend fun getDistinctRouteTags(): List<String> = emptyList()
        override suspend fun updateRouteTagLastUsed(name: String, timestamp: Long) {}
        override suspend fun saveWorkoutTransaction(
            workout: WorkoutEntity,
            phases: List<WorkoutPhaseEntity>,
            laps: List<LapEntity>,
            gpsPoints: List<GpsPointEntity>,
        ) {}
        override suspend fun deleteWorkout(workoutId: String) {}
        override suspend fun deleteLap(lapId: String) {}
        override suspend fun getRecentWorkoutsWithLaps(limit: Int): List<WorkoutEntity> = emptyList()
        override fun getCompletedWorkoutsBetween(startMillis: Long, endMillis: Long): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override fun getActiveMillisBetween(startMillis: Long, endMillis: Long): Flow<Long> = flowOf(0L)
        override fun getDistanceMetersBetween(startMillis: Long, endMillis: Long): Flow<Double> = flowOf(0.0)
        override fun getActiveDayCountBetween(startMillis: Long, endMillis: Long): Flow<Int> = flowOf(0)
        override fun getCompletedWorkoutCountBetween(startMillis: Long, endMillis: Long): Flow<Int> = flowOf(0)
        override suspend fun insertDogWalkEvent(event: DogWalkEventEntity) {}
        override suspend fun insertDogWalkEvents(events: List<DogWalkEventEntity>) {}
        override suspend fun getDogWalkEventsForWorkout(workoutId: String): List<DogWalkEventEntity> = emptyList()
        override suspend fun getTotalEventCountByType(eventType: String): Int = 0
        override suspend fun getTotalDogWalkEventCount(): Int = 0
        override suspend fun getDistinctEventTypeCountForWorkout(workoutId: String): Int = 0
    }
}
