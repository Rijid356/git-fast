package com.gitfast.app.ui.home

import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.local.WorkoutStateStore
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelGoalsTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var characterRepository: CharacterRepository
    private lateinit var workoutStateStore: WorkoutStateStore
    private lateinit var settingsStore: SettingsStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        workoutRepository = mockk()
        characterRepository = mockk()
        workoutStateStore = mockk()
        settingsStore = mockk()

        every { workoutStateStore.hasActiveWorkout() } returns false
        every { characterRepository.getProfile() } returns flowOf(CharacterProfile())
        every { characterRepository.getXpByWorkout() } returns flowOf(emptyMap())
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())
        every { workoutRepository.getCompletedWorkoutsByType(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `daily metrics with no workouts shows zero progress`() = runTest {
        every { workoutRepository.getTodaysActiveMillis() } returns flowOf(0L)
        every { workoutRepository.getTodaysDistanceMeters() } returns flowOf(0.0)
        every { workoutRepository.getWeeklyActiveDayCount() } returns flowOf(0)
        every { settingsStore.dailyActiveMinutesGoal } returns 22
        every { settingsStore.dailyDistanceGoalMiles } returns 1.5
        every { settingsStore.weeklyActiveDaysGoal } returns 5

        val viewModel = HomeViewModel(
            workoutStateStore = workoutStateStore,
            workoutRepository = workoutRepository,
            characterRepository = characterRepository,
            settingsStore = settingsStore,
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.dailyMetrics.collect {}
        }

        val metrics = viewModel.dailyMetrics.value
        assertEquals(0, metrics.activeMinutes)
        assertEquals(0.0, metrics.distanceMiles, 0.001)
        assertEquals(0, metrics.activeDaysThisWeek)
        assertEquals(22, metrics.activeMinutesGoal)
        assertEquals(1.5, metrics.distanceGoal, 0.001)
        assertEquals(5, metrics.activeDaysGoal)
    }

    @Test
    fun `daily metrics converts millis to minutes and meters to miles`() = runTest {
        every { workoutRepository.getTodaysActiveMillis() } returns flowOf(1_320_000L) // 22 minutes
        every { workoutRepository.getTodaysDistanceMeters() } returns flowOf(2414.02) // ~1.5 miles
        every { workoutRepository.getWeeklyActiveDayCount() } returns flowOf(3)
        every { settingsStore.dailyActiveMinutesGoal } returns 22
        every { settingsStore.dailyDistanceGoalMiles } returns 1.5
        every { settingsStore.weeklyActiveDaysGoal } returns 5

        val viewModel = HomeViewModel(
            workoutStateStore = workoutStateStore,
            workoutRepository = workoutRepository,
            characterRepository = characterRepository,
            settingsStore = settingsStore,
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.dailyMetrics.collect {}
        }

        val metrics = viewModel.dailyMetrics.value
        assertEquals(22, metrics.activeMinutes)
        assertEquals(1.5, metrics.distanceMiles, 0.01)
        assertEquals(3, metrics.activeDaysThisWeek)
    }

    @Test
    fun `daily metrics uses custom goal values from settings`() = runTest {
        every { workoutRepository.getTodaysActiveMillis() } returns flowOf(0L)
        every { workoutRepository.getTodaysDistanceMeters() } returns flowOf(0.0)
        every { workoutRepository.getWeeklyActiveDayCount() } returns flowOf(0)
        every { settingsStore.dailyActiveMinutesGoal } returns 45
        every { settingsStore.dailyDistanceGoalMiles } returns 3.0
        every { settingsStore.weeklyActiveDaysGoal } returns 7

        val viewModel = HomeViewModel(
            workoutStateStore = workoutStateStore,
            workoutRepository = workoutRepository,
            characterRepository = characterRepository,
            settingsStore = settingsStore,
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.dailyMetrics.collect {}
        }

        val metrics = viewModel.dailyMetrics.value
        assertEquals(45, metrics.activeMinutesGoal)
        assertEquals(3.0, metrics.distanceGoal, 0.001)
        assertEquals(7, metrics.activeDaysGoal)
    }
}
