package com.gitfast.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.analysis.RouteComparisonAnalyzer
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.LapAnalyzer
import com.gitfast.app.util.PhaseAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

                // Route comparison for dog walks
                val routeComparison = if (workout.activityType == ActivityType.DOG_WALK && workout.routeTag != null) {
                    val previousWalks = workoutRepository.getDogWalksByRouteOnce(workout.routeTag!!)
                        .filter { it.id != workout.id }
                    RouteComparisonAnalyzer.compare(workout, previousWalks)
                } else {
                    emptyList()
                }

                // Fetch XP earned for this workout
                val xpTransaction = characterRepository.getXpTransactionForWorkout(workoutId)

                _uiState.value = DetailUiState.Loaded(
                    detail = workout.toDetailItem().copy(
                        xpEarned = xpTransaction?.xpAmount ?: 0,
                        xpBreakdown = xpTransaction?.reason,
                    ),
                    phases = PhaseAnalyzer.analyzePhases(workout.phases),
                    lapAnalysis = lapAnalysis,
                    routeComparison = routeComparison
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
        val routeComparison: List<RouteComparisonAnalyzer.RouteComparisonItem> = emptyList()
    ) : DetailUiState()
}
