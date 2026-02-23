package com.gitfast.app.ui.analytics.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.TrendsCalculator
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import com.gitfast.app.util.formatPace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendsUiState())
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    private var allWorkouts: List<Workout> = emptyList()

    init {
        loadWorkouts()
    }

    fun setPeriod(period: TrendPeriod) {
        _uiState.update { it.copy(period = period) }
        recompute()
    }

    fun setFilter(filter: ActivityFilter) {
        _uiState.update { it.copy(filter = filter) }
        recompute()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            allWorkouts = workoutRepository.getAllCompletedWorkoutsOnce()
            if (allWorkouts.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, isEmpty = true) }
                return@launch
            }
            recompute()
        }
    }

    private fun recompute() {
        val state = _uiState.value
        val filtered = when (state.filter) {
            ActivityFilter.ALL -> allWorkouts
            ActivityFilter.RUNS -> allWorkouts.filter { it.activityType == ActivityType.RUN }
            ActivityFilter.WALKS -> allWorkouts.filter { it.activityType == ActivityType.DOG_WALK }
        }

        val summaries = when (state.period) {
            TrendPeriod.WEEK -> TrendsCalculator.groupByWeek(filtered)
            TrendPeriod.MONTH -> TrendsCalculator.groupByMonth(filtered)
        }

        val current = summaries.lastOrNull()
        val previous = if (summaries.size >= 2) summaries[summaries.size - 2] else null

        val comparison = if (current != null) {
            val result = TrendsCalculator.compare(current, previous)
            buildComparisonDisplay(result)
        } else {
            null
        }

        val distanceBars = summaries.mapIndexed { index, summary ->
            val miles = summary.totalDistanceMeters * 0.000621371
            ChartBar(
                label = summary.label,
                value = miles.toFloat(),
                displayValue = formatDistance(summary.totalDistanceMeters),
                isCurrent = index == summaries.lastIndex,
            )
        }

        val workoutBars = summaries.mapIndexed { index, summary ->
            ChartBar(
                label = summary.label,
                value = summary.workoutCount.toFloat(),
                displayValue = "${summary.workoutCount}",
                isCurrent = index == summaries.lastIndex,
            )
        }

        _uiState.update {
            it.copy(
                comparison = comparison,
                distanceBars = distanceBars,
                workoutBars = workoutBars,
                isLoading = false,
                isEmpty = false,
            )
        }
    }

    private fun buildComparisonDisplay(result: TrendsCalculator.ComparisonResult): ComparisonDisplay {
        val current = result.current
        val previous = result.previous

        return ComparisonDisplay(
            currentDistance = formatDistance(current.totalDistanceMeters),
            previousDistance = previous?.let { formatDistance(it.totalDistanceMeters) },
            distanceDelta = result.distanceDeltaPercent?.let { formatDelta(it) },
            distanceDeltaPositive = result.distanceDeltaPercent?.let { it >= 0 },

            currentWorkouts = "${current.workoutCount}",
            previousWorkouts = previous?.let { "${it.workoutCount}" },
            workoutsDelta = result.workoutCountDeltaPercent?.let { formatDelta(it) },
            workoutsDeltaPositive = result.workoutCountDeltaPercent?.let { it >= 0 },

            currentDuration = formatElapsedTime((current.totalDurationMillis / 1000).toInt()),
            previousDuration = previous?.let {
                formatElapsedTime((it.totalDurationMillis / 1000).toInt())
            },
            durationDelta = result.durationDeltaPercent?.let { formatDelta(it) },
            durationDeltaPositive = result.durationDeltaPercent?.let { it >= 0 },

            currentPace = current.avgPaceSecondsPerMile?.let { formatPace(it) },
            previousPace = previous?.avgPaceSecondsPerMile?.let { formatPace(it) },
            paceDelta = result.paceDeltaPercent?.let { formatDelta(it) },
            // For pace, negative change is good (faster)
            paceDeltaPositive = result.paceDeltaPercent?.let { it <= 0 },
        )
    }

    private fun formatDelta(percent: Double): String {
        val sign = if (percent >= 0) "+" else ""
        return "$sign${percent.toInt()}%"
    }
}
