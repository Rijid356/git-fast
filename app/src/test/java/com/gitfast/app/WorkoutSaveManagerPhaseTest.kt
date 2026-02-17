package com.gitfast.app

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.repository.WorkoutSaveManager
import com.gitfast.app.service.WorkoutSnapshot
import com.gitfast.app.service.WorkoutStateManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class WorkoutSaveManagerPhaseTest {

    private lateinit var fakeDao: FakeWorkoutDao
    private lateinit var saveManager: WorkoutSaveManager

    @Before
    fun setUp() {
        fakeDao = FakeWorkoutDao()
        saveManager = WorkoutSaveManager(fakeDao, CharacterRepository(FakeCharacterDao()), WorkoutRepository(fakeDao))
    }

    private fun createPhaseData(
        type: PhaseType,
        startMillis: Long,
        endMillis: Long,
        distance: Double = 0.0,
        laps: List<WorkoutStateManager.LapData> = emptyList()
    ): WorkoutStateManager.PhaseData {
        return WorkoutStateManager.PhaseData(
            type = type,
            startTime = Instant.ofEpochMilli(startMillis),
            endTime = Instant.ofEpochMilli(endMillis),
            distanceMeters = distance,
            steps = 0,
            laps = laps
        )
    }

    private fun createLapData(
        lapNumber: Int,
        startMillis: Long,
        endMillis: Long,
        distance: Double = 100.0
    ): WorkoutStateManager.LapData {
        return WorkoutStateManager.LapData(
            lapNumber = lapNumber,
            startTime = Instant.ofEpochMilli(startMillis),
            endTime = Instant.ofEpochMilli(endMillis),
            distanceMeters = distance,
            steps = 0,
            gpsStartIndex = 0,
            gpsEndIndex = 0
        )
    }

    private fun createSnapshot(
        workoutId: String = "w-phase-test",
        startTime: Instant = Instant.ofEpochMilli(1000L),
        endTime: Instant = Instant.ofEpochMilli(20000L),
        gpsPoints: List<GpsPoint> = emptyList(),
        totalDistanceMeters: Double = 500.0,
        phases: List<WorkoutStateManager.PhaseData>
    ): WorkoutSnapshot {
        return WorkoutSnapshot(
            workoutId = workoutId,
            startTime = startTime,
            endTime = endTime,
            gpsPoints = gpsPoints,
            totalDistanceMeters = totalDistanceMeters,
            totalPausedDurationMillis = 0L,
            phases = phases,
            activityType = ActivityType.RUN
        )
    }

    @Test
    fun `single phase workout saves one phase entity`() = runTest {
        val phases = listOf(
            createPhaseData(PhaseType.WARMUP, 1000L, 5000L, 200.0)
        )
        val snapshot = createSnapshot(phases = phases, totalDistanceMeters = 200.0)

        saveManager.saveCompletedWorkout(snapshot)

        assertEquals(1, fakeDao.savedPhases.size)
        assertEquals(PhaseType.WARMUP, fakeDao.savedPhases[0].type)
        assertEquals(1000L, fakeDao.savedPhases[0].startTime)
        assertEquals(5000L, fakeDao.savedPhases[0].endTime)
        assertEquals(200.0, fakeDao.savedPhases[0].distanceMeters, 0.001)
    }

    @Test
    fun `three phase workout saves three phase entities in order`() = runTest {
        val phases = listOf(
            createPhaseData(PhaseType.WARMUP, 1000L, 5000L, 100.0),
            createPhaseData(
                PhaseType.LAPS, 5000L, 15000L, 300.0,
                laps = listOf(
                    createLapData(1, 5000L, 8000L, 100.0),
                    createLapData(2, 8000L, 11000L, 100.0),
                    createLapData(3, 11000L, 15000L, 100.0)
                )
            ),
            createPhaseData(PhaseType.COOLDOWN, 15000L, 20000L, 100.0)
        )
        val snapshot = createSnapshot(phases = phases, totalDistanceMeters = 500.0)

        saveManager.saveCompletedWorkout(snapshot)

        assertEquals(3, fakeDao.savedPhases.size)
        assertEquals(PhaseType.WARMUP, fakeDao.savedPhases[0].type)
        assertEquals(PhaseType.LAPS, fakeDao.savedPhases[1].type)
        assertEquals(PhaseType.COOLDOWN, fakeDao.savedPhases[2].type)
    }

    @Test
    fun `laps saved with correct phaseId linking`() = runTest {
        val laps = listOf(
            createLapData(1, 5000L, 8000L),
            createLapData(2, 8000L, 11000L)
        )
        val phases = listOf(
            createPhaseData(PhaseType.WARMUP, 1000L, 5000L),
            createPhaseData(PhaseType.LAPS, 5000L, 15000L, laps = laps),
            createPhaseData(PhaseType.COOLDOWN, 15000L, 20000L)
        )
        val snapshot = createSnapshot(phases = phases)

        saveManager.saveCompletedWorkout(snapshot)

        // All laps should reference the LAPS phase entity's id
        val lapsPhaseEntity = fakeDao.savedPhases.find { it.type == PhaseType.LAPS }
        assertNotNull(lapsPhaseEntity)

        assertEquals(2, fakeDao.savedLaps.size)
        fakeDao.savedLaps.forEach { lapEntity ->
            assertEquals(
                "Lap should be linked to LAPS phase",
                lapsPhaseEntity!!.id,
                lapEntity.phaseId
            )
        }
    }

    @Test
    fun `lap entities have correct lap numbers`() = runTest {
        val laps = listOf(
            createLapData(1, 5000L, 8000L),
            createLapData(2, 8000L, 11000L),
            createLapData(3, 11000L, 15000L)
        )
        val phases = listOf(
            createPhaseData(PhaseType.LAPS, 5000L, 15000L, laps = laps)
        )
        val snapshot = createSnapshot(phases = phases)

        saveManager.saveCompletedWorkout(snapshot)

        assertEquals(3, fakeDao.savedLaps.size)
        assertEquals(1, fakeDao.savedLaps[0].lapNumber)
        assertEquals(2, fakeDao.savedLaps[1].lapNumber)
        assertEquals(3, fakeDao.savedLaps[2].lapNumber)
    }

    @Test
    fun `GPS points saved for entire workout regardless of phases`() = runTest {
        val gpsPoints = listOf(
            GpsPoint(40.0, -74.0, Instant.ofEpochMilli(1000L), 5.0f),
            GpsPoint(40.1, -74.1, Instant.ofEpochMilli(3000L), 5.0f),
            GpsPoint(40.2, -74.2, Instant.ofEpochMilli(6000L), 5.0f),
            GpsPoint(40.3, -74.3, Instant.ofEpochMilli(10000L), 5.0f),
            GpsPoint(40.4, -74.4, Instant.ofEpochMilli(18000L), 5.0f)
        )
        val phases = listOf(
            createPhaseData(PhaseType.WARMUP, 1000L, 5000L),
            createPhaseData(PhaseType.LAPS, 5000L, 15000L),
            createPhaseData(PhaseType.COOLDOWN, 15000L, 20000L)
        )
        val snapshot = createSnapshot(phases = phases, gpsPoints = gpsPoints)

        saveManager.saveCompletedWorkout(snapshot)

        // All 5 GPS points should be saved regardless of phase boundaries
        assertEquals(5, fakeDao.savedGpsPoints.size)
        assertEquals(0, fakeDao.savedGpsPoints[0].sortIndex)
        assertEquals(4, fakeDao.savedGpsPoints[4].sortIndex)
    }

    @Test
    fun `workout without laps phase saves no lap entities`() = runTest {
        val phases = listOf(
            createPhaseData(PhaseType.WARMUP, 1000L, 5000L, 200.0)
        )
        val snapshot = createSnapshot(phases = phases)

        saveManager.saveCompletedWorkout(snapshot)

        assertTrue(fakeDao.savedLaps.isEmpty())
    }
}
