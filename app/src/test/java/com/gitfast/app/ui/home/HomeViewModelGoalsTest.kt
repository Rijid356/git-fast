package com.gitfast.app.ui.home

import com.gitfast.app.data.local.WorkoutStateStore
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.repository.BodyCompRepository
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.SorenessRepository
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
    private lateinit var bodyCompRepository: BodyCompRepository
    private lateinit var workoutStateStore: WorkoutStateStore
    private lateinit var sorenessRepository: SorenessRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        workoutRepository = mockk()
        characterRepository = mockk()
        bodyCompRepository = mockk()
        workoutStateStore = mockk()
        sorenessRepository = mockk()

        every { bodyCompRepository.getLatestReading() } returns flowOf(null)
        every { sorenessRepository.observeTodayLog() } returns flowOf(null)

        every { workoutStateStore.hasActiveWorkout() } returns false
        every { characterRepository.getProfile() } returns flowOf(CharacterProfile())
        every { characterRepository.getXpByWorkout() } returns flowOf(emptyMap())
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())
        every { workoutRepository.getCompletedWorkoutsByType(any()) } returns flowOf(emptyList())
        every { workoutRepository.getWeeklyActiveMillis() } returns flowOf(0L)
        every { workoutRepository.getWeeklyDistanceMeters() } returns flowOf(0.0)
        every { workoutRepository.getWeeklyWorkoutCount() } returns flowOf(0)
        every { workoutRepository.getPreviousWeekActiveMillis() } returns flowOf(0L)
        every { workoutRepository.getPreviousWeekDistanceMeters() } returns flowOf(0.0)
        every { workoutRepository.getPreviousWeekWorkoutCount() } returns flowOf(0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel = HomeViewModel(
        workoutStateStore = workoutStateStore,
        workoutRepository = workoutRepository,
        characterRepository = characterRepository,
        bodyCompRepository = bodyCompRepository,
        sorenessRepository = sorenessRepository,
    )

    @Test
    fun `daily metrics with no workouts shows zero progress`() = runTest {
        every { workoutRepository.getTodaysActiveMillis() } returns flowOf(0L)
        every { workoutRepository.getTodaysDistanceMeters() } returns flowOf(0.0)
        every { workoutRepository.getWeeklyActiveDayCount() } returns flowOf(0)

        val viewModel = createViewModel()

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

        val viewModel = createViewModel()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.dailyMetrics.collect {}
        }

        val metrics = viewModel.dailyMetrics.value
        assertEquals(22, metrics.activeMinutes)
        assertEquals(1.5, metrics.distanceMiles, 0.01)
        assertEquals(3, metrics.activeDaysThisWeek)
    }

    @Test
    fun `daily metrics uses hardcoded default goals`() = runTest {
        every { workoutRepository.getTodaysActiveMillis() } returns flowOf(0L)
        every { workoutRepository.getTodaysDistanceMeters() } returns flowOf(0.0)
        every { workoutRepository.getWeeklyActiveDayCount() } returns flowOf(0)

        val viewModel = createViewModel()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.dailyMetrics.collect {}
        }

        val metrics = viewModel.dailyMetrics.value
        assertEquals(22, metrics.activeMinutesGoal)
        assertEquals(1.5, metrics.distanceGoal, 0.001)
        assertEquals(5, metrics.activeDaysGoal)
    }
}
