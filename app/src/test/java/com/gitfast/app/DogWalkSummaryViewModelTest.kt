package com.gitfast.app

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.DogWalkEventType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.Lap
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutPhase
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.repository.WorkoutSaveManager
import com.gitfast.app.ui.dogwalk.DogWalkSummaryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DogWalkSummaryViewModelTest {

    private lateinit var application: Application
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var workoutSaveManager: WorkoutSaveManager

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        application = ApplicationProvider.getApplicationContext()
        workoutRepository = mockk(relaxed = true)
        workoutSaveManager = mockk(relaxed = true)

        coEvery { workoutRepository.getAllRouteTags() } returns emptyList()
        coEvery { workoutRepository.getWorkoutWithDetails(any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(workoutId: String = "w1"): DogWalkSummaryViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to workoutId))
        return DogWalkSummaryViewModel(application, savedStateHandle, workoutRepository, workoutSaveManager)
    }

    @Test
    fun `init seeds default route tags`() {
        val vm = createViewModel()
        val tags = vm.uiState.value.routeTags
        assertTrue(tags.contains("Park"))
        assertTrue(tags.contains("Neighborhood"))
        assertTrue(tags.contains("City"))
    }

    @Test
    fun `workoutId is extracted from savedStateHandle`() {
        val vm = createViewModel("test-123")
        assertEquals("test-123", vm.workoutId)
    }

    @Test
    fun `selectRouteTag updates selected tag and clears creating state`() {
        val vm = createViewModel()
        vm.startCreatingNewTag()
        assertTrue(vm.uiState.value.isCreatingNewTag)

        vm.selectRouteTag("Park Loop")
        assertEquals("Park Loop", vm.uiState.value.selectedRouteTag)
        assertFalse(vm.uiState.value.isCreatingNewTag)
    }

    @Test
    fun `selectRouteTag with null clears selection`() {
        val vm = createViewModel()
        vm.selectRouteTag("Park Loop")
        vm.selectRouteTag(null)
        assertNull(vm.uiState.value.selectedRouteTag)
    }

    @Test
    fun `startCreatingNewTag enables creation mode with empty name`() {
        val vm = createViewModel()
        vm.startCreatingNewTag()
        assertTrue(vm.uiState.value.isCreatingNewTag)
        assertEquals("", vm.uiState.value.newTagName)
    }

    @Test
    fun `updateNewTagName updates tag name`() {
        val vm = createViewModel()
        vm.startCreatingNewTag()
        vm.updateNewTagName("River Trail")
        assertEquals("River Trail", vm.uiState.value.newTagName)
    }

    @Test
    fun `confirmNewTag adds tag and selects it`() {
        val vm = createViewModel()
        vm.startCreatingNewTag()
        vm.updateNewTagName("River Trail")
        vm.confirmNewTag()

        assertEquals("River Trail", vm.uiState.value.selectedRouteTag)
        assertFalse(vm.uiState.value.isCreatingNewTag)
        assertTrue(vm.uiState.value.routeTags.contains("River Trail"))
    }

    @Test
    fun `confirmNewTag with blank name does nothing`() {
        val vm = createViewModel()
        vm.startCreatingNewTag()
        vm.updateNewTagName("   ")
        vm.confirmNewTag()

        assertNull(vm.uiState.value.selectedRouteTag)
        assertTrue(vm.uiState.value.isCreatingNewTag)
    }

    @Test
    fun `selectWeatherCondition sets condition`() {
        val vm = createViewModel()
        vm.selectWeatherCondition(WeatherCondition.SUNNY)
        assertEquals(WeatherCondition.SUNNY, vm.uiState.value.weatherCondition)
    }

    @Test
    fun `selectWeatherCondition toggles off same value`() {
        val vm = createViewModel()
        vm.selectWeatherCondition(WeatherCondition.SUNNY)
        vm.selectWeatherCondition(WeatherCondition.SUNNY)
        assertNull(vm.uiState.value.weatherCondition)
    }

    @Test
    fun `selectWeatherCondition switches to different value`() {
        val vm = createViewModel()
        vm.selectWeatherCondition(WeatherCondition.SUNNY)
        vm.selectWeatherCondition(WeatherCondition.RAINY)
        assertEquals(WeatherCondition.RAINY, vm.uiState.value.weatherCondition)
    }

    @Test
    fun `selectWeatherTemp sets temp`() {
        val vm = createViewModel()
        vm.selectWeatherTemp(WeatherTemp.WARM)
        assertEquals(WeatherTemp.WARM, vm.uiState.value.weatherTemp)
    }

    @Test
    fun `selectWeatherTemp toggles off same value`() {
        val vm = createViewModel()
        vm.selectWeatherTemp(WeatherTemp.WARM)
        vm.selectWeatherTemp(WeatherTemp.WARM)
        assertNull(vm.uiState.value.weatherTemp)
    }

    @Test
    fun `selectEnergyLevel sets level`() {
        val vm = createViewModel()
        vm.selectEnergyLevel(EnergyLevel.HYPER)
        assertEquals(EnergyLevel.HYPER, vm.uiState.value.energyLevel)
    }

    @Test
    fun `selectEnergyLevel toggles off same value`() {
        val vm = createViewModel()
        vm.selectEnergyLevel(EnergyLevel.HYPER)
        vm.selectEnergyLevel(EnergyLevel.HYPER)
        assertNull(vm.uiState.value.energyLevel)
    }

    @Test
    fun `updateNotes updates notes`() {
        val vm = createViewModel()
        vm.updateNotes("Great walk today!")
        assertEquals("Great walk today!", vm.uiState.value.notes)
    }

    @Test
    fun `saveWalk saves metadata without dogName and marks saved`() {
        val vm = createViewModel()
        vm.saveWalk()

        assertTrue(vm.uiState.value.isSaved)
        assertFalse(vm.uiState.value.isSaving)
        coVerify {
            workoutSaveManager.updateDogWalkMetadata(
                workoutId = "w1",
                dogName = null,
                routeTag = null,
                weatherCondition = null,
                weatherTemp = null,
                energyLevel = null,
                notes = null,
            )
        }
    }

    @Test
    fun `discardWalk deletes workout and marks discarded`() {
        val vm = createViewModel()
        vm.discardWalk()

        assertTrue(vm.uiState.value.isDiscarded)
        coVerify { workoutRepository.deleteWorkout("w1") }
    }

    // --- New tests for init loading, sprint stats, events, save with metadata ---

    private fun createTestWorkout(
        id: String = "w1",
        distanceMeters: Double = 1609.34,
        durationMillis: Long = 1800_000L,
        activityType: ActivityType = ActivityType.DOG_WALK,
        phases: List<WorkoutPhase> = emptyList(),
    ): Workout {
        val start = Instant.ofEpochMilli(1_000_000L)
        val end = Instant.ofEpochMilli(1_000_000L + durationMillis)
        return Workout(
            id = id,
            startTime = start,
            endTime = end,
            totalSteps = 0,
            distanceMeters = distanceMeters,
            status = WorkoutStatus.COMPLETED,
            activityType = activityType,
            phases = phases,
            gpsPoints = emptyList(),
            dogName = null,
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = null,
        )
    }

    @Test
    fun `init loads route tags merged with defaults`() {
        coEvery { workoutRepository.getAllRouteTags() } returns listOf(
            RouteTagEntity(name = "River Trail", createdAt = 0, lastUsed = 0),
            RouteTagEntity(name = "Park", createdAt = 0, lastUsed = 0), // duplicate of default
        )

        val vm = createViewModel()
        val tags = vm.uiState.value.routeTags

        assertTrue(tags.contains("Park"))
        assertTrue(tags.contains("Neighborhood"))
        assertTrue(tags.contains("City"))
        assertTrue(tags.contains("River Trail"))
        // "Park" should not be duplicated
        assertEquals(1, tags.count { it == "Park" })
    }

    @Test
    fun `init loads workout stats`() {
        val workout = createTestWorkout(
            distanceMeters = 3218.69, // ~2 miles
            durationMillis = 2400_000L, // 40 minutes
        )
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { workoutRepository.getDogWalkEventsForWorkout("w1") } returns emptyList()

        val vm = createViewModel()
        val state = vm.uiState.value

        // Verify time is formatted (40:00)
        assertTrue(state.timeFormatted != "--:--")
        // Verify distance is formatted
        assertTrue(state.distanceFormatted != "0.00 mi")
        // Verify pace is formatted
        assertTrue(state.paceFormatted != "-- /mi")
    }

    @Test
    fun `init computes sprint stats from warmup phase laps`() {
        val laps = listOf(
            Lap("l1", 1, Instant.ofEpochMilli(0), Instant.ofEpochMilli(10_000), 50.0, 0),
            Lap("l2", 2, Instant.ofEpochMilli(10_000), Instant.ofEpochMilli(25_000), 80.0, 0),
            Lap("l3", 3, Instant.ofEpochMilli(25_000), Instant.ofEpochMilli(32_000), 40.0, 0),
        )
        val warmupPhase = WorkoutPhase("p1", PhaseType.WARMUP, Instant.ofEpochMilli(0), Instant.ofEpochMilli(32_000), 170.0, 0, laps)
        val workout = createTestWorkout(phases = listOf(warmupPhase))
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { workoutRepository.getDogWalkEventsForWorkout("w1") } returns emptyList()

        val vm = createViewModel()
        val state = vm.uiState.value

        assertEquals(3, state.sprintCount)
        assertTrue(state.totalSprintTimeFormatted != null)
        assertTrue(state.longestSprintTimeFormatted != null)
        assertTrue(state.avgSprintTimeFormatted != null)
    }

    @Test
    fun `init loads dog walk events and narrative`() {
        val events = listOf(
            DogWalkEvent("e1", "w1", DogWalkEventType.POOP, Instant.ofEpochMilli(500_000), null, null),
            DogWalkEvent("e2", "w1", DogWalkEventType.SQUIRREL_CHASE, Instant.ofEpochMilli(600_000), null, null),
        )
        val workout = createTestWorkout()
        coEvery { workoutRepository.getWorkoutWithDetails("w1") } returns workout
        coEvery { workoutRepository.getDogWalkEventsForWorkout("w1") } returns events

        val vm = createViewModel()
        val state = vm.uiState.value

        assertEquals(2, state.events.size)
        assertTrue(state.narrative != null)
    }

    @Test
    fun `saveWalk saves route tag and metadata with all fields`() {
        val vm = createViewModel()
        vm.selectRouteTag("Park")
        vm.selectWeatherCondition(WeatherCondition.SUNNY)
        vm.selectWeatherTemp(WeatherTemp.WARM)
        vm.selectEnergyLevel(EnergyLevel.HYPER)
        vm.updateNotes("Great walk!")

        vm.saveWalk()

        assertTrue(vm.uiState.value.isSaved)
        coVerify {
            workoutRepository.saveRouteTag(match { it.name == "Park" })
        }
        coVerify {
            workoutSaveManager.updateDogWalkMetadata(
                workoutId = "w1",
                dogName = null,
                routeTag = "Park",
                weatherCondition = WeatherCondition.SUNNY,
                weatherTemp = WeatherTemp.WARM,
                energyLevel = EnergyLevel.HYPER,
                notes = "Great walk!",
            )
        }
    }

    @Test
    fun `saveWalk with empty notes passes null`() {
        val vm = createViewModel()
        vm.updateNotes("")

        vm.saveWalk()

        coVerify {
            workoutSaveManager.updateDogWalkMetadata(
                workoutId = "w1",
                dogName = null,
                routeTag = null,
                weatherCondition = null,
                weatherTemp = null,
                energyLevel = null,
                notes = null,
            )
        }
    }
}
