package com.gitfast.app.ui.analytics.routeoverlay

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.ui.detail.LatLngPoint
import com.gitfast.app.ui.detail.RouteBounds
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.CyanAccent
import com.gitfast.app.ui.theme.NeonGreen
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

val traceColors = listOf(
    NeonGreen,
    CyanAccent,
    AmberAccent,
    NeonGreen.copy(alpha = 0.5f),
    CyanAccent.copy(alpha = 0.5f),
)

@HiltViewModel
class RouteOverlayViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteOverlayUiState())
    val uiState: StateFlow<RouteOverlayUiState> = _uiState.asStateFlow()

    init {
        loadRouteTags()
    }

    private fun loadRouteTags() {
        viewModelScope.launch {
            val tags = workoutRepository.getAllRouteTagNames()
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
            val workouts = workoutRepository.getWorkoutsWithGpsForRouteTag(tag)
            val traces = workouts.mapIndexed { index, workout ->
                val durationSeconds = workout.durationMillis?.let { (it / 1000).toInt() }
                RouteTrace(
                    workoutId = workout.id,
                    date = DateFormatter.shortDate(workout.startTime),
                    durationFormatted = durationSeconds?.let { formatElapsedTime(it) } ?: "--:--",
                    distanceFormatted = formatDistance(workout.distanceMeters),
                    points = workout.gpsPoints.map { LatLngPoint(it.latitude, it.longitude) },
                    color = traceColors.getOrElse(index) { traceColors.last() },
                )
            }
            val allPoints = traces.flatMap { it.points }
            val bounds = if (allPoints.size >= 2) {
                RouteBounds(
                    minLat = allPoints.minOf { it.latitude },
                    maxLat = allPoints.maxOf { it.latitude },
                    minLng = allPoints.minOf { it.longitude },
                    maxLng = allPoints.maxOf { it.longitude },
                )
            } else null

            _uiState.update { it.copy(traces = traces, bounds = bounds, isLoading = false) }
        }
    }
}
