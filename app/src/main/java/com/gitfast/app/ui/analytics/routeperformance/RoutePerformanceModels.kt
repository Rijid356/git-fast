package com.gitfast.app.ui.analytics.routeperformance

data class PerformanceRow(
    val workoutId: String,
    val date: String,
    val durationFormatted: String,
    val distanceFormatted: String,
    val deltaFormatted: String?,
    val deltaMillis: Long?,
    val isMostRecent: Boolean,
    val isPersonalBest: Boolean,
)

data class TrendSummary(
    val deltaFormatted: String,
    val isImproving: Boolean,
    val isConsistent: Boolean,
)

data class RoutePerformanceUiState(
    val routeTags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val rows: List<PerformanceRow> = emptyList(),
    val personalBest: PerformanceRow? = null,
    val trendSummary: TrendSummary? = null,
    val sessionCount: Int = 0,
    val isLoading: Boolean = false,
)
