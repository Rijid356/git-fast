package com.gitfast.app

import androidx.lifecycle.SavedStateHandle
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
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

        val viewModel = createViewModel("dw1")

        val state = viewModel.uiState.value as DetailUiState.Loaded
        // RouteComparisonAnalyzer always includes the current walk as first item
        assertEquals(1, state.routeComparison.size)
        assertTrue(state.routeComparison.first().isCurrentWalk)
    }

    private fun createViewModel(workoutId: String): DetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to workoutId))
        return DetailViewModel(workoutRepository, characterRepository, savedStateHandle)
    }

    private fun createTestWorkout(
        id: String,
        activityType: ActivityType = ActivityType.RUN,
        routeTag: String? = null,
    ): Workout {
        return Workout(
            id = id,
            startTime = Instant.ofEpochMilli(1000),
            endTime = Instant.ofEpochMilli(5000),
            totalSteps = 0,
            distanceMeters = 1609.34,
            status = WorkoutStatus.COMPLETED,
            activityType = activityType,
            phases = emptyList(),
            gpsPoints = emptyList(),
            dogName = if (activityType == ActivityType.DOG_WALK) "Juniper" else null,
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = routeTag,
        )
    }
}
