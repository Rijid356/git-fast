package com.gitfast.app

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.ui.analytics.AnalyticsHubViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsHubViewModelTest {

    private lateinit var workoutRepository: WorkoutRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        workoutRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty workout history shows all zeros`() = runTest {
        coEvery { workoutRepository.getCompletedWorkoutCount() } returns 0
        coEvery { workoutRepository.getTotalDistanceMeters() } returns 0.0
        coEvery { workoutRepository.getTotalDurationMillis() } returns 0L
        coEvery { workoutRepository.getAllCompletedWorkoutsOnce() } returns emptyList()

        val viewModel = AnalyticsHubViewModel(workoutRepository)
        val stats = viewModel.stats.value

        assertEquals(0, stats.totalWorkouts)
        assertEquals("0.0 mi", stats.totalDistanceFormatted)
        assertEquals("0h 0m", stats.totalDurationFormatted)
        assertEquals(0, stats.bestStreak)
    }

    @Test
    fun `total workouts count loads correctly`() = runTest {
        coEvery { workoutRepository.getCompletedWorkoutCount() } returns 47
        coEvery { workoutRepository.getTotalDistanceMeters() } returns 0.0
        coEvery { workoutRepository.getTotalDurationMillis() } returns 0L
        coEvery { workoutRepository.getAllCompletedWorkoutsOnce() } returns emptyList()

        val viewModel = AnalyticsHubViewModel(workoutRepository)

        assertEquals(47, viewModel.stats.value.totalWorkouts)
    }

    @Test
    fun `total distance converts meters to miles`() = runTest {
        // 5 miles = 8046.72 meters
        coEvery { workoutRepository.getCompletedWorkoutCount() } returns 1
        coEvery { workoutRepository.getTotalDistanceMeters() } returns 8046.72
        coEvery { workoutRepository.getTotalDurationMillis() } returns 0L
        coEvery { workoutRepository.getAllCompletedWorkoutsOnce() } returns emptyList()

        val viewModel = AnalyticsHubViewModel(workoutRepository)

        assertEquals("5.0 mi", viewModel.stats.value.totalDistanceFormatted)
    }

    @Test
    fun `total distance shows integer for 100+ miles`() = runTest {
        // ~161 km = 100 miles
        coEvery { workoutRepository.getCompletedWorkoutCount() } returns 50
        coEvery { workoutRepository.getTotalDistanceMeters() } returns 160934.0
        coEvery { workoutRepository.getTotalDurationMillis() } returns 0L
        coEvery { workoutRepository.getAllCompletedWorkoutsOnce() } returns emptyList()

        val viewModel = AnalyticsHubViewModel(workoutRepository)

        assertEquals("100 mi", viewModel.stats.value.totalDistanceFormatted)
    }

    @Test
    fun `total duration formats hours and minutes`() = runTest {
        // 2h 30m = 9_000_000 ms
        coEvery { workoutRepository.getCompletedWorkoutCount() } returns 1
        coEvery { workoutRepository.getTotalDistanceMeters() } returns 0.0
        coEvery { workoutRepository.getTotalDurationMillis() } returns 9_000_000L
        coEvery { workoutRepository.getAllCompletedWorkoutsOnce() } returns emptyList()

        val viewModel = AnalyticsHubViewModel(workoutRepository)

        assertEquals("2h 30m", viewModel.stats.value.totalDurationFormatted)
    }

    @Test
    fun `best streak calculates from workout history`() = runTest {
        val zone = ZoneId.systemDefault()
        val baseDate = LocalDate.of(2026, 2, 10)
        // 3-day streak
        val workouts = (0L..2L).map { dayOffset ->
            val date = baseDate.plusDays(dayOffset)
            val instant = date.atStartOfDay(zone).toInstant().plusSeconds(3600)
            Workout(
                id = "w-$dayOffset",
                startTime = instant,
                endTime = instant.plusSeconds(1800),
                totalSteps = 0,
                distanceMeters = 3000.0,
                status = WorkoutStatus.COMPLETED,
                activityType = ActivityType.RUN,
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

        coEvery { workoutRepository.getCompletedWorkoutCount() } returns 3
        coEvery { workoutRepository.getTotalDistanceMeters() } returns 9000.0
        coEvery { workoutRepository.getTotalDurationMillis() } returns 5_400_000L
        coEvery { workoutRepository.getAllCompletedWorkoutsOnce() } returns workouts

        val viewModel = AnalyticsHubViewModel(workoutRepository)

        assertEquals(3, viewModel.stats.value.bestStreak)
    }
}
