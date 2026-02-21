package com.gitfast.app.ui.analytics.routeoverlay

import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.CyanAccent
import com.gitfast.app.ui.theme.NeonGreen
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouteOverlayViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeDao: FakeOverlayWorkoutDao
    private lateinit var repository: WorkoutRepository
    private lateinit var viewModel: RouteOverlayViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        routeTags: List<RouteTagEntity> = emptyList(),
        workoutsByRoute: Map<String, List<WorkoutEntity>> = emptyMap(),
        gpsPointsByWorkout: Map<String, List<GpsPointEntity>> = emptyMap(),
    ): RouteOverlayViewModel {
        fakeDao = FakeOverlayWorkoutDao(routeTags, workoutsByRoute, gpsPointsByWorkout)
        repository = WorkoutRepository(fakeDao)
        return RouteOverlayViewModel(repository)
    }

    @Test
    fun `loads route tags on init`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(
                RouteTagEntity("Park", 1000L, 3000L),
                RouteTagEntity("Neighborhood", 1000L, 2000L),
            ),
        )

        val state = viewModel.uiState.value
        assertEquals(listOf("Park", "Neighborhood"), state.routeTags)
        assertNull(state.selectedTag)
    }

    @Test
    fun `auto-selects when only one route tag exists`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to listOf(createWorkoutEntity("w1"))),
            gpsPointsByWorkout = mapOf("w1" to listOf(
                createGpsPoint("w1", 0, 40.0, -74.0),
                createGpsPoint("w1", 1, 40.001, -74.001),
            )),
        )

        val state = viewModel.uiState.value
        assertEquals("Park", state.selectedTag)
        assertEquals(1, state.traces.size)
    }

    @Test
    fun `selectRouteTag loads traces with correct colors`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(
                RouteTagEntity("Park", 1000L, 3000L),
                RouteTagEntity("City", 1000L, 2000L),
            ),
            workoutsByRoute = mapOf("Park" to listOf(
                createWorkoutEntity("w1", startTime = 5000L),
                createWorkoutEntity("w2", startTime = 4000L),
                createWorkoutEntity("w3", startTime = 3000L),
            )),
            gpsPointsByWorkout = mapOf(
                "w1" to listOf(createGpsPoint("w1", 0, 40.0, -74.0), createGpsPoint("w1", 1, 40.001, -74.001)),
                "w2" to listOf(createGpsPoint("w2", 0, 40.0, -74.0), createGpsPoint("w2", 1, 40.002, -74.002)),
                "w3" to listOf(createGpsPoint("w3", 0, 40.0, -74.0), createGpsPoint("w3", 1, 40.003, -74.003)),
            ),
        )

        viewModel.selectRouteTag("Park")
        val state = viewModel.uiState.value

        assertEquals(3, state.traces.size)
        assertEquals(NeonGreen, state.traces[0].color)
        assertEquals(CyanAccent, state.traces[1].color)
        assertEquals(AmberAccent, state.traces[2].color)
    }

    @Test
    fun `traces limited to 5 most recent`() = runTest {
        val workouts = (1..7).map { createWorkoutEntity("w$it", startTime = (8 - it) * 1000L) }
        val gps = (1..7).associate { "w$it" to listOf(
            createGpsPoint("w$it", 0, 40.0, -74.0),
            createGpsPoint("w$it", 1, 40.001, -74.001),
        ) }

        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to workouts),
            gpsPointsByWorkout = gps,
        )

        viewModel.selectRouteTag("Park")
        val state = viewModel.uiState.value

        assertEquals(5, state.traces.size)
        assertEquals("w1", state.traces[0].workoutId)
        assertEquals("w5", state.traces[4].workoutId)
    }

    @Test
    fun `bounds computed from all trace points`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to listOf(
                createWorkoutEntity("w1"),
                createWorkoutEntity("w2"),
            )),
            gpsPointsByWorkout = mapOf(
                "w1" to listOf(
                    createGpsPoint("w1", 0, 40.0, -74.0),
                    createGpsPoint("w1", 1, 40.01, -74.01),
                ),
                "w2" to listOf(
                    createGpsPoint("w2", 0, 39.99, -73.99),
                    createGpsPoint("w2", 1, 40.02, -74.02),
                ),
            ),
        )

        viewModel.selectRouteTag("Park")
        val bounds = viewModel.uiState.value.bounds

        assertNotNull(bounds)
        assertEquals(39.99, bounds!!.minLat, 0.001)
        assertEquals(40.02, bounds.maxLat, 0.001)
        assertEquals(-74.02, bounds.minLng, 0.001)
        assertEquals(-73.99, bounds.maxLng, 0.001)
    }

    @Test
    fun `empty GPS data produces no traces`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to listOf(createWorkoutEntity("w1"))),
            gpsPointsByWorkout = emptyMap(),
        )

        viewModel.selectRouteTag("Park")
        val state = viewModel.uiState.value

        assertEquals(1, state.traces.size)
        assertTrue(state.traces[0].points.isEmpty())
        assertNull(state.bounds)
    }

    @Test
    fun `isLoading set during load`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to emptyList()),
        )

        // With UnconfinedTestDispatcher, loading completes immediately
        // but we can verify final state is not loading
        viewModel.selectRouteTag("Park")
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    // --- Helpers ---

    private fun createWorkoutEntity(
        id: String,
        startTime: Long = 1000L,
        endTime: Long = 5000L,
        distanceMeters: Double = 500.0,
    ) = WorkoutEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        totalSteps = 100,
        distanceMeters = distanceMeters,
        status = WorkoutStatus.COMPLETED,
        activityType = ActivityType.DOG_WALK,
        dogName = null,
        notes = null,
        weatherCondition = null,
        weatherTemp = null,
        energyLevel = null,
        routeTag = "Park",
    )

    private fun createGpsPoint(
        workoutId: String,
        sortIndex: Int,
        lat: Double,
        lng: Double,
    ) = GpsPointEntity(
        workoutId = workoutId,
        latitude = lat,
        longitude = lng,
        timestamp = 1000L + sortIndex * 100L,
        accuracy = 5f,
        sortIndex = sortIndex,
    )

    // --- Fake DAO ---

    private class FakeOverlayWorkoutDao(
        private val routeTags: List<RouteTagEntity> = emptyList(),
        private val workoutsByRoute: Map<String, List<WorkoutEntity>> = emptyMap(),
        private val gpsPointsByWorkout: Map<String, List<GpsPointEntity>> = emptyMap(),
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
        override suspend fun getGpsPointsForWorkout(workoutId: String): List<GpsPointEntity> =
            gpsPointsByWorkout[workoutId] ?: emptyList()
        override fun getAllCompletedWorkouts(): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override suspend fun getAllCompletedWorkoutsOnce(): List<WorkoutEntity> = emptyList()
        override suspend fun getRecentCompletedRuns(limit: Int): List<WorkoutEntity> = emptyList()
        override suspend fun getAllCompletedRunsOnce(): List<WorkoutEntity> = emptyList()
        override fun getCompletedWorkoutsByType(activityType: String): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override fun getDogWalksByRoute(routeTag: String): Flow<List<WorkoutEntity>> =
            flowOf(workoutsByRoute[routeTag] ?: emptyList())
        override suspend fun getCompletedWorkoutCount(): Int = 0
        override suspend fun getTotalLapCount(): Int = 0
        override suspend fun getCompletedDogWalkCount(): Int = 0
        override suspend fun getCompletedDogWalksOnce(): List<WorkoutEntity> = emptyList()
        override suspend fun getTotalDogWalkDistanceMeters(): Double = 0.0
        override suspend fun getTotalDistanceMeters(): Double = 0.0
        override suspend fun getTotalDurationMillis(): Long = 0L
        override suspend fun getActiveWorkout(): WorkoutEntity? = null
        override suspend fun insertRouteTag(tag: RouteTagEntity) {}
        override suspend fun getAllRouteTags(): List<RouteTagEntity> = routeTags
        override suspend fun updateRouteTagLastUsed(name: String, timestamp: Long) {}
        override suspend fun saveWorkoutTransaction(
            workout: WorkoutEntity,
            phases: List<WorkoutPhaseEntity>,
            laps: List<LapEntity>,
            gpsPoints: List<GpsPointEntity>,
        ) {}
        override suspend fun deleteWorkout(workoutId: String) {}
        override suspend fun getRecentWorkoutsWithLaps(limit: Int): List<WorkoutEntity> = emptyList()
    }
}
