package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.ui.analytics.records.RecordItem

object PersonalRecordsCalculator {

    fun calculateRunRecords(
        runs: List<Workout>,
        runsWithLaps: List<Workout>,
    ): List<RecordItem> {
        if (runs.isEmpty()) return emptyList()
        val records = mutableListOf<RecordItem>()

        // Fastest pace
        val fastestPace = runs
            .filter { it.averagePaceSecondsPerMile != null && it.distanceMiles > 0.1 }
            .minByOrNull { it.averagePaceSecondsPerMile!! }
        fastestPace?.let {
            records.add(RecordItem(
                title = "FASTEST PACE",
                value = formatPace(it.averagePaceSecondsPerMile!!.toInt()),
                context = "${DateFormatter.shortDate(it.startTime)} \u00B7 ${formatDistance(it.distanceMeters)} run",
                workoutId = it.id,
            ))
        }

        // Longest run (distance)
        val longestRun = runs.maxByOrNull { it.distanceMeters }
        longestRun?.takeIf { it.distanceMeters > 0 }?.let {
            val duration = it.durationMillis?.let { d -> formatElapsedTime((d / 1000).toInt()) } ?: "--:--"
            records.add(RecordItem(
                title = "LONGEST RUN",
                value = formatDistance(it.distanceMeters),
                context = "${DateFormatter.shortDate(it.startTime)} \u00B7 $duration",
                workoutId = it.id,
            ))
        }

        // Longest duration
        val longestDuration = runs
            .filter { it.durationMillis != null }
            .maxByOrNull { it.durationMillis!! }
        longestDuration?.let {
            records.add(RecordItem(
                title = "LONGEST DURATION",
                value = formatElapsedTime((it.durationMillis!! / 1000).toInt()),
                context = "${DateFormatter.shortDate(it.startTime)} \u00B7 ${formatDistance(it.distanceMeters)}",
                workoutId = it.id,
            ))
        }

        // Best lap (shortest lap time)
        val allLaps = runsWithLaps.flatMap { workout ->
            workout.phases.flatMap { phase ->
                phase.laps.map { lap -> Triple(lap, workout, phase.laps.size) }
            }
        }
        val bestLap = allLaps
            .filter { (lap, _, _) -> lap.durationMillis != null && lap.durationMillis!! > 0 }
            .minByOrNull { (lap, _, _) -> lap.durationMillis!! }
        bestLap?.let { (lap, workout, totalLaps) ->
            records.add(RecordItem(
                title = "BEST LAP",
                value = formatElapsedTime((lap.durationMillis!! / 1000).toInt()),
                context = "${DateFormatter.shortDate(workout.startTime)} \u00B7 Lap ${lap.lapNumber} of $totalLaps",
                workoutId = workout.id,
            ))
        }

        return records
    }

    fun calculateWalkRecords(walks: List<Workout>): List<RecordItem> {
        if (walks.isEmpty()) return emptyList()
        val records = mutableListOf<RecordItem>()

        // Longest walk (distance)
        val longestWalk = walks.maxByOrNull { it.distanceMeters }
        longestWalk?.takeIf { it.distanceMeters > 0 }?.let {
            val tag = it.routeTag ?: ""
            records.add(RecordItem(
                title = "LONGEST WALK",
                value = formatDistance(it.distanceMeters),
                context = "${DateFormatter.shortDate(it.startTime)}${if (tag.isNotEmpty()) " \u00B7 $tag" else ""}",
                workoutId = it.id,
            ))
        }

        // Longest duration
        val longestDuration = walks
            .filter { it.durationMillis != null }
            .maxByOrNull { it.durationMillis!! }
        longestDuration?.let {
            val tag = it.routeTag ?: ""
            records.add(RecordItem(
                title = "LONGEST DURATION",
                value = formatElapsedTime((it.durationMillis!! / 1000).toInt()),
                context = "${DateFormatter.shortDate(it.startTime)}${if (tag.isNotEmpty()) " \u00B7 $tag" else ""}",
                workoutId = it.id,
            ))
        }

        // Most steps
        val mostSteps = walks
            .filter { it.totalSteps > 0 }
            .maxByOrNull { it.totalSteps }
        mostSteps?.let {
            val tag = it.routeTag ?: ""
            records.add(RecordItem(
                title = "MOST STEPS",
                value = "%,d".format(it.totalSteps),
                context = "${DateFormatter.shortDate(it.startTime)}${if (tag.isNotEmpty()) " \u00B7 $tag" else ""}",
                workoutId = it.id,
            ))
        }

        return records
    }

    fun calculateOverallRecords(
        allWorkouts: List<Workout>,
        totalRunDistance: Double,
        totalWalkDistance: Double,
        longestStreak: Int,
    ): List<RecordItem> {
        if (allWorkouts.isEmpty()) return emptyList()
        val records = mutableListOf<RecordItem>()

        // Best streak
        if (longestStreak > 0) {
            records.add(RecordItem(
                title = "BEST STREAK",
                value = "$longestStreak days",
                context = "",
                workoutId = null,
            ))
        }

        // Total workouts
        val sorted = allWorkouts.sortedBy { it.startTime }
        records.add(RecordItem(
            title = "TOTAL WORKOUTS",
            value = "${allWorkouts.size}",
            context = "Since ${DateFormatter.shortDate(sorted.first().startTime)}",
            workoutId = null,
        ))

        // Total distance with breakdown
        val totalMeters = totalRunDistance + totalWalkDistance
        records.add(RecordItem(
            title = "TOTAL DISTANCE",
            value = formatDistance(totalMeters),
            context = "Runs: ${formatDistance(totalRunDistance)} \u00B7 Walks: ${formatDistance(totalWalkDistance)}",
            workoutId = null,
        ))

        return records
    }
}
