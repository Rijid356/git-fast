package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields

object TrendsCalculator {

    data class PeriodSummary(
        val label: String,
        val startDate: LocalDate,
        val workoutCount: Int,
        val totalDistanceMeters: Double,
        val totalDurationMillis: Long,
        val avgPaceSecondsPerMile: Int?,
    )

    data class ComparisonResult(
        val current: PeriodSummary,
        val previous: PeriodSummary?,
        val distanceDeltaPercent: Double?,
        val workoutCountDeltaPercent: Double?,
        val durationDeltaPercent: Double?,
        val paceDeltaPercent: Double?,
    )

    fun groupByWeek(
        workouts: List<Workout>,
        weeksBack: Int = 8,
        today: LocalDate = LocalDate.now(),
    ): List<PeriodSummary> {
        val weekFields = WeekFields.ISO
        val zone = ZoneId.systemDefault()

        // Find the Monday of the current week
        val currentWeekStart = today.with(weekFields.dayOfWeek(), 1)

        val buckets = (0 until weeksBack).map { i ->
            val weekStart = currentWeekStart.minusWeeks(i.toLong())
            weekStart
        }.reversed()

        return buckets.map { weekStart ->
            val weekEnd = weekStart.plusDays(7)
            val weekWorkouts = workouts.filter { workout ->
                val workoutDate = workout.startTime.atZone(zone).toLocalDate()
                workoutDate >= weekStart && workoutDate < weekEnd
            }
            buildSummary(
                label = "${weekStart.monthValue}/${weekStart.dayOfMonth}",
                startDate = weekStart,
                workouts = weekWorkouts,
            )
        }
    }

    fun groupByMonth(
        workouts: List<Workout>,
        monthsBack: Int = 6,
        today: LocalDate = LocalDate.now(),
    ): List<PeriodSummary> {
        val zone = ZoneId.systemDefault()

        val buckets = (0 until monthsBack).map { i ->
            today.minusMonths(i.toLong()).withDayOfMonth(1)
        }.reversed()

        return buckets.map { monthStart ->
            val monthEnd = monthStart.plusMonths(1)
            val monthWorkouts = workouts.filter { workout ->
                val workoutDate = workout.startTime.atZone(zone).toLocalDate()
                workoutDate >= monthStart && workoutDate < monthEnd
            }
            val monthLabel = monthStart.month.name.take(3)
            buildSummary(
                label = monthLabel,
                startDate = monthStart,
                workouts = monthWorkouts,
            )
        }
    }

    fun compare(current: PeriodSummary, previous: PeriodSummary?): ComparisonResult {
        return ComparisonResult(
            current = current,
            previous = previous,
            distanceDeltaPercent = deltaPercent(
                current.totalDistanceMeters,
                previous?.totalDistanceMeters,
            ),
            workoutCountDeltaPercent = deltaPercent(
                current.workoutCount.toDouble(),
                previous?.workoutCount?.toDouble(),
            ),
            durationDeltaPercent = deltaPercent(
                current.totalDurationMillis.toDouble(),
                previous?.totalDurationMillis?.toDouble(),
            ),
            paceDeltaPercent = if (current.avgPaceSecondsPerMile != null && previous?.avgPaceSecondsPerMile != null) {
                // For pace, lower is better, so we invert: negative delta = improvement
                deltaPercent(
                    current.avgPaceSecondsPerMile.toDouble(),
                    previous.avgPaceSecondsPerMile.toDouble(),
                )
            } else {
                null
            },
        )
    }

    private fun buildSummary(
        label: String,
        startDate: LocalDate,
        workouts: List<Workout>,
    ): PeriodSummary {
        val totalDistance = workouts.sumOf { it.distanceMeters }
        val totalDuration = workouts.sumOf { it.durationMillis ?: 0L }
        val runs = workouts.filter { it.activityType == ActivityType.RUN }
        val avgPace = if (runs.isNotEmpty()) {
            val paces = runs.mapNotNull { it.averagePaceSecondsPerMile?.toInt() }
            if (paces.isNotEmpty()) paces.average().toInt() else null
        } else {
            null
        }

        return PeriodSummary(
            label = label,
            startDate = startDate,
            workoutCount = workouts.size,
            totalDistanceMeters = totalDistance,
            totalDurationMillis = totalDuration,
            avgPaceSecondsPerMile = avgPace,
        )
    }

    private fun deltaPercent(current: Double, previous: Double?): Double? {
        if (previous == null || previous == 0.0) return null
        return ((current - previous) / previous) * 100.0
    }
}
