package com.gitfast.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.analysis.RouteComparisonAnalyzer
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.DogWalkEvent
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.model.Lap
import com.gitfast.app.util.LapAnalyzer
import com.gitfast.app.util.PhaseAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MS_TO_MPH = 2.23694f

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val characterRepository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    init {
        loadWorkout()
    }

    private fun loadWorkout() {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutWithDetails(workoutId)
            if (workout == null) {
                _uiState.value = DetailUiState.NotFound
            } else {
                val lapsPhase = workout.phases.find { it.type == PhaseType.LAPS }
                val lapAnalysis = lapsPhase?.laps?.let { LapAnalyzer.analyze(it) }

                // Route comparison for dog activities
                val routeComparison = if (workout.activityType.isDogActivity && workout.routeTag != null) {
                    val previousWalks = workoutRepository.getDogWalksByRouteOnce(workout.routeTag!!)
                        .filter { it.id != workout.id }
                    RouteComparisonAnalyzer.compare(workout, previousWalks)
                } else {
                    emptyList()
                }

                // Sprint analysis for dog activities (sprints stored as WARMUP phase laps)
                val sprintLaps = if (workout.activityType.isDogActivity) {
                    workout.phases.find { it.type == PhaseType.WARMUP }?.laps ?: emptyList()
                } else {
                    emptyList()
                }

                // Fetch XP earned for this workout
                val xpTransaction = characterRepository.getXpTransactionForWorkout(workoutId)

                // Build speed chart data from GPS points
                val workoutStartMs = workout.startTime.toEpochMilli()
                val speedPoints = workout.gpsPoints
                    .filter { it.speed != null }
                    .map { point ->
                        val elapsedMinutes = (point.timestamp.toEpochMilli() - workoutStartMs) / 60_000f
                        val speedMph = point.speed!! * MS_TO_MPH
                        SpeedChartPoint(elapsedMinutes, speedMph)
                    }
                val avgSpeed = if (speedPoints.isNotEmpty()) {
                    speedPoints.map { it.speedMph }.average().toFloat()
                } else 0f
                val maxSpeed = if (speedPoints.isNotEmpty()) {
                    speedPoints.maxOf { it.speedMph }
                } else 0f

                // Load dog walk events
                val dogWalkEvents = if (workout.activityType.isDogActivity) {
                    workoutRepository.getDogWalkEventsForWorkout(workoutId)
                } else {
                    emptyList()
                }

                _uiState.value = DetailUiState.Loaded(
                    detail = workout.toDetailItem().copy(
                        xpEarned = xpTransaction?.xpAmount ?: 0,
                        xpBreakdown = xpTransaction?.reason,
                    ),
                    phases = PhaseAnalyzer.analyzePhases(workout.phases),
                    lapAnalysis = lapAnalysis,
                    routeComparison = routeComparison,
                    speedChartPoints = speedPoints,
                    averageSpeedMph = avgSpeed,
                    maxSpeedMph = maxSpeed,
                    sprintLaps = sprintLaps,
                    dogWalkEvents = dogWalkEvents,
                )
            }
        }
    }

    fun deleteLap(lapId: String) {
        viewModelScope.launch {
            workoutRepository.deleteLap(lapId)
            loadWorkout()
        }
    }

    fun deleteWorkout() {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            _uiState.value = DetailUiState.Deleted
        }
    }
}

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data object NotFound : DetailUiState()
    data object Deleted : DetailUiState()
    data class Loaded(
        val detail: WorkoutDetailItem,
        val phases: List<PhaseAnalyzer.PhaseDisplayItem>,
        val lapAnalysis: LapAnalysis?,
        val routeComparison: List<RouteComparisonAnalyzer.RouteComparisonItem> = emptyList(),
        val speedChartPoints: List<SpeedChartPoint> = emptyList(),
        val averageSpeedMph: Float = 0f,
        val maxSpeedMph: Float = 0f,
        val sprintLaps: List<Lap> = emptyList(),
        val dogWalkEvents: List<DogWalkEvent> = emptyList(),
    ) : DetailUiState()
}
