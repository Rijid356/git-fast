package com.gitfast.app.util

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Lap
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutPhase
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PersonalRecordsCalculatorTest {

    @Test
    fun `empty runs returns empty records`() {
        val result = PersonalRecordsCalculator.calculateRunRecords(emptyList(), emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `fastest pace selected from multiple runs`() {
        val runs = listOf(
            createRun("r1", distanceMeters = 3000.0, startMs = 0, endMs = 1200_000),  // slow
            createRun("r2", distanceMeters = 3000.0, startMs = 0, endMs = 900_000),    // fastest
            createRun("r3", distanceMeters = 3000.0, startMs = 0, endMs = 1000_000),
        )
        val records = PersonalRecordsCalculator.calculateRunRecords(runs, emptyList())
        val fastestPace = records.find { it.title == "FASTEST PACE" }

        assertEquals("r2", fastestPace?.workoutId)
    }

    @Test
    fun `longest run by distance`() {
        val runs = listOf(
            createRun("r1", distanceMeters = 3000.0),
            createRun("r2", distanceMeters = 8000.0),
            createRun("r3", distanceMeters = 5000.0),
        )
        val records = PersonalRecordsCalculator.calculateRunRecords(runs, emptyList())
        val longestRun = records.find { it.title == "LONGEST RUN" }

        assertEquals("r2", longestRun?.workoutId)
    }

    @Test
    fun `longest duration run`() {
        val runs = listOf(
            createRun("r1", startMs = 0, endMs = 1800_000),  // 30 min
            createRun("r2", startMs = 0, endMs = 3600_000),  // 60 min — longest
            createRun("r3", startMs = 0, endMs = 2400_000),  // 40 min
        )
        val records = PersonalRecordsCalculator.calculateRunRecords(runs, emptyList())
        val longestDuration = records.find { it.title == "LONGEST DURATION" }

        assertEquals("r2", longestDuration?.workoutId)
    }

    @Test
    fun `best lap found across all workouts`() {
        val lap1 = Lap("l1", 1, Instant.ofEpochMilli(0), Instant.ofEpochMilli(120_000), 400.0, 0)
        val lap2 = Lap("l2", 2, Instant.ofEpochMilli(120_000), Instant.ofEpochMilli(210_000), 400.0, 0) // 90s — best
        val lap3 = Lap("l3", 1, Instant.ofEpochMilli(0), Instant.ofEpochMilli(150_000), 400.0, 0)

        val phase1 = WorkoutPhase("p1", PhaseType.LAPS, Instant.ofEpochMilli(0), Instant.ofEpochMilli(210_000), 800.0, 0, listOf(lap1, lap2))
        val phase2 = WorkoutPhase("p2", PhaseType.LAPS, Instant.ofEpochMilli(0), Instant.ofEpochMilli(150_000), 400.0, 0, listOf(lap3))

        val runsWithLaps = listOf(
            createRun("r1", phases = listOf(phase1)),
            createRun("r2", phases = listOf(phase2)),
        )

        val records = PersonalRecordsCalculator.calculateRunRecords(emptyList(), runsWithLaps)
        val bestLap = records.find { it.title == "BEST LAP" }

        assertEquals("r1", bestLap?.workoutId)
        assertTrue(bestLap!!.context.contains("Lap 2"))
    }

    @Test
    fun `runs with no laps skip best lap record`() {
        val runs = listOf(createRun("r1"))
        val records = PersonalRecordsCalculator.calculateRunRecords(runs, emptyList())

        assertEquals(null, records.find { it.title == "BEST LAP" })
    }

    @Test
    fun `empty walks returns empty records`() {
        val result = PersonalRecordsCalculator.calculateWalkRecords(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `longest walk by distance`() {
        val walks = listOf(
            createWalk("w1", distanceMeters = 1000.0, routeTag = "Park"),
            createWalk("w2", distanceMeters = 3500.0, routeTag = "City"),
            createWalk("w3", distanceMeters = 2000.0, routeTag = null),
        )
        val records = PersonalRecordsCalculator.calculateWalkRecords(walks)
        val longestWalk = records.find { it.title == "LONGEST WALK" }

        assertEquals("w2", longestWalk?.workoutId)
        assertTrue(longestWalk!!.context.contains("City"))
    }

    @Test
    fun `most steps walk`() {
        val walks = listOf(
            createWalk("w1", steps = 2000),
            createWalk("w2", steps = 5000),
            createWalk("w3", steps = 3000),
        )
        val records = PersonalRecordsCalculator.calculateWalkRecords(walks)
        val mostSteps = records.find { it.title == "MOST STEPS" }

        assertEquals("w2", mostSteps?.workoutId)
        assertTrue(mostSteps!!.value.contains("5,000"))
    }

    @Test
    fun `walks with no steps skip most steps record`() {
        val walks = listOf(createWalk("w1", steps = 0))
        val records = PersonalRecordsCalculator.calculateWalkRecords(walks)

        assertEquals(null, records.find { it.title == "MOST STEPS" })
    }

    @Test
    fun `overall records include streak and totals`() {
        val allWorkouts = listOf(
            createRun("r1", startMs = 1000),
            createWalk("w1", startMs = 2000),
        )
        val records = PersonalRecordsCalculator.calculateOverallRecords(
            allWorkouts = allWorkouts,
            totalRunDistance = 5000.0,
            totalWalkDistance = 2000.0,
            longestStreak = 3,
        )

        assertEquals(3, records.size)
        assertEquals("BEST STREAK", records[0].title)
        assertTrue(records[0].value.contains("3"))
        assertEquals("TOTAL WORKOUTS", records[1].title)
        assertEquals("2", records[1].value)
        assertEquals("TOTAL DISTANCE", records[2].title)
    }

    @Test
    fun `overall records skip streak when zero`() {
        val allWorkouts = listOf(createRun("r1"))
        val records = PersonalRecordsCalculator.calculateOverallRecords(
            allWorkouts = allWorkouts,
            totalRunDistance = 1000.0,
            totalWalkDistance = 0.0,
            longestStreak = 0,
        )

        assertEquals(null, records.find { it.title == "BEST STREAK" })
        assertEquals(2, records.size)
    }

    // --- Helpers ---

    private fun createRun(
        id: String,
        distanceMeters: Double = 3000.0,
        startMs: Long = 0,
        endMs: Long = 1800_000,
        steps: Int = 0,
        phases: List<WorkoutPhase> = emptyList(),
    ) = Workout(
        id = id,
        startTime = Instant.ofEpochMilli(startMs),
        endTime = Instant.ofEpochMilli(endMs),
        totalSteps = steps,
        distanceMeters = distanceMeters,
        status = WorkoutStatus.COMPLETED,
        activityType = ActivityType.RUN,
        phases = phases,
        gpsPoints = emptyList(),
        dogName = null,
        notes = null,
        weatherCondition = null,
        weatherTemp = null,
        energyLevel = null,
        routeTag = null,
    )

    private fun createWalk(
        id: String,
        distanceMeters: Double = 1500.0,
        startMs: Long = 0,
        endMs: Long = 1800_000,
        steps: Int = 2000,
        routeTag: String? = null,
    ) = Workout(
        id = id,
        startTime = Instant.ofEpochMilli(startMs),
        endTime = Instant.ofEpochMilli(endMs),
        totalSteps = steps,
        distanceMeters = distanceMeters,
        status = WorkoutStatus.COMPLETED,
        activityType = ActivityType.DOG_WALK,
        phases = emptyList(),
        gpsPoints = emptyList(),
        dogName = null,
        notes = null,
        weatherCondition = null,
        weatherTemp = null,
        energyLevel = null,
        routeTag = routeTag,
    )
}
