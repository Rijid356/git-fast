package com.gitfast.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.DistanceCalculator
import com.gitfast.app.util.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsHubViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    data class AnalyticsStats(
        val totalWorkouts: Int = 0,
        val totalDistanceFormatted: String = "0.0 mi",
        val totalDurationFormatted: String = "0h 0m",
        val bestStreak: Int = 0,
    )

    private val _stats = MutableStateFlow(AnalyticsStats())
    val stats: StateFlow<AnalyticsStats> = _stats.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val totalWorkouts = workoutRepository.getCompletedWorkoutCount()
            val totalMeters = workoutRepository.getTotalDistanceMeters()
            val totalMillis = workoutRepository.getTotalDurationMillis()
            val allWorkouts = workoutRepository.getAllCompletedWorkoutsOnce()
            val bestStreak = StreakCalculator.getLongestStreak(allWorkouts)

            val miles = DistanceCalculator.metersToMiles(totalMeters)
            val distanceFormatted = if (miles >= 100) {
                "${miles.toInt()} mi"
            } else {
                "${"%.1f".format(miles)} mi"
            }

            val totalSeconds = totalMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val durationFormatted = "${hours}h ${minutes}m"

            _stats.value = AnalyticsStats(
                totalWorkouts = totalWorkouts,
                totalDistanceFormatted = distanceFormatted,
                totalDurationFormatted = durationFormatted,
                bestStreak = bestStreak,
            )
        }
    }
}
