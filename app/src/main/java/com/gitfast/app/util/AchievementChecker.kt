package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import java.time.LocalDate
import java.time.ZoneId

data class AchievementSnapshot(
    val allWorkouts: List<Workout>,
    val totalLapCount: Int,
    val dogWalkCount: Int,
    val characterLevel: Int,
    val unlockedIds: Set<String>,
    val totalDogWalkDistanceMiles: Double = 0.0,
)

object AchievementChecker {

    /**
     * Check user-profile achievements (profileId=1).
     * Only evaluates achievements with profileId=1.
     */
    fun checkNewAchievements(snapshot: AchievementSnapshot): List<AchievementDef> {
        val earned = mutableListOf<AchievementDef>()

        for (def in AchievementDef.entries) {
            if (def.profileId != 1) continue
            if (def.id in snapshot.unlockedIds) continue
            if (isEarned(def, snapshot)) {
                earned.add(def)
            }
        }

        return earned
    }

    /**
     * Check Juniper-profile achievements (profileId=2).
     * Only evaluates achievements with profileId=2.
     */
    fun checkJuniperAchievements(snapshot: AchievementSnapshot): List<AchievementDef> {
        val earned = mutableListOf<AchievementDef>()

        for (def in AchievementDef.entries) {
            if (def.profileId != 2) continue
            if (def.id in snapshot.unlockedIds) continue
            if (isEarned(def, snapshot)) {
                earned.add(def)
            }
        }

        return earned
    }

    private fun isEarned(def: AchievementDef, snapshot: AchievementSnapshot): Boolean {
        return when (def) {
            // Cumulative distance
            AchievementDef.FIRST_MILE -> totalDistanceMiles(snapshot) >= 1.0
            AchievementDef.MARATHON_CLUB -> totalDistanceMiles(snapshot) >= 26.2
            AchievementDef.CENTURY_RUNNER -> totalDistanceMiles(snapshot) >= 100.0
            AchievementDef.ULTRA_RUNNER -> totalDistanceMiles(snapshot) >= 250.0

            // Single-workout PRs
            AchievementDef.FIVE_K_FINISHER -> maxSingleRunMiles(snapshot) >= 3.1
            AchievementDef.TEN_K_FINISHER -> maxSingleRunMiles(snapshot) >= 6.2
            AchievementDef.HALF_MARATHON -> maxSingleRunMiles(snapshot) >= 13.1

            // Workout count
            AchievementDef.FIRST_STEPS -> snapshot.allWorkouts.size >= 1
            AchievementDef.GETTING_STARTED -> snapshot.allWorkouts.size >= 5
            AchievementDef.DEDICATED -> snapshot.allWorkouts.size >= 25
            AchievementDef.COMMITTED -> snapshot.allWorkouts.size >= 50
            AchievementDef.CENTURION -> snapshot.allWorkouts.size >= 100

            // Streaks
            AchievementDef.THREE_PEAT -> longestStreak(snapshot) >= 3
            AchievementDef.WEEK_WARRIOR -> longestStreak(snapshot) >= 7
            AchievementDef.FORTNIGHT_FORCE -> longestStreak(snapshot) >= 14

            // Laps
            AchievementDef.LAP_LEADER -> snapshot.totalLapCount >= 10
            AchievementDef.TRACK_STAR -> snapshot.totalLapCount >= 50

            // Dog walks (user)
            AchievementDef.GOOD_BOY -> snapshot.dogWalkCount >= 1
            AchievementDef.DOGS_BEST_FRIEND -> snapshot.dogWalkCount >= 25

            // Juniper achievements
            AchievementDef.JUNIPER_FIRST_SNIFF -> snapshot.dogWalkCount >= 1
            AchievementDef.JUNIPER_TRAIL_SNIFFER -> snapshot.totalDogWalkDistanceMiles >= 10.0
            AchievementDef.JUNIPER_ADVENTURE_PUP -> snapshot.totalDogWalkDistanceMiles >= 25.0
            AchievementDef.JUNIPER_PACK_LEADER -> snapshot.dogWalkCount >= 10
            AchievementDef.JUNIPER_TRAIL_MASTER -> snapshot.dogWalkCount >= 50
            AchievementDef.JUNIPER_GOOD_GIRL -> snapshot.characterLevel >= 5

            // Leveling
            AchievementDef.LEVEL_5 -> snapshot.characterLevel >= 5
            AchievementDef.LEVEL_10 -> snapshot.characterLevel >= 10
        }
    }

    private fun totalDistanceMiles(snapshot: AchievementSnapshot): Double {
        return snapshot.allWorkouts.sumOf { it.distanceMiles }
    }

    private fun maxSingleRunMiles(snapshot: AchievementSnapshot): Double {
        return snapshot.allWorkouts
            .filter { it.activityType == ActivityType.RUN }
            .maxOfOrNull { it.distanceMiles } ?: 0.0
    }

    fun longestStreak(snapshot: AchievementSnapshot): Int {
        if (snapshot.allWorkouts.isEmpty()) return 0

        val zone = ZoneId.systemDefault()
        val workoutDates = snapshot.allWorkouts
            .map { it.startTime.atZone(zone).toLocalDate() }
            .distinct()
            .sorted()

        if (workoutDates.isEmpty()) return 0

        var longest = 1
        var current = 1

        for (i in 1 until workoutDates.size) {
            if (workoutDates[i] == workoutDates[i - 1].plusDays(1)) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 1
            }
        }

        return longest
    }
}
