package com.gitfast.app.util

import com.gitfast.app.data.model.Workout
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.min

object StreakCalculator {

    private const val MAX_MULTIPLIER = 1.5
    private const val MULTIPLIER_PER_DAY = 0.1

    /**
     * Count consecutive workout days ending at [today] or yesterday.
     * If [today] has a workout, counts backward from today.
     * If not, checks yesterday — if yesterday has one, counts backward from yesterday.
     * Returns 0 if no recent consecutive workouts.
     */
    fun getCurrentStreak(
        workouts: List<Workout>,
        today: LocalDate = LocalDate.now(),
    ): Int {
        if (workouts.isEmpty()) return 0

        val zone = ZoneId.systemDefault()
        val dateSet = workouts
            .map { it.startTime.atZone(zone).toLocalDate() }
            .toSet()

        // Start from today if it has a workout, otherwise try yesterday
        val startDate = when {
            dateSet.contains(today) -> today
            dateSet.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }

        var streak = 0
        var checkDate = startDate
        while (dateSet.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }

    /**
     * Find the longest consecutive-day streak across all workouts.
     * Scans all workout dates and returns the longest run of consecutive days.
     */
    fun getLongestStreak(workouts: List<Workout>): Int {
        if (workouts.isEmpty()) return 0

        val zone = ZoneId.systemDefault()
        val sortedDates = workouts
            .map { it.startTime.atZone(zone).toLocalDate() }
            .distinct()
            .sorted()

        var longest = 1
        var current = 1

        for (i in 1 until sortedDates.size) {
            if (sortedDates[i] == sortedDates[i - 1].plusDays(1)) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }

        return longest
    }

    /**
     * Count consecutive active days ending at [today] or yesterday.
     * A day counts as "active" if it has a workout OR an exercise session.
     * This unifies both activity types into a single streak.
     */
    fun getCurrentStreak(
        workouts: List<Workout>,
        exerciseSessionDates: Set<LocalDate>,
        today: LocalDate = LocalDate.now(),
    ): Int {
        if (workouts.isEmpty() && exerciseSessionDates.isEmpty()) return 0

        val zone = ZoneId.systemDefault()
        val workoutDates = workouts
            .map { it.startTime.atZone(zone).toLocalDate() }
            .toSet()
        val dateSet = workoutDates + exerciseSessionDates

        val startDate = when {
            dateSet.contains(today) -> today
            dateSet.contains(today.minusDays(1)) -> today.minusDays(1)
            else -> return 0
        }

        var streak = 0
        var checkDate = startDate
        while (dateSet.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }

    /**
     * Find the longest consecutive-day streak across all workouts and exercise sessions.
     */
    fun getLongestStreak(
        workouts: List<Workout>,
        exerciseSessionDates: Set<LocalDate>,
    ): Int {
        val zone = ZoneId.systemDefault()
        val workoutDates = workouts
            .map { it.startTime.atZone(zone).toLocalDate() }
            .toSet()
        val allDates = (workoutDates + exerciseSessionDates).sorted()

        if (allDates.isEmpty()) return 0

        var longest = 1
        var current = 1

        for (i in 1 until allDates.size) {
            if (allDates[i] == allDates[i - 1].plusDays(1)) {
                current++
                if (current > longest) longest = current
            } else if (allDates[i] != allDates[i - 1]) {
                current = 1
            }
        }

        return longest
    }

    /**
     * XP multiplier for a given streak length.
     * Day 1 = 1.0x, Day 2 = 1.1x, Day 3 = 1.2x, ... capped at 1.5x.
     */
    fun getMultiplier(streakDays: Int): Double {
        if (streakDays <= 1) return 1.0
        return min(1.0 + (streakDays - 1) * MULTIPLIER_PER_DAY, MAX_MULTIPLIER)
    }

    /**
     * Human-readable multiplier label, e.g. "1.2x".
     */
    fun getMultiplierLabel(streakDays: Int): String {
        val multiplier = getMultiplier(streakDays)
        return if (multiplier == multiplier.toLong().toDouble()) {
            "${multiplier.toLong()}.0x"
        } else {
            "${"%.1f".format(multiplier)}x"
        }
    }
}
