package com.gitfast.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.local.WorkoutStateStore
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.service.WorkoutService
import com.gitfast.app.ui.history.WorkoutHistoryItem
import com.gitfast.app.ui.history.toHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutStateStore: WorkoutStateStore,
    workoutRepository: WorkoutRepository,
    characterRepository: CharacterRepository,
) : ViewModel() {

    private val _showRecoveryDialog = MutableStateFlow(false)
    val showRecoveryDialog: StateFlow<Boolean> = _showRecoveryDialog.asStateFlow()

    val characterProfile: StateFlow<CharacterProfile> =
        characterRepository.getProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CharacterProfile())

    val recentRuns: StateFlow<List<WorkoutHistoryItem>> =
        workoutRepository.getCompletedWorkoutsByType(ActivityType.RUN)
            .map { list -> list.take(3).map { it.toHistoryItem() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDogWalks: StateFlow<List<WorkoutHistoryItem>> =
        workoutRepository.getCompletedWorkoutsByType(ActivityType.DOG_WALK)
            .map { list -> list.take(3).map { it.toHistoryItem() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkForIncompleteWorkout()
    }

    private fun checkForIncompleteWorkout() {
        _showRecoveryDialog.value =
            workoutStateStore.hasActiveWorkout() && !WorkoutService.isRunning
    }

    fun dismissRecoveryDialog() {
        workoutStateStore.clearActiveWorkout()
        _showRecoveryDialog.value = false
    }
}
