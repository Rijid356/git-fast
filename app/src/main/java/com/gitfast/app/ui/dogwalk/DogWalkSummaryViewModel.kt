package com.gitfast.app.ui.dogwalk

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.repository.WorkoutSaveManager
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DogWalkSummaryUiState(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDiscarded: Boolean = false,
    val dogName: String = "",
    val routeTags: List<String> = emptyList(),
    val selectedRouteTag: String? = null,
    val isCreatingNewTag: Boolean = false,
    val newTagName: String = "",
    val weatherCondition: WeatherCondition? = null,
    val weatherTemp: WeatherTemp? = null,
    val energyLevel: EnergyLevel? = null,
    val notes: String = "",
    val timeFormatted: String = "--:--",
    val distanceFormatted: String = "0.00 mi",
    val paceFormatted: String = "-- /mi",
)

@HiltViewModel
class DogWalkSummaryViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val workoutSaveManager: WorkoutSaveManager,
) : AndroidViewModel(application) {

    val workoutId: String = checkNotNull(savedStateHandle["workoutId"])

    private val _uiState = MutableStateFlow(DogWalkSummaryUiState())
    val uiState: StateFlow<DogWalkSummaryUiState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences("dog_walk_prefs", Context.MODE_PRIVATE)

    init {
        // Load last-used dog name
        val lastDogName = prefs.getString("last_dog_name", "Juniper") ?: "Juniper"
        _uiState.value = _uiState.value.copy(dogName = lastDogName)

        // Load route tags
        viewModelScope.launch {
            val tags = workoutRepository.getAllRouteTags()
            _uiState.value = _uiState.value.copy(
                routeTags = tags.map { it.name }
            )
        }

        // Load workout stats from DB
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutWithDetails(workoutId)
            workout?.let {
                val durationSeconds = it.durationMillis?.let { d -> (d / 1000).toInt() }
                _uiState.value = _uiState.value.copy(
                    timeFormatted = durationSeconds?.let { s -> formatElapsedTime(s) } ?: "--:--",
                    distanceFormatted = formatDistance(it.distanceMeters),
                    paceFormatted = it.averagePaceSecondsPerMile?.let { p -> formatPace(p.toInt()) } ?: "-- /mi",
                )
            }
        }
    }

    fun updateDogName(name: String) {
        _uiState.value = _uiState.value.copy(dogName = name)
    }

    fun selectRouteTag(tag: String?) {
        _uiState.value = _uiState.value.copy(
            selectedRouteTag = tag,
            isCreatingNewTag = false
        )
    }

    fun startCreatingNewTag() {
        _uiState.value = _uiState.value.copy(isCreatingNewTag = true, newTagName = "")
    }

    fun updateNewTagName(name: String) {
        _uiState.value = _uiState.value.copy(newTagName = name)
    }

    fun confirmNewTag() {
        val name = _uiState.value.newTagName.trim()
        if (name.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                selectedRouteTag = name,
                isCreatingNewTag = false,
                routeTags = _uiState.value.routeTags + name
            )
        }
    }

    fun selectWeatherCondition(condition: WeatherCondition?) {
        val current = _uiState.value.weatherCondition
        _uiState.value = _uiState.value.copy(
            weatherCondition = if (current == condition) null else condition
        )
    }

    fun selectWeatherTemp(temp: WeatherTemp?) {
        val current = _uiState.value.weatherTemp
        _uiState.value = _uiState.value.copy(
            weatherTemp = if (current == temp) null else temp
        )
    }

    fun selectEnergyLevel(level: EnergyLevel?) {
        val current = _uiState.value.energyLevel
        _uiState.value = _uiState.value.copy(
            energyLevel = if (current == level) null else level
        )
    }

    fun updateNotes(text: String) {
        _uiState.value = _uiState.value.copy(notes = text)
    }

    fun saveWalk() {
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            val state = _uiState.value

            // Save dog name for next time
            prefs.edit().putString("last_dog_name", state.dogName).apply()

            // Save or touch route tag
            state.selectedRouteTag?.let { tag ->
                if (tag !in state.routeTags.dropLast(if (state.routeTags.contains(tag)) 0 else 1)) {
                    // New tag
                    workoutRepository.saveRouteTag(
                        RouteTagEntity(
                            name = tag,
                            createdAt = System.currentTimeMillis(),
                            lastUsed = System.currentTimeMillis()
                        )
                    )
                } else {
                    workoutRepository.touchRouteTag(tag)
                }
            }

            // Update workout with metadata
            workoutSaveManager.updateDogWalkMetadata(
                workoutId = workoutId,
                dogName = state.dogName.ifEmpty { null },
                routeTag = state.selectedRouteTag,
                weatherCondition = state.weatherCondition,
                weatherTemp = state.weatherTemp,
                energyLevel = state.energyLevel,
                notes = state.notes.ifEmpty { null }
            )

            _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
        }
    }

    fun discardWalk() {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            _uiState.value = _uiState.value.copy(isDiscarded = true)
        }
    }
}
