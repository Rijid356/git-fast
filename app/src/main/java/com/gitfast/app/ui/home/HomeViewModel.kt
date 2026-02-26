package com.gitfast.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.local.WorkoutStateStore
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.DailyActivityMetrics
import com.gitfast.app.data.model.WeeklyMetrics
import com.gitfast.app.data.model.BodyCompReading
import com.gitfast.app.data.repository.BodyCompRepository
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.model.SorenessLog
import com.gitfast.app.data.repository.SorenessRepository
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
    bodyCompRepository: BodyCompRepository,
    sorenessRepository: SorenessRepository,
    settingsStore: SettingsStore,
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

    val dailyMetrics: StateFlow<DailyActivityMetrics> =
        combine(
            workoutRepository.getTodaysActiveMillis(),
            workoutRepository.getTodaysDistanceMeters(),
            workoutRepository.getWeeklyActiveDayCount(),
        ) { activeMillis, distanceMeters, activeDays ->
            val activeMinutes = (activeMillis / 60_000).toInt()
            val distanceMiles = distanceMeters * 0.000621371
            DailyActivityMetrics(
                activeMinutes = activeMinutes,
                activeMinutesGoal = settingsStore.dailyActiveMinutesGoal,
                distanceMiles = distanceMiles,
                distanceGoal = settingsStore.dailyDistanceGoalMiles,
                activeDaysThisWeek = activeDays,
                activeDaysGoal = settingsStore.weeklyActiveDaysGoal,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyActivityMetrics())

    val weeklyMetrics: StateFlow<WeeklyMetrics> =
        combine(
            workoutRepository.getWeeklyActiveMillis(),
            workoutRepository.getWeeklyDistanceMeters(),
            workoutRepository.getWeeklyWorkoutCount(),
            workoutRepository.getWeeklyActiveDayCount(),
            workoutRepository.getPreviousWeekActiveMillis(),
        ) { activeMillis, distMeters, count, activeDays, prevMillis ->
            WeeklyMetrics(
                activeMinutes = (activeMillis / 60_000).toInt(),
                distanceMiles = distMeters * 0.000621371,
                activeDays = activeDays,
                activeDaysGoal = settingsStore.weeklyActiveDaysGoal,
                workoutCount = count,
                prevWeekActiveMinutes = (prevMillis / 60_000).toInt(),
            )
        }.combine(
            combine(
                workoutRepository.getPreviousWeekDistanceMeters(),
                workoutRepository.getPreviousWeekWorkoutCount(),
            ) { prevDist, prevCount -> prevDist to prevCount }
        ) { partial, (prevDist, prevCount) ->
            partial.copy(
                prevWeekDistanceMiles = prevDist * 0.000621371,
                prevWeekWorkoutCount = prevCount,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyMetrics())

    val latestWeight: StateFlow<BodyCompReading?> =
        bodyCompRepository.getLatestReading()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todaySoreness: StateFlow<SorenessLog?> =
        sorenessRepository.observeTodayLog()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    val recentDogRuns: StateFlow<List<WorkoutHistoryItem>> =
        combine(
            workoutRepository.getCompletedWorkoutsByType(ActivityType.DOG_RUN),
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
