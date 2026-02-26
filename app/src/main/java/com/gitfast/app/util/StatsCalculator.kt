package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.CharacterStats
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.data.model.SorenessLog
import com.gitfast.app.data.model.Workout
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

data class StatBreakdown(
    val description: String,
    val details: List<Pair<String, String>>,
    val brackets: String,
    val decayNote: String,
)

object StatsCalculator {

    private const val MAX_STAT = 99
    private const val MIN_STAT = 1

    fun calculateAll(
        allWorkouts: List<Workout>,
        recentRuns: List<Workout>,
    ): CharacterStats {
        return CharacterStats(
            speed = calculateSpeed(recentRuns),
            endurance = calculateEndurance(allWorkouts),
            consistency = calculateConsistency(allWorkouts),
        )
    }

    fun calculateDogStats(dogWalks: List<Workout>, totalEventCount: Int = 0): CharacterStats {
        return CharacterStats(
            speed = calculateWalkSpeed(dogWalks),
            endurance = calculateEndurance(dogWalks),
            consistency = calculateConsistency(dogWalks),
            foraging = calculateForaging(totalEventCount),
        )
    }

    /**
     * Calculate Foraging stat based on total dog walk events logged.
     * Uses bracket interpolation: more events = higher stat.
     */
    fun calculateForaging(totalEventCount: Int): Int {
        if (totalEventCount <= 0) return MIN_STAT
        // 5 events→10 | 20→25 | 50→50 | 100→75 | 200+→99
        return interpolateBrackets(
            value = totalEventCount.toDouble(),
            brackets = listOf(
                5.0 to 10,
                20.0 to 25,
                50.0 to 50,
                100.0 to 75,
                200.0 to 99,
            ),
            inverted = false,
        )
    }

    /**
     * Calculate TGH (Toughness) stat based on 30-day soreness logging.
     * Weighted points: MILD=1, MODERATE=2, SEVERE=3.
     * Brackets: 0→1, 3→25, 7→50, 14→75, 25→99.
     */
    fun calculateToughness(recentLogs: List<SorenessLog>): Int {
        if (recentLogs.isEmpty()) return MIN_STAT
        val weightedPoints = recentLogs.sumOf { log: SorenessLog ->
            when (log.intensity) {
                SorenessIntensity.MILD -> 1
                SorenessIntensity.MODERATE -> 2
                SorenessIntensity.SEVERE -> 3
            } as Int
        }
        return interpolateBrackets(
            value = weightedPoints.toDouble(),
            brackets = listOf(
                0.0 to 1,
                3.0 to 25,
                7.0 to 50,
                14.0 to 75,
                25.0 to 99,
            ),
            inverted = false,
        )
    }

    fun toughnessBreakdown(recentLogs: List<SorenessLog>, effectiveScore: Int): StatBreakdown {
        val mildCount = recentLogs.count { it.intensity == SorenessIntensity.MILD }
        val moderateCount = recentLogs.count { it.intensity == SorenessIntensity.MODERATE }
        val severeCount = recentLogs.count { it.intensity == SorenessIntensity.SEVERE }
        val weightedPoints = mildCount + moderateCount * 2 + severeCount * 3

        val details = listOf(
            "Logs (30d)" to "${recentLogs.size}",
            "Mild / Mod / Sev" to "$mildCount / $moderateCount / $severeCount",
            "Weighted points" to "$weightedPoints",
            "Effective score" to "$effectiveScore",
        )

        return StatBreakdown(
            description = "Based on 30-day soreness logs (weighted by intensity)",
            details = details,
            brackets = "0\u21921 | 3\u219225 | 7\u219250 | 14\u219275 | 25\u219299",
            decayNote = "Actively decays \u2014 uses a 30-day rolling window. Log soreness daily!",
        )
    }

    /**
     * Calculate VIT (Vitality) stat based on weigh-in consistency and body fat trend.
     * 50/50 blend of frequency and trend components.
     *
     * @param weighInCount Number of weigh-ins in the last 30 days
     * @param bodyFatTrendPercent Body fat % change over 30 days (negative = improving).
     *                            Null if no body fat data available.
     */
    fun calculateVitality(weighInCount: Int, bodyFatTrendPercent: Double?): Int {
        val frequencyComponent = weighInFrequencyToStat(weighInCount)
        val trendComponent = bodyFatTrendToStat(bodyFatTrendPercent)
        return ((frequencyComponent * 0.5 + trendComponent * 0.5).roundToInt()).coerceIn(MIN_STAT, MAX_STAT)
    }

