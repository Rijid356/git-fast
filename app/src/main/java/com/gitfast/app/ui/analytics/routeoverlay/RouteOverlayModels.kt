package com.gitfast.app.ui.analytics.routeoverlay

import androidx.compose.ui.graphics.Color
import com.gitfast.app.ui.detail.LatLngPoint
import com.gitfast.app.ui.detail.RouteBounds

data class RouteTrace(
    val workoutId: String,
    val date: String,
    val durationFormatted: String,
    val distanceFormatted: String,
    val points: List<LatLngPoint>,
    val color: Color,
)

data class RouteOverlayUiState(
    val routeTags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val traces: List<RouteTrace> = emptyList(),
    val bounds: RouteBounds? = null,
    val isLoading: Boolean = false,
)
