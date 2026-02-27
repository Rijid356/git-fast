package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.service.WorkoutSnapshot
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt

data class XpResult(
    val totalXp: Int,
    val breakdown: List<String>,
    val streakDays: Int = 0,
    val streakMultiplier: Double = 1.0,
)

object XpCalculator {

    private const val XP_PER_MILE_RUN = 10
    private const val XP_PER_MILE_WALK = 8
    private const val XP_PER_10_MINUTES = 5
    private const val XP_PER_LAP = 20
    private const val XP_ALL_PHASES_BONUS = 15
    private const val MINIMUM_XP = 5

    /**
     * Calculate XP earned from a completed workout.
     */
    fun calculateXp(
        distanceMeters: Double,
        durationMillis: Long,
        activityType: ActivityType,
        lapCount: Int,
        hasWarmup: Boolean,
        hasCooldown: Boolean,
        hasLaps: Boolean,
        weatherCondition: WeatherCondition? = null,
        weatherTemp: WeatherTemp? = null,
        streakDays: Int = 0,
    ): XpResult {
        val breakdown = mutableListOf<String>()
        var rawXp = 0

        // Distance XP
        val miles = DistanceCalculator.metersToMiles(distanceMeters)
        val xpPerMile = when (activityType) {
            ActivityType.RUN, ActivityType.DOG_RUN -> XP_PER_MILE_RUN
            else -> XP_PER_MILE_WALK
        }
        val distanceXp = (miles * xpPerMile).toInt()
        if (distanceXp > 0) {
            breakdown.add("+$distanceXp XP: %.1f miles".format(miles))
        }
        rawXp += distanceXp

        // Duration XP
        val minutes = durationMillis / 60_000.0
        val durationXp = ((minutes / 10.0) * XP_PER_10_MINUTES).toInt()
        if (durationXp > 0) {
            breakdown.add("+$durationXp XP: ${minutes.toInt()} min active")
        }
        rawXp += durationXp

        // Lap bonus
        if (lapCount > 0) {
            val lapXp = lapCount * XP_PER_LAP
            breakdown.add("+$lapXp XP: $lapCount laps")
            rawXp += lapXp
        }

        // All-phases bonus
        if (hasWarmup && hasLaps && hasCooldown) {
            breakdown.add("+$XP_ALL_PHASES_BONUS XP: full workout (warmup+laps+cooldown)")
            rawXp += XP_ALL_PHASES_BONUS
        }

        // Weather multiplier
        val weatherMultiplier = weatherMultiplier(weatherCondition, weatherTemp)
        if (weatherMultiplier > 1.0) {
            val bonusXp = ((rawXp * weatherMultiplier) - rawXp).toInt()
            val label = buildString {
                weatherCondition?.let { append(it.name.lowercase()) }
                weatherTemp?.let {
                    if (isNotEmpty()) append("/")
                    append(it.name.lowercase())
                }
            }
            if (bonusXp > 0) {
                breakdown.add("+$bonusXp XP: weather bonus ($label)")
            }
            rawXp = (rawXp * weatherMultiplier).toInt()
        }

        // Streak multiplier
        val streakMultiplier = StreakCalculator.getMultiplier(streakDays)
        if (streakMultiplier > 1.0) {
            val bonusXp = ((rawXp * streakMultiplier) - rawXp).toInt()
            if (bonusXp > 0) {
                breakdown.add("+$bonusXp XP: $streakDays-day streak (${StreakCalculator.getMultiplierLabel(streakDays)})")
            }
            rawXp = (rawXp * streakMultiplier).toInt()
        }

        val finalXp = max(rawXp, MINIMUM_XP)
        return XpResult(
            totalXp = finalXp,
            breakdown = breakdown,
            streakDays = streakDays,
            streakMultiplier = streakMultiplier,
        )
    }

    /**
     * Convenience overload that extracts fields from a WorkoutSnapshot.
     */
    fun calculateXp(
        snapshot: WorkoutSnapshot,
        weatherCondition: WeatherCondition? = null,
        weatherTemp: WeatherTemp? = null,
        streakDays: Int = 0,
    ): XpResult {
        val durationMillis = snapshot.endTime.toEpochMilli() - snapshot.startTime.toEpochMilli() - snapshot.totalPausedDurationMillis
        val lapsPhase = snapshot.phases.find { it.type == PhaseType.LAPS }
        return calculateXp(
            distanceMeters = snapshot.totalDistanceMeters,
            durationMillis = durationMillis,
            activityType = snapshot.activityType,
            lapCount = lapsPhase?.laps?.size ?: 0,
            hasWarmup = snapshot.phases.any { it.type == PhaseType.WARMUP },
            hasCooldown = snapshot.phases.any { it.type == PhaseType.COOLDOWN },
            hasLaps = lapsPhase != null,
            weatherCondition = weatherCondition,
            weatherTemp = weatherTemp,
            streakDays = streakDays,
        )
    }

    private fun weatherMultiplier(
        condition: WeatherCondition?,
        temp: WeatherTemp?,
    ): Double {
        var multiplier = 1.0
        if (condition == WeatherCondition.RAINY || condition == WeatherCondition.SNOWY) {
            multiplier = maxOf(multiplier, 1.25)
        }
        if (temp == WeatherTemp.HOT || temp == WeatherTemp.COLD) {
            multiplier = maxOf(multiplier, 1.1)
        }
        return multiplier
    }

    private const val XP_PER_SORENESS_LOG = 5