    private fun weighInFrequencyToStat(count: Int): Int {
        // 1 day→1 | 7 days→25 | 14 days→50 | 21 days→75 | 28 days→99
        return interpolateBrackets(
            value = count.toDouble(),
            brackets = listOf(
                1.0 to 1,
                7.0 to 25,
                14.0 to 50,
                21.0 to 75,
                28.0 to 99,
            ),
            inverted = false,
        )
    }

    private fun bodyFatTrendToStat(trendPercent: Double?): Int {
        if (trendPercent == null) return 50 // neutral default when no data
        // Improving (≥1% drop) → 99, Stable (±0.5%) → 75, Slight gain (0.5-2%) → 50,
        // Moderate gain (2-4%) → 25, Significant gain (>4%) → 1
        return interpolateBrackets(
            value = trendPercent,
            brackets = listOf(
                -1.0 to 99,
                -0.5 to 75,
                0.5 to 75,
                2.0 to 50,
                4.0 to 25,
                6.0 to 1,
            ),
            inverted = true, // lower (more negative) = better
        )
    }

    fun calculateSpeed(recentRuns: List<Workout>): Int {
        val validPaces = recentRuns.mapNotNull { it.averagePaceSecondsPerMile }
            .filter { it > 0 }
        if (validPaces.isEmpty()) return MIN_STAT

        val best = validPaces.min()
        val sorted = validPaces.sorted()
        val median = sorted[sorted.size / 2]
        val effectivePace = best * 0.6 + median * 0.4

        return paceToStat(effectivePace)
    }

    fun calculateEndurance(allWorkouts: List<Workout>): Int {
        if (allWorkouts.isEmpty()) return MIN_STAT

        val distances = allWorkouts.map { it.distanceMiles }
        val durations = allWorkouts.mapNotNull { it.durationMillis?.let { ms -> ms / 60_000.0 } }

        val distanceStat = calculateDistanceStat(distances)
        val durationStat = calculateDurationStat(durations)

        return ((distanceStat * 0.5 + durationStat * 0.5).roundToInt()).coerceIn(MIN_STAT, MAX_STAT)
    }

    fun calculateConsistency(
        allWorkouts: List<Workout>,
        now: Instant = Instant.now(),
    ): Int {
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val thirtyDaysAgo = today.minusDays(30)

        val recentWorkouts = allWorkouts.filter { workout ->
            val workoutDate = workout.startTime.atZone(zone).toLocalDate()
            !workoutDate.isBefore(thirtyDaysAgo) && !workoutDate.isAfter(today)
        }

        if (recentWorkouts.isEmpty()) return MIN_STAT

        val frequencyStat = frequencyToStat(recentWorkouts.size)
        val streakStat = calculateStreakStat(recentWorkouts, today, thirtyDaysAgo, zone)

        return ((frequencyStat * 0.5 + streakStat * 0.5).roundToInt()).coerceIn(MIN_STAT, MAX_STAT)
    }

    fun calculateWalkSpeed(recentWalks: List<Workout>): Int {
        val validPaces = recentWalks.mapNotNull { it.averagePaceSecondsPerMile }
            .filter { it > 0 }
        if (validPaces.isEmpty()) return MIN_STAT

        val best = validPaces.min()
        val sorted = validPaces.sorted()
        val median = sorted[sorted.size / 2]
        val effectivePace = best * 0.6 + median * 0.4

        return walkPaceToStat(effectivePace)
    }

    // --- Speed bracket mapping ---

    private fun paceToStat(paceSeconds: Double): Int {
        // Pace in seconds per mile → stat
        // sub-5:00 (300s) → 99, ~7:00 (420s) → 75, ~9:00 (540s) → 50, ~12:00 (720s) → 25, 16:00+ (960s) → 1
        return interpolateBrackets(
            value = paceSeconds,
            brackets = listOf(
                300.0 to 99,
                420.0 to 75,
                540.0 to 50,
                720.0 to 25,
                960.0 to 1,
            ),
            inverted = true, // lower pace = higher stat
        )
    }

    private fun walkPaceToStat(paceSeconds: Double): Int {
        // Walk pace brackets: sub-12:00 (720s) → 99, ~15:00 (900s) → 75, ~18:00 (1080s) → 50, ~22:00 (1320s) → 25, 30:00+ (1800s) → 1
        return interpolateBrackets(
            value = paceSeconds,
            brackets = listOf(
                720.0 to 99,
                900.0 to 75,
                1080.0 to 50,
                1320.0 to 25,
                1800.0 to 1,
            ),
            inverted = true,
        )
    }

