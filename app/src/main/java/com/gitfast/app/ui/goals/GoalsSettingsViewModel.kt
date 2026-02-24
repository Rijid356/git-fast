package com.gitfast.app.ui.goals

import androidx.lifecycle.ViewModel
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.model.DistanceUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class GoalsUiState(
    val dailyActiveMinutesGoal: Int = 22,
    val dailyDistanceGoalMiles: Double = 1.5,
    val weeklyActiveDaysGoal: Int = 5,
    val distanceUnit: DistanceUnit = DistanceUnit.MILES,
)

@HiltViewModel
class GoalsSettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GoalsUiState(
            dailyActiveMinutesGoal = settingsStore.dailyActiveMinutesGoal,
            dailyDistanceGoalMiles = settingsStore.dailyDistanceGoalMiles,
            weeklyActiveDaysGoal = settingsStore.weeklyActiveDaysGoal,
            distanceUnit = settingsStore.distanceUnit,
        )
    )
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    fun setDailyActiveMinutesGoal(minutes: Int) {
        val clamped = minutes.coerceIn(5, 120)
        settingsStore.dailyActiveMinutesGoal = clamped
        _uiState.value = _uiState.value.copy(dailyActiveMinutesGoal = clamped)
    }

    fun setDailyDistanceGoal(miles: Double) {
        val clamped = miles.coerceIn(0.5, 20.0)
        settingsStore.dailyDistanceGoalMiles = clamped
        _uiState.value = _uiState.value.copy(dailyDistanceGoalMiles = clamped)
    }

    fun setWeeklyActiveDaysGoal(days: Int) {
        val clamped = days.coerceIn(1, 7)
        settingsStore.weeklyActiveDaysGoal = clamped
        _uiState.value = _uiState.value.copy(weeklyActiveDaysGoal = clamped)
    }
}
