package com.gitfast.app.ui.analytics.routeperformance

import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class RoutePerformanceViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeDao: FakePerfWorkoutDao
    private lateinit var repository: WorkoutRepository
    private lateinit var viewModel: RoutePerformanceViewModel

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
    ): RoutePerformanceViewModel {
        fakeDao = FakePerfWorkoutDao(routeTags, workoutsByRoute)
        repository = WorkoutRepository(fakeDao)
        return RoutePerformanceViewModel(repository)
    }

    @Test
    fun `loads route tags on init`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(
                RouteTagEntity("Park", 1000L, 3000L),
                RouteTagEntity("City", 1000L, 2000L),
            ),
        )

        val state = viewModel.uiState.value
        assertEquals(listOf("Park", "City"), state.routeTags)
        assertNull(state.selectedTag)
    }

    @Test
    fun `auto-selects when only one route tag exists`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to listOf(
                createWorkoutEntity("w1", startTime = 1000L, endTime = 31000L),
            )),
        )

        val state = viewModel.uiState.value
        assertEquals("Park", state.selectedTag)
        assertEquals(1, state.rows.size)
    }

    @Test
    fun `selectRouteTag loads all sessions with correct deltas`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 3000L)),
            workoutsByRoute = mapOf("Park" to listOf(
                createWorkoutEntity("w1", startTime = 5000L, endTime = 35000L),  // 30s duration (most recent)
                createWorkoutEntity("w2", startTime = 4000L, endTime = 44000L),  // 40s duration
                createWorkoutEntity("w3", startTime = 3000L, endTime = 28000L),  // 25s duration
            )),
        )

        viewModel.selectRouteTag("Park")
        val state = viewModel.uiState.value

        assertEquals(3, state.rows.size)
        assertEquals(3, state.sessionCount)
    }

    @Test
    fun `most recent session has no delta`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to listOf(
                createWorkoutEntity("w1", startTime = 5000L, endTime = 35000L),
                createWorkoutEntity("w2", startTime = 4000L, endTime = 44000L),
            )),
        )

        viewModel.selectRouteTag("Park")
        val rows = viewModel.uiState.value.rows

        assertTrue(rows[0].isMostRecent)
        assertNull(rows[0].deltaFormatted)
        assertNull(rows[0].deltaMillis)

        assertFalse(rows[1].isMostRecent)
        assertNotNull(rows[1].deltaFormatted)
    }

    @Test
    fun `personal best correctly identified`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to listOf(
                createWorkoutEntity("w1", startTime = 5000L, endTime = 35000L),  // 30s
                createWorkoutEntity("w2", startTime = 4000L, endTime = 29000L),  // 25s — fastest
                createWorkoutEntity("w3", startTime = 3000L, endTime = 43000L),  // 40s
            )),
        )

        viewModel.selectRouteTag("Park")
        val state = viewModel.uiState.value

        assertNotNull(state.personalBest)
        assertEquals("w2", state.personalBest!!.workoutId)
        assertTrue(state.rows.find { it.workoutId == "w2" }!!.isPersonalBest)
        assertFalse(state.rows.find { it.workoutId == "w1" }!!.isPersonalBest)
    }

    @Test
    fun `personal best not shown with single session`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to listOf(
                createWorkoutEntity("w1", startTime = 5000L, endTime = 35000L),
            )),
        )

        viewModel.selectRouteTag("Park")
        assertNull(viewModel.uiState.value.personalBest)
    }

    @Test
    fun `trend summary computed when 4+ sessions`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to listOf(
                createWorkoutEntity("w1", startTime = 7000L, endTime = 37000L),  // 30s
                createWorkoutEntity("w2", startTime = 6000L, endTime = 36000L),  // 30s
                createWorkoutEntity("w3", startTime = 5000L, endTime = 35000L),  // 30s
                createWorkoutEntity("w4", startTime = 4000L, endTime = 44000L),  // 40s
            )),
        )

        viewModel.selectRouteTag("Park")
        assertNotNull(viewModel.uiState.value.trendSummary)
    }

    @Test
    fun `trend summary null when fewer than 4 sessions`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to listOf(
                createWorkoutEntity("w1", startTime = 5000L, endTime = 35000L),
                createWorkoutEntity("w2", startTime = 4000L, endTime = 34000L),
                createWorkoutEntity("w3", startTime = 3000L, endTime = 33000L),
            )),
        )

        viewModel.selectRouteTag("Park")
        assertNull(viewModel.uiState.value.trendSummary)
    }

    @Test
    fun `improving trend detected`() = runTest {
        // Recent 3 are faster (20s each) than older 3 (40s each)
        val workouts = listOf(
            Workout(id = "w1", startTime = Instant.ofEpochMilli(6000), endTime = Instant.ofEpochMilli(26000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w2", startTime = Instant.ofEpochMilli(5000), endTime = Instant.ofEpochMilli(25000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w3", startTime = Instant.ofEpochMilli(4000), endTime = Instant.ofEpochMilli(24000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w4", startTime = Instant.ofEpochMilli(3000), endTime = Instant.ofEpochMilli(43000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w5", startTime = Instant.ofEpochMilli(2000), endTime = Instant.ofEpochMilli(42000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w6", startTime = Instant.ofEpochMilli(1000), endTime = Instant.ofEpochMilli(41000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
        )

        viewModel = createViewModel()
        val trend = viewModel.computeTrend(workouts)

        assertNotNull(trend)
        assertTrue(trend!!.isImproving)
        assertFalse(trend.isConsistent)
    }

    @Test
    fun `declining trend detected`() = runTest {
        // Recent 3 are slower (40s each) than older 3 (20s each)
        val workouts = listOf(
            Workout(id = "w1", startTime = Instant.ofEpochMilli(6000), endTime = Instant.ofEpochMilli(46000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w2", startTime = Instant.ofEpochMilli(5000), endTime = Instant.ofEpochMilli(45000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w3", startTime = Instant.ofEpochMilli(4000), endTime = Instant.ofEpochMilli(44000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w4", startTime = Instant.ofEpochMilli(3000), endTime = Instant.ofEpochMilli(23000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w5", startTime = Instant.ofEpochMilli(2000), endTime = Instant.ofEpochMilli(22000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w6", startTime = Instant.ofEpochMilli(1000), endTime = Instant.ofEpochMilli(21000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
        )

        viewModel = createViewModel()
        val trend = viewModel.computeTrend(workouts)

        assertNotNull(trend)
        assertFalse(trend!!.isImproving)
        assertFalse(trend.isConsistent)
    }

    @Test
    fun `consistent trend when delta under 5s`() = runTest {
        // All sessions ~30s — difference is under 5s threshold
        val workouts = listOf(
            Workout(id = "w1", startTime = Instant.ofEpochMilli(6000), endTime = Instant.ofEpochMilli(36000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w2", startTime = Instant.ofEpochMilli(5000), endTime = Instant.ofEpochMilli(35000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w3", startTime = Instant.ofEpochMilli(4000), endTime = Instant.ofEpochMilli(34000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
            Workout(id = "w4", startTime = Instant.ofEpochMilli(3000), endTime = Instant.ofEpochMilli(33000), totalSteps = 0, distanceMeters = 500.0, status = WorkoutStatus.COMPLETED, activityType = ActivityType.DOG_WALK, phases = emptyList(), gpsPoints = emptyList(), dogName = null, notes = null, weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = "Park"),
        )

        viewModel = createViewModel()
        val trend = viewModel.computeTrend(workouts)

        assertNotNull(trend)
        assertTrue(trend!!.isConsistent)
    }

    @Test
    fun `empty route produces no rows`() = runTest {
        viewModel = createViewModel(
            routeTags = listOf(RouteTagEntity("Park", 1000L, 2000L)),
            workoutsByRoute = mapOf("Park" to emptyList()),
        )

        viewModel.selectRouteTag("Park")
        val state = viewModel.uiState.value

        assertTrue(state.rows.isEmpty())
        assertEquals(0, state.sessionCount)
        assertNull(state.personalBest)
        assertNull(state.trendSummary)
    }

    // --- Helpers ---

    private fun createWorkoutEntity(
        id: String,
        startTime: Long = 1000L,
        endTime: Long = 31000L,
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

    // --- Fake DAO ---

    private class FakePerfWorkoutDao(
        private val routeTags: List<RouteTagEntity> = emptyList(),
        private val workoutsByRoute: Map<String, List<WorkoutEntity>> = emptyMap(),
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
        override fun getAllCompletedWorkouts(): Flow<List<WorkoutEntity>> = flowOf(emptyList())
        override suspend fun getAllCompletedWorkoutsOnce(): List<WorkoutEntity> = emptyList()
        override suspend fun getRecentCompletedRuns(limit: Int): List<WorkoutEntity> = emptyList()
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