    // --- Endurance helpers ---

    private fun calculateDistanceStat(distances: List<Double>): Int {
        if (distances.isEmpty()) return MIN_STAT
        val maxDist = distances.max()
        val recent10 = distances.take(10)
        val avgRecent = if (recent10.isNotEmpty()) recent10.average() else 0.0
        val effective = maxDist * 0.7 + avgRecent * 0.3

        // 13.1mi → 99, 6mi → 75, 3mi → 50, 1mi → 25, 0.1mi → 1
        return interpolateBrackets(
            value = effective,
            brackets = listOf(
                0.1 to 1,
                1.0 to 25,
                3.0 to 50,
                6.0 to 75,
                13.1 to 99,
            ),
            inverted = false,
        )
    }

    private fun calculateDurationStat(durations: List<Double>): Int {
        if (durations.isEmpty()) return MIN_STAT
        val maxDur = durations.max()
        val recent10 = durations.take(10)
        val avgRecent = if (recent10.isNotEmpty()) recent10.average() else 0.0
        val effective = maxDur * 0.7 + avgRecent * 0.3

        // 120min → 99, 60 → 75, 30 → 50, 15 → 25, 5min → 1
        return interpolateBrackets(
            value = effective,
            brackets = listOf(
                5.0 to 1,
                15.0 to 25,
                30.0 to 50,
                60.0 to 75,
                120.0 to 99,
            ),
            inverted = false,
        )
    }

    // --- Consistency helpers ---

    private fun frequencyToStat(count: Int): Int {
        // 28+/30d → 99, 20 → 75, 12 → 50, 6 → 25, 0-1 → 1
        return interpolateBrackets(
            value = count.toDouble(),
            brackets = listOf(
                1.0 to 1,
                6.0 to 25,
                12.0 to 50,
                20.0 to 75,
                28.0 to 99,
            ),
            inverted = false,
        )
    }

