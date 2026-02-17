package com.gitfast.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gitfast.app.data.local.GitFastDatabase
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WorkoutStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GitFastDatabaseTest {

    private lateinit var database: GitFastDatabase
    private lateinit var dao: WorkoutDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GitFastDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.workoutDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // --- Helper factories ---

    private fun createWorkout(
        id: String = "workout-1",
        startTime: Long = 1000L,
        endTime: Long? = 2000L,
        totalSteps: Int = 500,
        distanceMeters: Double = 1609.34,
        status: WorkoutStatus = WorkoutStatus.COMPLETED
    ) = WorkoutEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        totalSteps = totalSteps,
        distanceMeters = distanceMeters,
        status = status
    )

    private fun createPhase(
        id: String = "phase-1",
        workoutId: String = "workout-1",
        type: PhaseType = PhaseType.WARMUP,
        startTime: Long = 1000L,
        endTime: Long? = 2000L,
        distanceMeters: Double = 1609.34,
        steps: Int = 500
    ) = WorkoutPhaseEntity(
        id = id,
        workoutId = workoutId,
        type = type,
        startTime = startTime,
        endTime = endTime,
        distanceMeters = distanceMeters,
        steps = steps
    )

    private fun createLap(
        id: String = "lap-1",
        phaseId: String = "phase-1",
        lapNumber: Int = 1,
        startTime: Long = 1000L,
        endTime: Long? = 1500L,
        distanceMeters: Double = 400.0,
        steps: Int = 200
    ) = LapEntity(
        id = id,
        phaseId = phaseId,
        lapNumber = lapNumber,
        startTime = startTime,
        endTime = endTime,
        distanceMeters = distanceMeters,
        steps = steps
    )

    private fun createGpsPoint(
        workoutId: String = "workout-1",
        latitude: Double = 40.7128,
        longitude: Double = -74.0060,
        timestamp: Long = 1000L,
        accuracy: Float = 5.0f,
        sortIndex: Int = 0
    ) = GpsPointEntity(
        workoutId = workoutId,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        accuracy = accuracy,
        sortIndex = sortIndex
    )

    // --- Tests ---

    @Test
    fun insertWorkoutAndRetrieveById() = runTest {
        val workout = createWorkout()
        dao.insertWorkout(workout)

        val retrieved = dao.getWorkoutById("workout-1")

        assertNotNull(retrieved)
        assertEquals("workout-1", retrieved!!.id)
        assertEquals(1000L, retrieved.startTime)
        assertEquals(2000L, retrieved.endTime)
        assertEquals(500, retrieved.totalSteps)
        assertEquals(1609.34, retrieved.distanceMeters, 0.01)
        assertEquals(WorkoutStatus.COMPLETED, retrieved.status)
    }

    @Test
    fun insertWorkoutWithPhasesAndRetrievePhasesByWorkoutId() = runTest {
        val workout = createWorkout()
        dao.insertWorkout(workout)

        val phase1 = createPhase(id = "phase-1", startTime = 1000L, type = PhaseType.WARMUP)
        val phase2 = createPhase(id = "phase-2", startTime = 1500L, type = PhaseType.LAPS)
        dao.insertPhase(phase1)
        dao.insertPhase(phase2)

        val phases = dao.getPhasesForWorkout("workout-1")

        assertEquals(2, phases.size)
        // Ordered by startTime ASC
        assertEquals("phase-1", phases[0].id)
        assertEquals(PhaseType.WARMUP, phases[0].type)
        assertEquals("phase-2", phases[1].id)
        assertEquals(PhaseType.LAPS, phases[1].type)
    }

    @Test
    fun insertLapsForPhaseAndRetrieveOrderedByLapNumber() = runTest {
        val workout = createWorkout()
        dao.insertWorkout(workout)
        val phase = createPhase()
        dao.insertPhase(phase)

        // Insert out of order
        val lap3 = createLap(id = "lap-3", lapNumber = 3, startTime = 1800L)
        val lap1 = createLap(id = "lap-1", lapNumber = 1, startTime = 1000L)
        val lap2 = createLap(id = "lap-2", lapNumber = 2, startTime = 1400L)
        dao.insertLap(lap3)
        dao.insertLap(lap1)
        dao.insertLap(lap2)

        val laps = dao.getLapsForPhase("phase-1")

        assertEquals(3, laps.size)
        assertEquals(1, laps[0].lapNumber)
        assertEquals(2, laps[1].lapNumber)
        assertEquals(3, laps[2].lapNumber)
    }

    @Test
    fun insertGpsPointsAndRetrieveOrderedBySortIndex() = runTest {
        val workout = createWorkout()
        dao.insertWorkout(workout)

        // Insert out of order
        val point3 = createGpsPoint(latitude = 40.71, sortIndex = 2)
        val point1 = createGpsPoint(latitude = 40.72, sortIndex = 0)
        val point2 = createGpsPoint(latitude = 40.73, sortIndex = 1)
        dao.insertGpsPoint(point3)
        dao.insertGpsPoint(point1)
        dao.insertGpsPoint(point2)

        val points = dao.getGpsPointsForWorkout("workout-1")

        assertEquals(3, points.size)
        assertEquals(0, points[0].sortIndex)
        assertEquals(40.72, points[0].latitude, 0.001)
        assertEquals(1, points[1].sortIndex)
        assertEquals(40.73, points[1].latitude, 0.001)
        assertEquals(2, points[2].sortIndex)
        assertEquals(40.71, points[2].latitude, 0.001)
    }

    @Test
    fun deleteWorkoutCascadesDeletesToPhasesLapsAndGpsPoints() = runTest {
        // Insert full hierarchy
        val workout = createWorkout()
        dao.insertWorkout(workout)

        val phase = createPhase()
        dao.insertPhase(phase)

        val lap = createLap()
        dao.insertLap(lap)

        val gpsPoint = createGpsPoint()
        dao.insertGpsPoint(gpsPoint)

        // Verify data exists
        assertNotNull(dao.getWorkoutById("workout-1"))
        assertEquals(1, dao.getPhasesForWorkout("workout-1").size)
        assertEquals(1, dao.getLapsForPhase("phase-1").size)
        assertEquals(1, dao.getGpsPointsForWorkout("workout-1").size)

        // Delete workout
        dao.deleteWorkout("workout-1")

        // Verify cascading deletes
        assertNull(dao.getWorkoutById("workout-1"))
        assertTrue(dao.getPhasesForWorkout("workout-1").isEmpty())
        assertTrue(dao.getLapsForPhase("phase-1").isEmpty())
        assertTrue(dao.getGpsPointsForWorkout("workout-1").isEmpty())
    }

    @Test
    fun getAllCompletedWorkoutsReturnsOnlyCompletedOrderedByStartTimeDesc() = runTest {
        // Insert workouts with different statuses
        dao.insertWorkout(createWorkout(id = "w-active", status = WorkoutStatus.ACTIVE, startTime = 3000L, endTime = null))
        dao.insertWorkout(createWorkout(id = "w-paused", status = WorkoutStatus.PAUSED, startTime = 2000L, endTime = null))
        dao.insertWorkout(createWorkout(id = "w-completed-old", status = WorkoutStatus.COMPLETED, startTime = 1000L))
        dao.insertWorkout(createWorkout(id = "w-completed-new", status = WorkoutStatus.COMPLETED, startTime = 4000L))

        val completed = dao.getAllCompletedWorkouts().first()

        assertEquals(2, completed.size)
        // Ordered by startTime DESC (newest first)
        assertEquals("w-completed-new", completed[0].id)
        assertEquals("w-completed-old", completed[1].id)
        // Verify all are COMPLETED
        assertTrue(completed.all { it.status == WorkoutStatus.COMPLETED })
    }

    @Test
    fun getActiveWorkoutReturnsActiveWorkoutWhenOneExists() = runTest {
        dao.insertWorkout(createWorkout(id = "w-completed", status = WorkoutStatus.COMPLETED))
        dao.insertWorkout(createWorkout(id = "w-active", status = WorkoutStatus.ACTIVE, endTime = null))

        val active = dao.getActiveWorkout()

        assertNotNull(active)
        assertEquals("w-active", active!!.id)
        assertEquals(WorkoutStatus.ACTIVE, active.status)
    }

    @Test
    fun getActiveWorkoutReturnsNullWhenNoActiveWorkoutsExist() = runTest {
        dao.insertWorkout(createWorkout(id = "w-completed-1", status = WorkoutStatus.COMPLETED))
        dao.insertWorkout(createWorkout(id = "w-completed-2", status = WorkoutStatus.COMPLETED))

        val active = dao.getActiveWorkout()

        assertNull(active)
    }

    @Test
    fun getCompletedWorkoutCountReturnsCorrectCount() = runTest {
        assertEquals(0, dao.getCompletedWorkoutCount())

        dao.insertWorkout(createWorkout(id = "w-1", status = WorkoutStatus.COMPLETED))
        dao.insertWorkout(createWorkout(id = "w-2", status = WorkoutStatus.COMPLETED))
        dao.insertWorkout(createWorkout(id = "w-3", status = WorkoutStatus.ACTIVE, endTime = null))

        assertEquals(2, dao.getCompletedWorkoutCount())
    }
}
