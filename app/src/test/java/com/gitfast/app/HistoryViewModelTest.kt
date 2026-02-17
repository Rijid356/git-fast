package com.gitfast.app

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.ui.components.ActivityFilter
import com.gitfast.app.ui.history.HistoryUiState
import com.gitfast.app.ui.history.HistoryViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class HistoryViewModelTest {

    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var characterRepository: CharacterRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        workoutRepository = mockk()
        characterRepository = mockk()
        every { characterRepository.getXpByWorkout() } returns flowOf(emptyMap())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial filter is ALL`() {
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())

        val viewModel = HistoryViewModel(workoutRepository, characterRepository)

        assertEquals(ActivityFilter.ALL, viewModel.filter.value)
    }

    @Test
    fun `workouts emits Empty when no completed workouts`() = runTest {
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())

        val viewModel = HistoryViewModel(workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.workouts.collect {}
        }

        assertEquals(HistoryUiState.Empty, viewModel.workouts.value)
    }

    @Test
    fun `workouts emits Loaded with grouped workouts`() = runTest {
        val workouts = listOf(createTestWorkout("w1"))
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(workouts)

        val viewModel = HistoryViewModel(workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.workouts.collect {}
        }

        val state = viewModel.workouts.value
        assertTrue(state is HistoryUiState.Loaded)
        assertEquals(1, (state as HistoryUiState.Loaded).groupedWorkouts.values.flatten().size)
    }

    @Test
    fun `setFilter to RUNS queries run workouts`() = runTest {
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())
        val runs = listOf(createTestWorkout("r1", ActivityType.RUN))
        every { workoutRepository.getCompletedWorkoutsByType(ActivityType.RUN) } returns flowOf(runs)

        val viewModel = HistoryViewModel(workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.workouts.collect {}
        }

        viewModel.setFilter(ActivityFilter.RUNS)

        assertEquals(ActivityFilter.RUNS, viewModel.filter.value)
        val state = viewModel.workouts.value
        assertTrue(state is HistoryUiState.Loaded)
    }

    @Test
    fun `setFilter to WALKS queries dog walk workouts`() = runTest {
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())
        val walks = listOf(createTestWorkout("dw1", ActivityType.DOG_WALK))
        every { workoutRepository.getCompletedWorkoutsByType(ActivityType.DOG_WALK) } returns flowOf(walks)

        val viewModel = HistoryViewModel(workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.workouts.collect {}
        }

        viewModel.setFilter(ActivityFilter.WALKS)

        assertEquals(ActivityFilter.WALKS, viewModel.filter.value)
        val state = viewModel.workouts.value
        assertTrue(state is HistoryUiState.Loaded)
    }

    @Test
    fun `workouts includes xp from xpByWorkout map`() = runTest {
        val workouts = listOf(createTestWorkout("w1"))
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(workouts)
        every { characterRepository.getXpByWorkout() } returns flowOf(mapOf("w1" to 100))

        val viewModel = HistoryViewModel(workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.workouts.collect {}
        }

        val state = viewModel.workouts.value as HistoryUiState.Loaded
        val item = state.groupedWorkouts.values.flatten().first()
        assertEquals(100, item.xpEarned)
    }

    @Test
    fun `switching filter back to ALL re-queries all workouts`() = runTest {
        val allWorkouts = listOf(
            createTestWorkout("w1"),
            createTestWorkout("w2", ActivityType.DOG_WALK),
        )
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(allWorkouts)
        every { workoutRepository.getCompletedWorkoutsByType(ActivityType.RUN) } returns flowOf(emptyList())

        val viewModel = HistoryViewModel(workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.workouts.collect {}
        }

        viewModel.setFilter(ActivityFilter.RUNS)
        viewModel.setFilter(ActivityFilter.ALL)

        val state = viewModel.workouts.value
        assertTrue(state is HistoryUiState.Loaded)
        assertEquals(2, (state as HistoryUiState.Loaded).groupedWorkouts.values.flatten().size)
    }

    private fun createTestWorkout(id: String, type: ActivityType = ActivityType.RUN): Workout {
        return Workout(
            id = id,
            startTime = Instant.ofEpochMilli(1000),
            endTime = Instant.ofEpochMilli(5000),
            totalSteps = 0,
            distanceMeters = 1609.34,
            status = WorkoutStatus.COMPLETED,
            activityType = type,
            phases = emptyList(),
            gpsPoints = emptyList(),
            dogName = null,
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = null,
        )
    }
}
