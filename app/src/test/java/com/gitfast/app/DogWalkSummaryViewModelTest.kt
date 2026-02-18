package com.gitfast.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.repository.WorkoutSaveManager
import com.gitfast.app.ui.dogwalk.DogWalkSummaryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
    fun `init loads default dog name from prefs`() {
        val vm = createViewModel()
        assertEquals("Juniper", vm.uiState.value.dogName)
    }

    @Test
    fun `init loads saved dog name from prefs`() {
        val prefs = application.getSharedPreferences("dog_walk_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_dog_name", "Buddy").commit()

        val vm = createViewModel()
        assertEquals("Buddy", vm.uiState.value.dogName)
    }

    @Test
    fun `workoutId is extracted from savedStateHandle`() {
        val vm = createViewModel("test-123")
        assertEquals("test-123", vm.workoutId)
    }

    @Test
    fun `updateDogName updates state`() {
        val vm = createViewModel()
        vm.updateDogName("Rex")
        assertEquals("Rex", vm.uiState.value.dogName)
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
    fun `saveWalk saves metadata and marks saved`() {
        val vm = createViewModel()
        vm.updateDogName("Buddy")
        vm.saveWalk()

        assertTrue(vm.uiState.value.isSaved)
        assertFalse(vm.uiState.value.isSaving)
        coVerify {
            workoutSaveManager.updateDogWalkMetadata(
                workoutId = "w1",
                dogName = "Buddy",
                routeTag = null,
                weatherCondition = null,
                weatherTemp = null,
                energyLevel = null,
                notes = null,
            )
        }
    }

    @Test
    fun `saveWalk persists dog name to prefs`() {
        val vm = createViewModel()
        vm.updateDogName("Fido")
        vm.saveWalk()

        val prefs = application.getSharedPreferences("dog_walk_prefs", Context.MODE_PRIVATE)
        assertEquals("Fido", prefs.getString("last_dog_name", null))
    }

    @Test
    fun `saveWalk with empty dog name passes null`() {
        val vm = createViewModel()
        vm.updateDogName("")
        vm.saveWalk()

        coVerify {
            workoutSaveManager.updateDogWalkMetadata(
                workoutId = "w1",
                dogName = null,
                routeTag = any(),
                weatherCondition = any(),
                weatherTemp = any(),
                energyLevel = any(),
                notes = any(),
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
}
