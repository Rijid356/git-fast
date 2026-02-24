package com.gitfast.app.data.model

data class DailyActivityMetrics(
    val activeMinutes: Int = 0,
    val activeMinutesGoal: Int = 22,
    val distanceMiles: Double = 0.0,
    val distanceGoal: Double = 1.5,
    val activeDaysThisWeek: Int = 0,
    val activeDaysGoal: Int = 5,
) {
    val activeMinutesProgress: Float
        get() = if (activeMinutesGoal > 0) (activeMinutes.toFloat() / activeMinutesGoal).coerceAtLeast(0f) else 0f

    val distanceProgress: Float
        get() = if (distanceGoal > 0.0) (distanceMiles.toFloat() / distanceGoal.toFloat()).coerceAtLeast(0f) else 0f

    val activeDaysProgress: Float
        get() = if (activeDaysGoal > 0) (activeDaysThisWeek.toFloat() / activeDaysGoal).coerceAtLeast(0f) else 0f
}
