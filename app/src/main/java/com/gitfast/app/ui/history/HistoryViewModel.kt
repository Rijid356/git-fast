package com.gitfast.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.ui.components.ActivityFilter
import com.gitfast.app.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(ActivityFilter.ALL)
    val filter: StateFlow<ActivityFilter> = _filter.asStateFlow()

    fun setFilter(filter: ActivityFilter) {
        _filter.value = filter
    }

    val workouts: StateFlow<HistoryUiState> = _filter.flatMapLatest { filter ->
        when (filter) {
            ActivityFilter.ALL -> workoutRepository.getCompletedWorkouts()
            ActivityFilter.RUNS -> workoutRepository.getCompletedWorkoutsByType(ActivityType.RUN)
            ActivityFilter.WALKS -> workoutRepository.getCompletedWorkoutsByType(ActivityType.DOG_WALK)
        }
    }.map { list ->
        if (list.isEmpty()) {
            HistoryUiState.Empty
        } else {
            val grouped = list
                .map { it.toHistoryItem() }
                .groupBy { DateFormatter.monthYear(it.startTime) }
            HistoryUiState.Loaded(grouped)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState.Loading)
}

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data object Empty : HistoryUiState()
    data class Loaded(val groupedWorkouts: Map<String, List<WorkoutHistoryItem>>) : HistoryUiState()
}
