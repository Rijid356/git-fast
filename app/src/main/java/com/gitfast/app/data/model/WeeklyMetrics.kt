package com.gitfast.app.data.model

data class WeeklyMetrics(
    val activeMinutes: Int = 0,
    val distanceMiles: Double = 0.0,
    val activeDays: Int = 0,
    val activeDaysGoal: Int = 5,
    val workoutCount: Int = 0,
    val prevWeekActiveMinutes: Int = 0,
    val prevWeekDistanceMiles: Double = 0.0,
    val prevWeekWorkoutCount: Int = 0,
)
