package com.gitfast.app.ui.analytics.records

import com.gitfast.app.data.local.WorkoutDao
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalRecordsViewModelTest {

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
    fun `loads records on init`() = runTest {
        val dao = FakeRecordsWorkoutDao(
            allWorkouts = listOf(
                createWorkoutEntity("r1", ActivityType.RUN, distanceMeters = 5000.0),
                createWorkoutEntity("w1", ActivityType.DOG_WALK, distanceMeters = 2000.0, steps = 3000),
            ),
            totalDistance = 7000.0,
            totalDogWalkDistance = 2000.0,
        )
        val viewModel = PersonalRecordsViewModel(WorkoutRepository(dao))
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertEquals(3, state.sections.size)
        assertEquals("RUNNING", state.sections[0].header)
        assertEquals("DOG WALKS", state.sections[1].header)
        assertEquals("OVERALL", state.sections[2].header)
    }

    @Test
    fun `empty history shows empty state`() = runTest {
        val dao = FakeRecordsWorkoutDao()
        val viewModel = PersonalRecordsViewModel(WorkoutRepository(dao))
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertTrue(state.sections.isEmpty())
    }

    @Test
    fun `sections only appear when records exist`() = runTest {
        // Only runs, no walks
        val dao = FakeRecordsWorkoutDao(
            allWorkouts = listOf(
                createWorkoutEntity("r1", ActivityType.RUN, distanceMeters = 5000.0),
            ),
            totalDistance = 5000.0,
        )
        val viewModel = PersonalRecordsViewModel(WorkoutRepository(dao))
        val state = viewModel.uiState.value

        assertEquals(2, state.sections.size) // RUNNING + OVERALL, no DOG WALKS
        assertEquals("RUNNING", state.sections[0].header)
        assertEquals("OVERALL", state.sections[1].header)
    }

    // --- Helpers ---

    private fun createWorkoutEntity(
        id: String,
        activityType: ActivityType,
        distanceMeters: Double = 3000.0,
        startTime: Long = 1000L,
        endTime: Long = 1800_000L,
        steps: Int = 0,
    ) = WorkoutEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        totalSteps = steps,
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

    private class FakeRecordsWorkoutDao(
        private val allWorkouts: List<WorkoutEntity> = emptyList(),
        private val totalDistance: Double = 0.0,
        private val totalDogWalkDistance: Double = 0.0,
    ) : WorkoutDao {
        override suspend fun insertWorkout(workout: WorkoutEntity) {}
        override suspend fun insertPhase(phase: WorkoutPhaseEntity) {}
        override suspend fun insertLap(lap: LapEntity) {}
        override suspend fun insertGpsPoint(point: GpsPointEntity) {}
        override suspend fun insertGpsPoints(points: List<GpsPointEntity>) {}
        override suspend fun updateWorkout(workout: WorkoutEntity) {}
        override suspend fun updatePhase(phase: WorkoutPhaseEntity) {}
        override suspend fun updateLap(lap: LapEntity) {}
        override suspend fun getWorkoutById(workoutId: String): WorkoutEntity? = null
        override suspend fun getPhasesForWorkout(workoutId: String): List<WorkoutPhaseEntity> = emptyList()
        override suspend fun getLapsForPhase(phaseId: String): List<LapEntity> = emptyList()
        override suspend fun getGpsPointsForWorkout(workoutId: String): List<GpsPointEntity> = emptyList()
        override fun getAllCompletedWorkouts(): Flow<List<WorkoutEntity>> = flowOf(allWorkouts)
        override suspend fun getAllCompletedWorkoutsOnce(): List<WorkoutEntity> = allWorkouts
        override suspend fun getRecentCompletedRuns(limit: Int): List<WorkoutEntity> = emptyList()
        override suspend fun getAllCompletedRunsOnce(): List<WorkoutEntity> =
            allWorkouts.filter { it.activityType == ActivityType.RUN }
        override fun getCompletedWorkoutsByType(activityType: String): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override fun getDogWalksByRoute(routeTag: String): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override suspend fun getCompletedWorkoutCount(): Int = allWorkouts.size
        override suspend fun getTotalLapCount(): Int = 0
        override suspend fun getCompletedDogWalkCount(): Int = 0
        override suspend fun getCompletedDogWalksOnce(): List<WorkoutEntity> = emptyList()
        override suspend fun getTotalDogWalkDistanceMeters(): Double = totalDogWalkDistance
        override suspend fun getTotalDistanceMeters(): Double = totalDistance
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
    }
}
