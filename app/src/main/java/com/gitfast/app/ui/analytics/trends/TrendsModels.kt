package com.gitfast.app.ui.analytics.trends

enum class TrendPeriod {
    WEEK, MONTH
}

enum class ActivityFilter {
    ALL, RUNS, WALKS
}

data class ComparisonDisplay(
    val currentDistance: String,
    val previousDistance: String?,
    val distanceDelta: String?,
    val distanceDeltaPositive: Boolean?,

    val currentWorkouts: String,
    val previousWorkouts: String?,
    val workoutsDelta: String?,
    val workoutsDeltaPositive: Boolean?,

    val currentDuration: String,
    val previousDuration: String?,
    val durationDelta: String?,
    val durationDeltaPositive: Boolean?,

    val currentPace: String?,
    val previousPace: String?,
    val paceDelta: String?,
    val paceDeltaPositive: Boolean?,
)

data class ChartBar(
    val label: String,
    val value: Float,
    val displayValue: String,
    val isCurrent: Boolean,
)

data class TrendsUiState(
    val period: TrendPeriod = TrendPeriod.WEEK,
    val filter: ActivityFilter = ActivityFilter.ALL,
    val comparison: ComparisonDisplay? = null,
    val distanceBars: List<ChartBar> = emptyList(),
    val workoutBars: List<ChartBar> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
)
