package com.gitfast.app.ui.analytics.routeperformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.analysis.RouteComparisonAnalyzer
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.DateFormatter
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutePerformanceViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutePerformanceUiState())
    val uiState: StateFlow<RoutePerformanceUiState> = _uiState.asStateFlow()

    init {
        loadRouteTags()
    }

    private fun loadRouteTags() {
        viewModelScope.launch {
            val tags = workoutRepository.getAllRouteTags()
                .sortedByDescending { it.lastUsed }
                .map { it.name }
            _uiState.update { it.copy(
                routeTags = tags,
                selectedTag = if (tags.size == 1) tags.first() else null,
            ) }
            if (tags.size == 1) selectRouteTag(tags.first())
        }
    }

    fun selectRouteTag(tag: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedTag = tag, isLoading = true) }
            val workouts = workoutRepository.getDogWalksByRouteOnce(tag)

            if (workouts.isEmpty()) {
                _uiState.update { it.copy(
                    rows = emptyList(),
                    personalBest = null,
                    trendSummary = null,
                    sessionCount = 0,
                    isLoading = false,
                ) }
                return@launch
            }

            val mostRecent = workouts.first()
            val mostRecentDuration = mostRecent.durationMillis ?: 0L

            val bestWorkout = workouts.minByOrNull { it.durationMillis ?: Long.MAX_VALUE }

            val rows = workouts.map { workout ->
                val duration = workout.durationMillis ?: 0L
                val durationSeconds = (duration / 1000).toInt()
                val delta = if (workout.id == mostRecent.id) null else duration - mostRecentDuration
                val deltaSeconds = delta?.let { (it / 1000).toInt() }

                PerformanceRow(
                    workoutId = workout.id,
                    date = DateFormatter.shortDate(workout.startTime),
                    durationFormatted = formatElapsedTime(durationSeconds),
                    distanceFormatted = formatDistance(workout.distanceMeters),
                    deltaFormatted = deltaSeconds?.let { RouteComparisonAnalyzer.formatDelta(it) },
                    deltaMillis = delta,
                    isMostRecent = workout.id == mostRecent.id,
                    isPersonalBest = workout.id == bestWorkout?.id && workouts.size > 1,
                )
            }

            val trendSummary = computeTrend(workouts)

            _uiState.update { it.copy(
                rows = rows,
                personalBest = rows.find { row -> row.isPersonalBest },
                trendSummary = trendSummary,
                sessionCount = workouts.size,
                isLoading = false,
            ) }
        }
    }

    internal fun computeTrend(workouts: List<Workout>): TrendSummary? {
        if (workouts.size < 4) return null

        val recentDurations = workouts.take(3).mapNotNull { it.durationMillis }
        val olderDurations = workouts.drop(3).take(3).mapNotNull { it.durationMillis }

        if (recentDurations.isEmpty() || olderDurations.isEmpty()) return null

        val recentAvg = recentDurations.average()
        val olderAvg = olderDurations.average()
        val deltaSeconds = ((recentAvg - olderAvg) / 1000).toInt()

        return when {
            deltaSeconds < -5 -> TrendSummary(
                deltaFormatted = RouteComparisonAnalyzer.formatDelta(deltaSeconds),
                isImproving = true,
                isConsistent = false,
            )
            deltaSeconds > 5 -> TrendSummary(
                deltaFormatted = RouteComparisonAnalyzer.formatDelta(deltaSeconds),
                isImproving = false,
                isConsistent = false,
            )
            else -> TrendSummary(
                deltaFormatted = "",
                isImproving = false,
                isConsistent = true,
            )
        }
    }
}
