package com.gitfast.app

import androidx.lifecycle.SavedStateHandle
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.DogWalkEventType
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.Lap
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutPhase
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.ui.detail.DetailUiState
import com.gitfast.app.ui.detail.DetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var characterRepository: CharacterRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        workoutRepository = mockk()
        characterRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads workout and emits Loaded state`() = runTest {
        val workout = createTestWorkout("w1")
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value
        assertTrue(state is DetailUiState.Loaded)
        assertEquals("w1", (state as DetailUiState.Loaded).detail.workoutId)
    }

    @Test
    fun `init emits NotFound when workout does not exist`() = runTest {
        coEvery { workoutRepository.getWorkoutWithDetails("missing") } returns null

        val viewModel = createViewModel("missing")

        assertEquals(DetailUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun `deleteWorkout emits Deleted state`() = runTest {
        val workout = createTestWorkout("w1")
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null
        coEvery { workoutRepository.deleteWorkout("w1") } returns Unit

        val viewModel = createViewModel("w1")
        viewModel.deleteWorkout()

        assertEquals(DetailUiState.Deleted, viewModel.uiState.value)
        coVerify { workoutRepository.deleteWorkout("w1") }
    }

    @Test
    fun `loaded state includes xp from transaction`() = runTest {
        val workout = createTestWorkout("w1")
        val xpTx = XpTransaction("tx1", "w1", 50, "Run completed", Instant.ofEpochMilli(3000))
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns xpTx

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertEquals(50, state.detail.xpEarned)
        assertEquals("Run completed", state.detail.xpBreakdown)
    }

    @Test
    fun `loaded state has empty route comparison for runs`() = runTest {
        val workout = createTestWorkout("w1", activityType = ActivityType.RUN)
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertTrue(state.routeComparison.isEmpty())
    }

    @Test
    fun `loaded state fetches route comparison for dog walks with route tag`() = runTest {
        val workout = createTestWorkout("dw1", activityType = ActivityType.DOG_WALK, routeTag = "Park Loop")
        coEvery { workoutRepository.getWorkoutWithDetails("dw1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("dw1") } returns null
        coEvery { workoutRepository.getDogWalksByRouteOnce("Park Loop") } returns listOf(workout)
        coEvery { workoutRepository.getDogWalkEventsForWorkout("dw1") } returns emptyList()

        val viewModel = createViewModel("dw1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertEquals(1, state.routeComparison.size)
        assertTrue(state.routeComparison.first().isCurrentWalk)
    }

    @Test
    fun `loaded state xp defaults to zero when no transaction`() = runTest {
        val workout = createTestWorkout("w1")
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertEquals(0, state.detail.xpEarned)
        assertNull(state.detail.xpBreakdown)
    }

    @Test
    fun `deleteLap calls repository and reloads workout`() = runTest {
        val workout = createTestWorkout("w1")
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null
        coEvery { workoutRepository.deleteLap(any()) } returns Unit

        val viewModel = createViewModel("w1")
        viewModel.deleteLap("lap-1")

        coVerify { workoutRepository.deleteLap("lap-1") }
        coVerify(exactly = 2) { workoutRepository.getWorkoutWithDetails("w1") }
        assertTrue(viewModel.uiState.value is DetailUiState.Loaded)
    }

    @Test
    fun `dog walk without route tag has empty route comparison`() = runTest {
        val workout = createTestWorkout("dw1", activityType = ActivityType.DOG_WALK, routeTag = null)
        coEvery { workoutRepository.getWorkoutWithDetails("dw1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("dw1") } returns null
        coEvery { workoutRepository.getDogWalkEventsForWorkout("dw1") } returns emptyList()

        val viewModel = createViewModel("dw1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertTrue(state.routeComparison.isEmpty())
    }

    @Test
    fun `loaded state includes speed chart data from GPS points`() = runTest {
        val gpsPoints = listOf(
            GpsPoint(40.0, -74.0, Instant.ofEpochMilli(1000), 5f, speed = 3.0f),
            GpsPoint(40.001, -74.001, Instant.ofEpochMilli(61000), 5f, speed = 5.0f),
            GpsPoint(40.002, -74.002, Instant.ofEpochMilli(121000), 5f, speed = 4.0f),
        )
        val workout = createTestWorkout("w1", gpsPoints = gpsPoints)
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertEquals(3, state.speedChartPoints.size)
        val expectedAvg = (3.0f * 2.23694f + 5.0f * 2.23694f + 4.0f * 2.23694f) / 3f
        assertEquals(expectedAvg, state.averageSpeedMph, 0.01f)
        assertEquals(5.0f * 2.23694f, state.maxSpeedMph, 0.01f)
    }

    @Test
    fun `loaded state handles GPS points without speed`() = runTest {
        val gpsPoints = listOf(
            GpsPoint(40.0, -74.0, Instant.ofEpochMilli(1000), 5f, speed = null),
            GpsPoint(40.001, -74.001, Instant.ofEpochMilli(61000), 5f, speed = null),
        )
        val workout = createTestWorkout("w1", gpsPoints = gpsPoints)
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertTrue(state.speedChartPoints.isEmpty())
        assertEquals(0f, state.averageSpeedMph)
        assertEquals(0f, state.maxSpeedMph)
    }

    @Test
    fun `loaded state includes sprint laps for dog walks`() = runTest {
        val sprintLaps = listOf(
            Lap("lap1", 1, Instant.ofEpochMilli(1000), Instant.ofEpochMilli(2000), 50.0, 10),
            Lap("lap2", 2, Instant.ofEpochMilli(2000), Instant.ofEpochMilli(3000), 60.0, 12),
        )
        val warmupPhase = WorkoutPhase(
            id = "phase1",
            type = PhaseType.WARMUP,
            startTime = Instant.ofEpochMilli(1000),
            endTime = Instant.ofEpochMilli(3000),
            distanceMeters = 110.0,
            steps = 22,
            laps = sprintLaps,
        )
        val workout = createTestWorkout(
            "dw1",
            activityType = ActivityType.DOG_WALK,
            phases = listOf(warmupPhase),
        )
        coEvery { workoutRepository.getWorkoutWithDetails("dw1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("dw1") } returns null
        coEvery { workoutRepository.getDogWalkEventsForWorkout("dw1") } returns emptyList()

        val viewModel = createViewModel("dw1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertEquals(2, state.sprintLaps.size)
        assertEquals("lap1", state.sprintLaps[0].id)
        assertEquals("lap2", state.sprintLaps[1].id)
    }

    @Test
    fun `loaded state has empty sprint laps for runs`() = runTest {
        val warmupPhase = WorkoutPhase(
            id = "phase1",
            type = PhaseType.WARMUP,
            startTime = Instant.ofEpochMilli(1000),
            endTime = Instant.ofEpochMilli(3000),
            distanceMeters = 100.0,
            steps = 20,
            laps = listOf(
                Lap("lap1", 1, Instant.ofEpochMilli(1000), Instant.ofEpochMilli(2000), 50.0, 10),
            ),
        )
        val workout = createTestWorkout("w1", activityType = ActivityType.RUN, phases = listOf(warmupPhase))
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertTrue(state.sprintLaps.isEmpty())
    }

    @Test
    fun `loaded state includes lap analysis for laps phase`() = runTest {
        val laps = listOf(
            Lap("lap1", 1, Instant.ofEpochMilli(1000), Instant.ofEpochMilli(61000), 400.0, 50),
            Lap("lap2", 2, Instant.ofEpochMilli(61000), Instant.ofEpochMilli(121000), 400.0, 50),
        )
        val lapsPhase = WorkoutPhase(
            id = "phase1",
            type = PhaseType.LAPS,
            startTime = Instant.ofEpochMilli(1000),
            endTime = Instant.ofEpochMilli(121000),
            distanceMeters = 800.0,
            steps = 100,
            laps = laps,
        )
        val workout = createTestWorkout("w1", phases = listOf(lapsPhase))
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertNotNull(state.lapAnalysis)
        assertEquals(2, state.lapAnalysis!!.lapCount)
    }

    @Test
    fun `loaded state has null lap analysis when no laps phase`() = runTest {
        val workout = createTestWorkout("w1", phases = emptyList())
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertNull(state.lapAnalysis)
    }

    @Test
    fun `loaded state fetches dog walk events for dog walks`() = runTest {
        val events = listOf(
            DogWalkEvent("e1", "dw1", DogWalkEventType.POOP, Instant.ofEpochMilli(2000), 40.0, -74.0),
            DogWalkEvent("e2", "dw1", DogWalkEventType.DEEP_SNIFF, Instant.ofEpochMilli(3000), 40.001, -74.001),
        )
        val workout = createTestWorkout("dw1", activityType = ActivityType.DOG_WALK)
        coEvery { workoutRepository.getWorkoutWithDetails("dw1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("dw1") } returns null
        coEvery { workoutRepository.getDogWalkEventsForWorkout("dw1") } returns events

        val viewModel = createViewModel("dw1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertEquals(2, state.dogWalkEvents.size)
        assertEquals(DogWalkEventType.POOP, state.dogWalkEvents[0].eventType)
        assertEquals(DogWalkEventType.DEEP_SNIFF, state.dogWalkEvents[1].eventType)
        coVerify { workoutRepository.getDogWalkEventsForWorkout("dw1") }
    }

    @Test
    fun `loaded state skips dog walk events for runs`() = runTest {
        val workout = createTestWorkout("w1", activityType = ActivityType.RUN)
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { characterRepository.getXpTransactionForWorkout("w1") } returns null

        val viewModel = createViewModel("w1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertTrue(state.dogWalkEvents.isEmpty())
        coVerify(exactly = 0) { workoutRepository.getDogWalkEventsForWorkout(any()) }
    }

    @Test
    fun `loaded state with route comparison includes previous walks`() = runTest {
        val currentWalk = createTestWorkout("dw1", activityType = ActivityType.DOG_WALK, routeTag = "Park Loop")
        val previousWalk = createTestWorkout("dw2", activityType = ActivityType.DOG_WALK, routeTag = "Park Loop")
        coEvery { workoutRepository.getWorkoutWithDetails("dw1") } returns currentWalk
        coEvery { characterRepository.getXpTransactionForWorkout("dw1") } returns null
        coEvery { workoutRepository.getDogWalksByRouteOnce("Park Loop") } returns listOf(currentWalk, previousWalk)
        coEvery { workoutRepository.getDogWalkEventsForWorkout("dw1") } returns emptyList()

        val viewModel = createViewModel("dw1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        assertEquals(2, state.routeComparison.size)
        assertTrue(state.routeComparison[0].isCurrentWalk)
        assertEquals("dw2", state.routeComparison[1].workoutId)
    }

    private fun createViewModel(workoutId: String): DetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to workoutId))
        return DetailViewModel(workoutRepository, characterRepository, savedStateHandle)
    }

    private fun createTestWorkout(
        id: String,
        activityType: ActivityType = ActivityType.RUN,
        routeTag: String? = null,
        phases: List<WorkoutPhase> = emptyList(),
        gpsPoints: List<GpsPoint> = emptyList(),
    ): Workout {
        return Workout(
            id = id,
            startTime = Instant.ofEpochMilli(1000),
            endTime = Instant.ofEpochMilli(5000),
            totalSteps = 0,
            distanceMeters = 1609.34,
            status = WorkoutStatus.COMPLETED,
            activityType = activityType,
            phases = phases,
            gpsPoints = gpsPoints,
            dogName = if (activityType.isDogActivity) "Juniper" else null,
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = routeTag,
        )
    }
}