    private fun calculateStreakStat(
        recentWorkouts: List<Workout>,
        today: LocalDate,
        windowStart: LocalDate,
        zone: ZoneId,
    ): Int {
        val workoutDates = recentWorkouts
            .map { it.startTime.atZone(zone).toLocalDate() }
            .distinct()
            .sorted()

        if (workoutDates.isEmpty()) return MIN_STAT

        // Current streak (counting back from today)
        var currentStreak = 0
        var checkDate = today
        val dateSet = workoutDates.toSet()
        while (!checkDate.isBefore(windowStart) && dateSet.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Longest streak in window
        var longestStreak = 0
        var runningStreak = 1
        for (i in 1 until workoutDates.size) {
            if (workoutDates[i] == workoutDates[i - 1].plusDays(1)) {
                runningStreak++
            } else {
                longestStreak = maxOf(longestStreak, runningStreak)
                runningStreak = 1
            }
        }
        longestStreak = maxOf(longestStreak, runningStreak)

        val effectiveStreak = maxOf(currentStreak, (longestStreak * 0.7).roundToInt())

        // 14d → 99, 10 → 75, 7 → 50, 3 → 25, 0-1 → 1
        return interpolateBrackets(
            value = effectiveStreak.toDouble(),
            brackets = listOf(
                1.0 to 1,
                3.0 to 25,
                7.0 to 50,
                10.0 to 75,
                14.0 to 99,
            ),
            inverted = false,
        )
    }

    // --- Vitality ---

    fun calculateVitality(
        weighInDates: List<LocalDate>,
        bodyFatReadings: List<Pair<LocalDate, Double>>,
        now: Instant = Instant.now(),
    ): Int {
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val thirtyDaysAgo = today.minusDays(30)

        // 50% weigh-in frequency (days with readings in last 30 days)
        val recentWeighIns = weighInDates.filter { !it.isBefore(thirtyDaysAgo) && !it.isAfter(today) }
            .distinct()
            .size
        val frequencyStat = weighInFrequencyToStat(recentWeighIns)

        // 50% body fat trend
        val trendStat = bodyFatTrendToStat(bodyFatReadings, thirtyDaysAgo, today)

        return ((frequencyStat * 0.5 + trendStat * 0.5).roundToInt()).coerceIn(MIN_STAT, MAX_STAT)
    }

    private fun bodyFatTrendToStat(
        bodyFatReadings: List<Pair<LocalDate, Double>>,
        windowStart: LocalDate,
        windowEnd: LocalDate,
    ): Int {
        val recentReadings = bodyFatReadings
            .filter { !it.first.isBefore(windowStart) && !it.first.isAfter(windowEnd) }
            .sortedBy { it.first }

        if (recentReadings.size < 2) return 50 // no data → neutral default

        // Compare earliest vs latest reading in the window
        val earliest = recentReadings.first().second
        val latest = recentReadings.last().second
        val change = latest - earliest // positive = gain, negative = drop

        return when {
            change <= -1.0 -> 99      // ≥1% drop → excellent
            change <= -0.5 -> 87      // slight drop → great
            change <= 0.5 -> 75       // stable ±0.5% → good
            change <= 2.0 -> 50       // 0.5-2% gain → moderate
            change <= 4.0 -> 25       // 2-4% gain → concerning
            else -> 1                  // >4% gain → low
        }
    }

    // --- Breakdown methods ---

    fun speedBreakdown(recentRuns: List<Workout>, isWalk: Boolean): StatBreakdown {
        val validPaces = recentRuns.mapNotNull { it.averagePaceSecondsPerMile }
            .filter { it > 0 }

        val details = mutableListOf<Pair<String, String>>()
        if (validPaces.isNotEmpty()) {
            val best = validPaces.min()
            val sorted = validPaces.sorted()
            val median = sorted[sorted.size / 2]
            val effectivePace = best * 0.6 + median * 0.4
            details.add("Best pace" to formatPace(best))
            details.add("Median pace" to formatPace(median))
            details.add("Effective" to formatPace(effectivePace))
        }
        details.add("Workouts used" to "${validPaces.size}")

        val brackets = if (isWalk) {
            "12:00\u219299 | 15:00\u219275 | 18:00\u219250 | 22:00\u219225 | 30:00\u21921"
        } else {
            "5:00\u219299 | 7:00\u219275 | 9:00\u219250 | 12:00\u219225 | 16:00\u21921"
        }

        val label = if (isWalk) "walks" else "runs"
        return StatBreakdown(
            description = "Based on your last 20 $label",
            details = details,
            brackets = brackets,
            decayNote = "Uses last 20 $label. Older ones rotate out as new ones are added.",
        )
    }

    fun enduranceBreakdown(allWorkouts: List<Workout>): StatBreakdown {
        val distances = allWorkouts.map { it.distanceMiles }
        val durations = allWorkouts.mapNotNull { it.durationMillis?.let { ms -> ms / 60_000.0 } }

        val details = mutableListOf<Pair<String, String>>()
        if (distances.isNotEmpty()) {
            details.add("Max distance" to "%.2f mi".format(distances.max()))
            val recent10 = distances.take(10)
            details.add("Recent avg dist" to "%.2f mi".format(recent10.average()))
        }
        if (durations.isNotEmpty()) {
            details.add("Max duration" to "%d min".format(durations.max().toInt()))
            val recent10 = durations.take(10)
            details.add("Recent avg dur" to "%d min".format(recent10.average().toInt()))
        }
        if (distances.isEmpty() && durations.isEmpty()) {
            details.add("No data" to "Complete a workout!")
        }

        return StatBreakdown(
            description = "Distance (50%) + duration (50%)",
            details = details,
            brackets = "Dist: 0.1mi\u21921 | 1mi\u219225 | 3mi\u219250 | 6mi\u219275 | 13.1mi\u219299\n" +
                "Dur: 5min\u21921 | 15min\u219225 | 30min\u219250 | 60min\u219275 | 120min\u219299",
            decayNote = "Rarely decays \u2014 your best workout anchors 70% of the score.",
        )
    }

    fun consistencyBreakdown(
        allWorkouts: List<Workout>,
        now: Instant = Instant.now(),
    ): StatBreakdown {
        val zone = ZoneId.systemDefault()
        val today = now.atZone(zone).toLocalDate()
        val thirtyDaysAgo = today.minusDays(30)

        val recentWorkouts = allWorkouts.filter { workout ->
            val workoutDate = workout.startTime.atZone(zone).toLocalDate()
            !workoutDate.isBefore(thirtyDaysAgo) && !workoutDate.isAfter(today)
        }

        val workoutDates = recentWorkouts
            .map { it.startTime.atZone(zone).toLocalDate() }
            .distinct()
            .sorted()

        var currentStreak = 0
        var checkDate = today
        val dateSet = workoutDates.toSet()
        while (!checkDate.isBefore(thirtyDaysAgo) && dateSet.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        var longestStreak = 0
        if (workoutDates.isNotEmpty()) {
            var running = 1
            for (i in 1 until workoutDates.size) {
                if (workoutDates[i] == workoutDates[i - 1].plusDays(1)) {
                    running++
                } else {
                    longestStreak = maxOf(longestStreak, running)
                    running = 1
                }
            }
            longestStreak = maxOf(longestStreak, running)
        }

        val details = listOf(
            "30-day count" to "${recentWorkouts.size}",
            "Current streak" to "${currentStreak}d",
            "Longest streak" to "${longestStreak}d",
        )

        return StatBreakdown(
            description = "Frequency (50%) + streak (50%)",
            details = details,
            brackets = "Freq: 1\u21921 | 6\u219225 | 12\u219250 | 20\u219275 | 28\u219299\n" +
                "Streak: 1d\u21921 | 3d\u219225 | 7d\u219250 | 10d\u219275 | 14d\u219299",
            decayNote = "Actively decays \u2014 uses a 30-day rolling window.",
        )
    }

    fun vitalityBreakdown(
        weighInCount: Int,
        bodyFatTrendPercent: Double?,
        effectiveScore: Int,
    ): StatBreakdown {
        val trendLabel = when {
            bodyFatTrendPercent == null -> "No data"
            bodyFatTrendPercent <= -1.0 -> "Improving (%.1f%%)".format(bodyFatTrendPercent)
            bodyFatTrendPercent <= 0.5 -> "Stable (%+.1f%%)".format(bodyFatTrendPercent)
            bodyFatTrendPercent <= 2.0 -> "Slight gain (+%.1f%%)".format(bodyFatTrendPercent)
            bodyFatTrendPercent <= 4.0 -> "Moderate gain (+%.1f%%)".format(bodyFatTrendPercent)
            else -> "Significant gain (+%.1f%%)".format(bodyFatTrendPercent)
        }

        val details = listOf(
            "Weigh-ins (30d)" to "$weighInCount",
            "Body fat trend" to trendLabel,
            "Effective score" to "$effectiveScore",
        )

        return StatBreakdown(
            description = "Weigh-in frequency (50%) + body fat trend (50%)",
            details = details,
            brackets = "Freq: 1\u21921 | 7\u219225 | 14\u219250 | 21\u219275 | 28\u219299\n" +
                "Trend: \u22651% drop\u219299 | stable\u219275 | +2%\u219250 | +4%\u219225 | +6%\u21921",
            decayNote = "Actively decays \u2014 uses a 30-day rolling window. Track consistently!",
        )
    }

    private fun formatPace(seconds: Double): String {
        val totalSeconds = seconds.toInt()
        val minutes = totalSeconds / 60
        val secs = totalSeconds % 60
        return "%d:%02d/mi".format(minutes, secs)
    }

    // --- Generic bracket interpolation ---

    private fun interpolateBrackets(
        value: Double,
        brackets: List<Pair<Double, Int>>,
        inverted: Boolean,
    ): Int {
        // For inverted (pace): brackets go high-value→high-stat to low-value→low-stat
        // Sorted ascending by the input value
        val sorted = if (inverted) brackets.sortedBy { it.first } else brackets.sortedBy { it.first }

        if (inverted) {
            // Lower value = higher stat
            if (value <= sorted.first().first) return sorted.first().second
            if (value >= sorted.last().first) return sorted.last().second
            for (i in 0 until sorted.size - 1) {
                val (v1, s1) = sorted[i]
                val (v2, s2) = sorted[i + 1]
                if (value in v1..v2) {
                    val t = (value - v1) / (v2 - v1)
                    return (s1 + t * (s2 - s1)).roundToInt().coerceIn(MIN_STAT, MAX_STAT)
                }
            }
        } else {
            // Higher value = higher stat
            if (value <= sorted.first().first) return sorted.first().second
            if (value >= sorted.last().first) return sorted.last().second
            for (i in 0 until sorted.size - 1) {
                val (v1, s1) = sorted[i]
                val (v2, s2) = sorted[i + 1]
                if (value in v1..v2) {
                    val t = (value - v1) / (v2 - v1)
                    return (s1 + t * (s2 - s1)).roundToInt().coerceIn(MIN_STAT, MAX_STAT)
                }
            }
        }

        return MIN_STAT
    }
}
