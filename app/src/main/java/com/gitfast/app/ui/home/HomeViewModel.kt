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
import com.gitfast.app.util.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
        combine(
            characterRepository.getProfile(),
            workoutRepository.getCompletedWorkouts(),
        ) { profile, workouts ->
            val streak = StreakCalculator.getCurrentStreak(workouts)
            profile.copy(
                currentStreak = streak,
                streakMultiplier = StreakCalculator.getMultiplier(streak),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CharacterProfile())

    private val xpByWorkout = characterRepository.getXpByWorkout()

    val recentRuns: StateFlow<List<WorkoutHistoryItem>> =
        combine(
            workoutRepository.getCompletedWorkoutsByType(ActivityType.RUN),
            xpByWorkout,
        ) { list, xpMap ->
            list.take(3).map { it.toHistoryItem().copy(xpEarned = xpMap[it.id] ?: 0) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDogWalks: StateFlow<List<WorkoutHistoryItem>> =
        combine(
            workoutRepository.getCompletedWorkoutsByType(ActivityType.DOG_WALK),
            xpByWorkout,
        ) { list, xpMap ->
            list.take(3).map { it.toHistoryItem().copy(xpEarned = xpMap[it.id] ?: 0) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
