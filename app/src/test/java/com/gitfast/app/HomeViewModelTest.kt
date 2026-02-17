package com.gitfast.app

import com.gitfast.app.data.local.WorkoutStateStore
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.service.WorkoutService
import com.gitfast.app.ui.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var workoutStateStore: WorkoutStateStore
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var characterRepository: CharacterRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        workoutStateStore = mockk(relaxed = true)
        workoutRepository = mockk()
        characterRepository = mockk()

        every { workoutStateStore.hasActiveWorkout() } returns false
        every { workoutRepository.getCompletedWorkoutsByType(any()) } returns flowOf(emptyList())
        every { characterRepository.getProfile() } returns flowOf(CharacterProfile())
        every { characterRepository.getXpByWorkout() } returns flowOf(emptyMap())
        WorkoutService.isRunning = false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        WorkoutService.isRunning = false
    }

    @Test
    fun `no recovery dialog when no active workout`() {
        val viewModel = HomeViewModel(workoutStateStore, workoutRepository, characterRepository)
        assertFalse(viewModel.showRecoveryDialog.value)
    }

    @Test
    fun `shows recovery dialog when active workout and service not running`() {
        every { workoutStateStore.hasActiveWorkout() } returns true
        WorkoutService.isRunning = false

        val viewModel = HomeViewModel(workoutStateStore, workoutRepository, characterRepository)
        assertTrue(viewModel.showRecoveryDialog.value)
    }

    @Test
    fun `hides recovery dialog when service is already running`() {
        every { workoutStateStore.hasActiveWorkout() } returns true
        WorkoutService.isRunning = true

        val viewModel = HomeViewModel(workoutStateStore, workoutRepository, characterRepository)
        assertFalse(viewModel.showRecoveryDialog.value)
    }

    @Test
    fun `dismissRecoveryDialog clears store and hides dialog`() {
        every { workoutStateStore.hasActiveWorkout() } returns true
        WorkoutService.isRunning = false

        val viewModel = HomeViewModel(workoutStateStore, workoutRepository, characterRepository)
        assertTrue(viewModel.showRecoveryDialog.value)

        viewModel.dismissRecoveryDialog()

        verify { workoutStateStore.clearActiveWorkout() }
        assertFalse(viewModel.showRecoveryDialog.value)
    }

    @Test
    fun `recentRuns emits empty list when no workouts`() = runTest {
        val viewModel = HomeViewModel(workoutStateStore, workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.recentRuns.collect {}
        }

        assertEquals(emptyList<Any>(), viewModel.recentRuns.value)
    }

    @Test
    fun `recentRuns limits to 3 items`() = runTest {
        val workouts = (1..5).map { createTestWorkout("w$it", ActivityType.RUN) }
        every { workoutRepository.getCompletedWorkoutsByType(ActivityType.RUN) } returns flowOf(workouts)

        val viewModel = HomeViewModel(workoutStateStore, workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.recentRuns.collect {}
        }

        assertEquals(3, viewModel.recentRuns.value.size)
    }

    @Test
    fun `recentRuns includes xp from xpByWorkout map`() = runTest {
        val workouts = listOf(createTestWorkout("w1", ActivityType.RUN))
        every { workoutRepository.getCompletedWorkoutsByType(ActivityType.RUN) } returns flowOf(workouts)
        every { characterRepository.getXpByWorkout() } returns flowOf(mapOf("w1" to 75))

        val viewModel = HomeViewModel(workoutStateStore, workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.recentRuns.collect {}
        }

        assertEquals(75, viewModel.recentRuns.value.first().xpEarned)
    }

    @Test
    fun `recentDogWalks emits dog walk workouts`() = runTest {
        val walks = listOf(createTestWorkout("dw1", ActivityType.DOG_WALK))
        every { workoutRepository.getCompletedWorkoutsByType(ActivityType.DOG_WALK) } returns flowOf(walks)

        val viewModel = HomeViewModel(workoutStateStore, workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.recentDogWalks.collect {}
        }

        assertEquals(1, viewModel.recentDogWalks.value.size)
        assertEquals("dw1", viewModel.recentDogWalks.value.first().workoutId)
    }

    @Test
    fun `characterProfile emits from repository`() = runTest {
        val profile = CharacterProfile(level = 3, totalXp = 250)
        every { characterRepository.getProfile() } returns flowOf(profile)

        val viewModel = HomeViewModel(workoutStateStore, workoutRepository, characterRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.characterProfile.collect {}
        }

        assertEquals(3, viewModel.characterProfile.value.level)
    }

    private fun createTestWorkout(id: String, type: ActivityType): Workout {
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
