package com.gitfast.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.PhaseType
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

                _uiState.value = DetailUiState.Loaded(
                    detail = workout.toDetailItem(),
                    phases = PhaseAnalyzer.analyzePhases(workout.phases),
                    lapAnalysis = lapAnalysis
                )
            }
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
        val lapAnalysis: LapAnalysis?
    ) : DetailUiState()
}