    /**
     * Calculate XP earned from a daily soreness check-in.
     * Base: +5 XP + intensity bonus (MILD:0, MODERATE:3, SEVERE:5).
     * Streak multiplier applied same as workouts.
     */
    fun calculateSorenessXp(intensity: SorenessIntensity, streakDays: Int = 0): XpResult {
        val breakdown = mutableListOf<String>()
        var rawXp = XP_PER_SORENESS_LOG
        breakdown.add("+$XP_PER_SORENESS_LOG XP: soreness check-in")

        if (intensity.xpBonus > 0) {
            breakdown.add("+${intensity.xpBonus} XP: ${intensity.displayName.lowercase()} intensity bonus")
            rawXp += intensity.xpBonus
        }

        val streakMultiplier = StreakCalculator.getMultiplier(streakDays)
        if (streakMultiplier > 1.0) {
            val bonusXp = ((rawXp * streakMultiplier) - rawXp).toInt()
            if (bonusXp > 0) {
                breakdown.add("+$bonusXp XP: $streakDays-day streak (${StreakCalculator.getMultiplierLabel(streakDays)})")
            }
            rawXp = (rawXp * streakMultiplier).toInt()
        }

        return XpResult(
            totalXp = max(rawXp, MINIMUM_XP),
            breakdown = breakdown,
            streakDays = streakDays,
            streakMultiplier = streakMultiplier,
        )
    }

    private const val XP_PER_SET = 3
    private const val XP_WEIGHT_BONUS_PER_SET = 1
    private const val XP_VOLUME_BONUS = 10
    private const val VOLUME_THRESHOLD = 10

    /**
     * Calculate XP earned from a completed exercise session.
     * Base: +3 XP per set. Weight bonus: +1 XP/set for weighted exercises.
     * Volume bonus: +10 XP if 10+ sets. Streak multiplier applied.
     */
    fun calculateSessionXp(totalSets: Int, weightedSets: Int = 0, streakDays: Int = 0): XpResult {
        val breakdown = mutableListOf<String>()
        var rawXp = 0

        val baseXp = totalSets * XP_PER_SET
        if (baseXp > 0) {
            breakdown.add("+$baseXp XP: $totalSets sets")
        }
        rawXp += baseXp

        if (weightedSets > 0) {
            val weightBonus = weightedSets * XP_WEIGHT_BONUS_PER_SET
            breakdown.add("+$weightBonus XP: $weightedSets weighted sets")
            rawXp += weightBonus
        }

        if (totalSets >= VOLUME_THRESHOLD) {
            breakdown.add("+$XP_VOLUME_BONUS XP: volume bonus ($totalSets+ sets)")
            rawXp += XP_VOLUME_BONUS
        }

        val streakMultiplier = StreakCalculator.getMultiplier(streakDays)
        if (streakMultiplier > 1.0) {
            val bonusXp = ((rawXp * streakMultiplier) - rawXp).toInt()
            if (bonusXp > 0) {
                breakdown.add("+$bonusXp XP: $streakDays-day streak (${StreakCalculator.getMultiplierLabel(streakDays)})")
            }
            rawXp = (rawXp * streakMultiplier).toInt()
        }

        return XpResult(
            totalXp = max(rawXp, MINIMUM_XP),
            breakdown = breakdown,
            streakDays = streakDays,
            streakMultiplier = streakMultiplier,
        )
    }

    private const val XP_PER_WEIGH_IN = 5

    /**
     * Calculate XP earned from a daily weigh-in.
     * Base: +5 XP. Streak multiplier applied same as workouts.
     */
    fun calculateWeighInXp(streakDays: Int = 0): XpResult {
        val breakdown = mutableListOf<String>()
        var rawXp = XP_PER_WEIGH_IN
        breakdown.add("+$XP_PER_WEIGH_IN XP: daily weigh-in")

        val streakMultiplier = StreakCalculator.getMultiplier(streakDays)
        if (streakMultiplier > 1.0) {
            val bonusXp = ((rawXp * streakMultiplier) - rawXp).toInt()
            if (bonusXp > 0) {
                breakdown.add("+$bonusXp XP: $streakDays-day streak (${StreakCalculator.getMultiplierLabel(streakDays)})")
            }
            rawXp = (rawXp * streakMultiplier).toInt()
        }

        return XpResult(
            totalXp = max(rawXp, MINIMUM_XP),
            breakdown = breakdown,
            streakDays = streakDays,
            streakMultiplier = streakMultiplier,
        )
    }

    /**
     * Total XP needed to reach a given level.
     * Level 1 = 0 XP, Level 2 = 100, Level 3 = 250, Level 4 = 450...
     * Each level N costs 50*N XP. Total = 50 * N * (N-1) / 2 + 50*(N-1)
     * Simplified: sum of 50*i for i in 1..(N-1) = 25*N*(N-1)
     */
    fun xpForLevel(level: Int): Int {
        if (level <= 1) return 0
        return 25 * level * (level - 1)
    }

    /**
     * Determine the level for a given total XP amount.
     * Inverse of xpForLevel: solve 25*L*(L-1) <= totalXp
     */
    fun levelForXp(totalXp: Int): Int {
        if (totalXp <= 0) return 1
        // 25*L*(L-1) <= totalXp → L^2 - L - totalXp/25 <= 0
        // L = (1 + sqrt(1 + 4*totalXp/25)) / 2
        val level = floor((1.0 + sqrt(1.0 + 4.0 * totalXp / 25.0)) / 2.0).toInt()
        // Verify and adjust
        return if (xpForLevel(level) <= totalXp) level else level - 1
    }

    /**
     * XP cost to go from level to level+1.
     */
    fun xpCostForLevel(level: Int): Int = 50 * level
}
