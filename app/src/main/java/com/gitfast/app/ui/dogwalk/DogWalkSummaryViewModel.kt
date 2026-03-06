package com.gitfast.app.ui.dogwalk

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WalkPhotoEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.repository.WorkoutSaveManager
import com.gitfast.app.util.DogWalkNarrativeGenerator
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class DogWalkSummaryUiState(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isDiscarded: Boolean = false,
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
    val activityType: ActivityType = ActivityType.DOG_WALK,
    val sprintCount: Int = 0,
    val totalSprintTimeFormatted: String? = null,
    val longestSprintTimeFormatted: String? = null,
    val avgSprintTimeFormatted: String? = null,
    // Dog walk events
    val events: List<DogWalkEvent> = emptyList(),
    val narrative: String? = null,
    val walkStartTimeMillis: Long = 0L,
    val gpsPoints: List<GpsPoint> = emptyList(),
    // Photos
    val photos: List<WalkPhoto> = emptyList(),
)

@HiltViewModel
class DogWalkSummaryViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val workoutSaveManager: WorkoutSaveManager,
) : AndroidViewModel(application) {

    val workoutId: String = checkNotNull(savedStateHandle["workoutId"])
    private val preSelectedRouteTag: String? = savedStateHandle.get<String>("routeTag")?.ifEmpty { null }

    private val _uiState = MutableStateFlow(DogWalkSummaryUiState())
    val uiState: StateFlow<DogWalkSummaryUiState> = _uiState.asStateFlow()

    companion object {
        private val DEFAULT_ROUTE_TAGS = listOf("Park", "Neighborhood", "City")
    }

    init {
        // Load route tags, merging with defaults
        viewModelScope.launch {
            val dbTags = workoutRepository.getAllRouteTags().map { it.name }
            val merged = DEFAULT_ROUTE_TAGS + dbTags.filter { it !in DEFAULT_ROUTE_TAGS }
            _uiState.value = _uiState.value.copy(
                routeTags = merged,
                selectedRouteTag = preSelectedRouteTag,
            )
        }

        // Load workout stats from DB
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutWithDetails(workoutId)
            workout?.let {
                val durationSeconds = it.durationMillis?.let { d -> (d / 1000).toInt() }
                val durationMinutes = durationSeconds?.let { s -> s / 60 } ?: 0

                // Compute sprint stats from WARMUP phase laps (sprint intervals)
                val warmupPhase = it.phases.find { p -> p.type == com.gitfast.app.data.model.PhaseType.WARMUP }
                val sprintLaps = warmupPhase?.laps ?: emptyList()
                val sprintDurations = sprintLaps.mapNotNull { lap -> lap.durationMillis?.let { d -> (d / 1000).toInt() } }

                // Load dog walk events
                val events = workoutRepository.getDogWalkEventsForWorkout(workoutId)
                val narrative = DogWalkNarrativeGenerator.generateNarrative(events, durationMinutes)

                _uiState.value = _uiState.value.copy(
                    timeFormatted = durationSeconds?.let { s -> formatElapsedTime(s) } ?: "--:--",
                    distanceFormatted = formatDistance(it.distanceMeters),
                    paceFormatted = it.averagePaceSecondsPerMile?.let { p -> formatPace(p.toInt()) } ?: "-- /mi",
                    activityType = it.activityType,
                    sprintCount = sprintDurations.size,
                    totalSprintTimeFormatted = if (sprintDurations.isNotEmpty()) formatElapsedTime(sprintDurations.sum()) else null,
                    longestSprintTimeFormatted = sprintDurations.maxOrNull()?.let { s -> formatElapsedTime(s) },
                    avgSprintTimeFormatted = if (sprintDurations.isNotEmpty()) formatElapsedTime(sprintDurations.average().toInt()) else null,
                    events = events,
                    narrative = narrative,
                    walkStartTimeMillis = it.startTime.toEpochMilli(),
                    gpsPoints = it.gpsPoints,
                )
            }
        }
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

            // Save or touch route tag (REPLACE upserts: inserts if new, updates if exists)
            state.selectedRouteTag?.let { tag ->
                workoutRepository.saveRouteTag(
                    RouteTagEntity(
                        name = tag,
                        createdAt = System.currentTimeMillis(),
                        lastUsed = System.currentTimeMillis()
                    )
                )
            }

            // Update workout with metadata
            workoutSaveManager.updateDogWalkMetadata(
                workoutId = workoutId,
                dogName = null,
                routeTag = state.selectedRouteTag,
                weatherCondition = state.weatherCondition,
                weatherTemp = state.weatherTemp,
                energyLevel = state.energyLevel,
                notes = state.notes.ifEmpty { null },
                narrativeDescription = state.narrative
            )

            _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
        }
    }

    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            val photoDir = File(getApplication<Application>().filesDir, "walk_photos/$workoutId")
            photoDir.mkdirs()
            val photoId = UUID.randomUUID().toString()
            val destFile = File(photoDir, "$photoId.jpg")

            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val entity = WalkPhotoEntity(
                    id = photoId,
                    workoutId = workoutId,
                    filePath = destFile.absolutePath,
                    createdAt = System.currentTimeMillis(),
                )
                workoutRepository.insertWalkPhoto(entity)

                _uiState.value = _uiState.value.copy(
                    photos = _uiState.value.photos + WalkPhoto(
                        id = photoId,
                        filePath = destFile.absolutePath,
                    )
                )
            } catch (e: Exception) {
                // If copy fails, clean up partial file
                destFile.delete()
            }
        }
    }

    fun removePhoto(photoId: String) {
        viewModelScope.launch {
            val photo = _uiState.value.photos.find { it.id == photoId } ?: return@launch
            File(photo.filePath).delete()
            workoutRepository.deleteWalkPhoto(photoId)
            _uiState.value = _uiState.value.copy(
                photos = _uiState.value.photos.filter { it.id != photoId }
            )
        }
    }

    fun discardWalk() {
        viewModelScope.launch {
            // Clean up photo files
            val photoDir = File(getApplication<Application>().filesDir, "walk_photos/$workoutId")
            photoDir.deleteRecursively()
            workoutRepository.deleteWorkout(workoutId)
            _uiState.value = _uiState.value.copy(isDiscarded = true)
        }
    }
}
