package com.gitfast.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.repository.WorkoutRepository
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
            _uiState.value = if (workout != null) {
                DetailUiState.Loaded(workout.toDetailItem())
            } else {
                DetailUiState.NotFound
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
    data class Loaded(val detail: WorkoutDetailItem) : DetailUiState()
}
