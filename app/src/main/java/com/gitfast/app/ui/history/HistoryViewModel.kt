package com.gitfast.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
) : ViewModel() {

    val workouts: StateFlow<HistoryUiState> = workoutRepository.getCompletedWorkouts()
        .map { list ->
            if (list.isEmpty()) {
                HistoryUiState.Empty
            } else {
                val grouped = list
                    .map { it.toHistoryItem() }
                    .groupBy { DateFormatter.monthYear(it.startTime) }
                HistoryUiState.Loaded(grouped)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState.Loading)
}

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data object Empty : HistoryUiState()
    data class Loaded(val groupedWorkouts: Map<String, List<WorkoutHistoryItem>>) : HistoryUiState()
}
