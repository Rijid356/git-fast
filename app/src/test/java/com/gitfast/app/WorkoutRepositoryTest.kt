package com.gitfast.app

import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class WorkoutRepositoryTest {

    private lateinit var mockDao: WorkoutDao
    private lateinit var repository: WorkoutRepository

    @Before
    fun setUp() {
        mockDao = mockk(relaxed = true)
        repository = WorkoutRepository(mockDao)
    }

    // --- Helpers ---

    private fun buildEntity(
        id: String = "w-1",
        activityType: ActivityType = ActivityType.RUN,
        distanceMeters: Double = 1609.34,
        startTime: Long = 1000L,
        endTime: Long = 5000L,
    ) = WorkoutEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        totalSteps = 0,
        distanceMeters = distanceMeters,
        status = WorkoutStatus.COMPLETED,
        activityType = activityType,
        dogName = null,
        notes = null,
        weatherCondition = null,
        weatherTemp = null,
        energyLevel = null,
        routeTag = null,
    )

    private fun buildPhaseEntity(
        id: String = "phase-1",
        workoutId: String = "w-1",
    ) = WorkoutPhaseEntity(
        id = id,
        workoutId = workoutId,
        type = PhaseType.WARMUP,
        startTime = 1000L,
        endTime = 5000L,
        distanceMeters = 1609.34,
        steps = 0,
    )

    private fun buildGpsEntity(workoutId: String = "w-1") = GpsPointEntity(
        workoutId = workoutId,
        latitude = 40.7128,
        longitude = -74.0060,
        timestamp = 2000L,
        accuracy = 5.0f,
        sortIndex = 0,
    )

    // =========================================================================
    // Simple delegation tests
    // =========================================================================

    @Test
    fun `deleteWorkout delegates to DAO`() = runTest {
        repository.deleteWorkout("w-1")
        coVerify { mockDao.deleteWorkout("w-1") }
    }

    @Test
    fun `deleteLap delegates to DAO`() = runTest {
        repository.deleteLap("lap-1")
        coVerify { mockDao.deleteLap("lap-1") }
    }

    @Test
    fun `getCompletedWorkoutCount delegates to DAO`() = runTest {
        coEvery { mockDao.getCompletedWorkoutCount() } returns 7
        assertEquals(7, repository.getCompletedWorkoutCount())
    }

    @Test
    fun `getTotalDistanceMeters delegates to DAO`() = runTest {
        coEvery { mockDao.getTotalDistanceMeters() } returns 42195.0
        assertEquals(42195.0, repository.getTotalDistanceMeters(), 0.001)
    }

    @Test
    fun `getTotalDurationMillis delegates to DAO`() = runTest {
        coEvery { mockDao.getTotalDurationMillis() } returns 3_600_000L
        assertEquals(3_600_000L, repository.getTotalDurationMillis())
    }

    @Test
    fun `getTotalLapCount delegates to DAO`() = runTest {
        coEvery { mockDao.getTotalLapCount() } returns 12
        assertEquals(12, repository.getTotalLapCount())
    }

    @Test
    fun `getCompletedDogWalkCount delegates to DAO`() = runTest {
        coEvery { mockDao.getCompletedDogWalkCount() } returns 5
        assertEquals(5, repository.getCompletedDogWalkCount())
    }

    @Test
    fun `getTotalDogWalkDistanceMeters delegates to DAO`() = runTest {
        coEvery { mockDao.getTotalDogWalkDistanceMeters() } returns 8000.0
        assertEquals(8000.0, repository.getTotalDogWalkDistanceMeters(), 0.001)
    }

    // =========================================================================
    // Mapping tests
    // =========================================================================

    @Test
    fun `getAllCompletedWorkoutsOnce maps entities to domain models`() = runTest {
        val entity = buildEntity("w-mapped")
        coEvery { mockDao.getAllCompletedWorkoutsOnce() } returns listOf(entity)

        val result = repository.getAllCompletedWorkoutsOnce()

        assertEquals(1, result.size)
        assertEquals("w-mapped", result[0].id)
        assertEquals(1609.34, result[0].distanceMeters, 0.001)
    }

    @Test
    fun `getAllCompletedWorkoutsOnce returns empty when DAO returns empty`() = runTest {
        coEvery { mockDao.getAllCompletedWorkoutsOnce() } returns emptyList()
        assertTrue(repository.getAllCompletedWorkoutsOnce().isEmpty())
    }

    @Test
    fun `getRecentCompletedRuns maps entities and passes limit`() = runTest {
        val entities = (1..3).map { buildEntity("w-$it") }
        coEvery { mockDao.getRecentCompletedRuns(3) } returns entities

        val result = repository.getRecentCompletedRuns(3)

        assertEquals(3, result.size)
        coVerify { mockDao.getRecentCompletedRuns(3) }
    }

    @Test
    fun `getCompletedDogWalks maps dog walk entities`() = runTest {
        val entity = buildEntity("dw-1", activityType = ActivityType.DOG_WALK)
        coEvery { mockDao.getCompletedDogWalksOnce() } returns listOf(entity)

        val result = repository.getCompletedDogWalks()

        assertEquals(1, result.size)
        assertEquals(ActivityType.DOG_WALK, result[0].activityType)
    }

    // =========================================================================
    // getWorkoutWithDetails
    // =========================================================================

    @Test
    fun `getWorkoutWithDetails returns null when workout not found`() = runTest {
        coEvery { mockDao.getWorkoutById("missing") } returns null

        assertNull(repository.getWorkoutWithDetails("missing"))
    }

    @Test
    fun `getWorkoutWithDetails returns workout with correct ID and distance`() = runTest {
        val entity = buildEntity("w-detail", distanceMeters = 3218.68)
        val gpsEntity = buildGpsEntity("w-detail")
        coEvery { mockDao.getWorkoutById("w-detail") } returns entity
        coEvery { mockDao.getGpsPointsForWorkout("w-detail") } returns listOf(gpsEntity)
        coEvery { mockDao.getPhasesForWorkout("w-detail") } returns emptyList()

        val result = repository.getWorkoutWithDetails("w-detail")

        assertNotNull(result)
        assertEquals("w-detail", result!!.id)
        assertEquals(3218.68, result.distanceMeters, 0.001)
        assertEquals(1, result.gpsPoints.size)
    }

    @Test
    fun `getWorkoutWithDetails maps phases and their laps`() = runTest {
        val entity = buildEntity("w-laps")
        val phase = buildPhaseEntity("phase-1", "w-laps")
        val lap = LapEntity(
            id = "lap-1", phaseId = "phase-1", lapNumber = 1,
            startTime = 1000L, endTime = 2000L, distanceMeters = 400.0,
            steps = 0, splitLatitude = null, splitLongitude = null,
        )
        coEvery { mockDao.getWorkoutById("w-laps") } returns entity
        coEvery { mockDao.getGpsPointsForWorkout("w-laps") } returns emptyList()
        coEvery { mockDao.getPhasesForWorkout("w-laps") } returns listOf(phase)
        coEvery { mockDao.getLapsForPhase("phase-1") } returns listOf(lap)

        val result = repository.getWorkoutWithDetails("w-laps")

        assertNotNull(result)
        assertEquals(1, result!!.phases.size)
        assertEquals(1, result.phases[0].laps.size)
        assertEquals(1, result.phases[0].laps[0].lapNumber)
    }

    // =========================================================================
    // Flow tests
    // =========================================================================

    @Test
    fun `getCompletedWorkouts flow emits mapped domain models`() = runTest {
        val entity = buildEntity("w-flow")
        every { mockDao.getAllCompletedWorkouts() } returns flowOf(listOf(entity))
        coEvery { mockDao.getPhasesForWorkouts(listOf("w-flow")) } returns emptyList()

        val result = repository.getCompletedWorkouts().first()

        assertEquals(1, result.size)
        assertEquals("w-flow", result[0].id)
    }

    @Test
    fun `getCompletedWorkoutsByType passes activity type name to DAO`() = runTest {
        every { mockDao.getCompletedWorkoutsByType("DOG_WALK") } returns flowOf(emptyList())

        repository.getCompletedWorkoutsByType(ActivityType.DOG_WALK).first()

        coVerify { mockDao.getCompletedWorkoutsByType("DOG_WALK") }
    }

    // =========================================================================
    // getAllRouteTagNames
    // =========================================================================

    @Test
    fun `getAllRouteTagNames returns empty when both sources empty`() = runTest {
        coEvery { mockDao.getAllRouteTags() } returns emptyList()
        coEvery { mockDao.getDistinctRouteTags() } returns emptyList()

        assertTrue(repository.getAllRouteTagNames().isEmpty())
    }

    @Test
    fun `getAllRouteTagNames merges and deduplicates db and workout tags`() = runTest {
        coEvery { mockDao.getAllRouteTags() } returns listOf(
            RouteTagEntity("Park Loop", createdAt = 1000L, lastUsed = 2000L),
            RouteTagEntity("Trail", createdAt = 500L, lastUsed = 1500L),
        )
        coEvery { mockDao.getDistinctRouteTags() } returns listOf("Beach", "Park Loop")

        val result = repository.getAllRouteTagNames()

        assertEquals(3, result.size)
        assertTrue(result.contains("Park Loop"))
        assertTrue(result.contains("Trail"))
        assertTrue(result.contains("Beach"))
        // "Park Loop" appears in both sources but deduped to 1
        assertEquals(1, result.count { it == "Park Loop" })
    }

    @Test
    fun `getAllRouteTagNames sorts db tags by lastUsed descending`() = runTest {
        coEvery { mockDao.getAllRouteTags() } returns listOf(
            RouteTagEntity("Older", createdAt = 1000L, lastUsed = 1000L),
            RouteTagEntity("Newer", createdAt = 1000L, lastUsed = 3000L),
            RouteTagEntity("Middle", createdAt = 1000L, lastUsed = 2000L),
        )
        coEvery { mockDao.getDistinctRouteTags() } returns emptyList()

        val result = repository.getAllRouteTagNames()

        assertEquals(listOf("Newer", "Middle", "Older"), result)
    }

    // =========================================================================
    // getDogWalksByRoute (Flow)
    // =========================================================================

    @Test
    fun `getDogWalksByRoute maps entities with phases`() = runTest {
        val entity = buildEntity("dw-1", activityType = ActivityType.DOG_WALK)
        every { mockDao.getDogWalksByRoute("Park Loop") } returns flowOf(listOf(entity))
        coEvery { mockDao.getPhasesForWorkouts(listOf("dw-1")) } returns emptyList()

        val result = repository.getDogWalksByRoute("Park Loop").first()

        assertEquals(1, result.size)
        assertEquals("dw-1", result[0].id)
    }

    @Test
    fun `getDogWalksByRoute returns empty flow for unknown route`() = runTest {
        every { mockDao.getDogWalksByRoute("Unknown") } returns flowOf(emptyList())

        val result = repository.getDogWalksByRoute("Unknown").first()

        assertTrue(result.isEmpty())
    }

    // =========================================================================
    // getDogWalksByRouteOnce
    // =========================================================================

    @Test
    fun `getDogWalksByRouteOnce returns mapped domain models`() = runTest {
        val entity = buildEntity("dw-1", activityType = ActivityType.DOG_WALK)
        every { mockDao.getDogWalksByRoute("Trail") } returns flowOf(listOf(entity))
        coEvery { mockDao.getPhasesForWorkouts(listOf("dw-1")) } returns emptyList()

        val result = repository.getDogWalksByRouteOnce("Trail")

        assertEquals(1, result.size)
        assertEquals("dw-1", result[0].id)
    }

    @Test
    fun `getDogWalksByRouteOnce returns empty list when no walks`() = runTest {
        every { mockDao.getDogWalksByRoute("Empty") } returns flowOf(emptyList())

        val result = repository.getDogWalksByRouteOnce("Empty")

        assertTrue(result.isEmpty())
    }

    // =========================================================================
    // saveWorkout
    // =========================================================================

    @Test
    fun `saveWorkout delegates all entities to DAO`() = runTest {
        val workout = buildEntity("w-save")
        val phases = listOf(buildPhaseEntity("p-1", "w-save"))
        val laps = listOf(
            LapEntity("lap-1", "p-1", 1, 1000L, 2000L, 400.0, 0, null, null)
        )
        val gpsPoints = listOf(buildGpsEntity("w-save"))

        repository.saveWorkout(workout, phases, laps, gpsPoints)

        coVerify { mockDao.insertWorkout(workout) }
        coVerify { mockDao.insertPhases(phases) }
        coVerify { mockDao.insertLaps(laps) }
        coVerify { mockDao.insertGpsPoints(gpsPoints) }
    }

    @Test
    fun `saveWorkout handles empty phases laps and gps`() = runTest {
        val workout = buildEntity("w-empty")

        repository.saveWorkout(workout, emptyList(), emptyList(), emptyList())

        coVerify { mockDao.insertWorkout(workout) }
        coVerify(exactly = 0) { mockDao.insertPhases(any()) }
        coVerify(exactly = 0) { mockDao.insertLaps(any()) }
        coVerify(exactly = 0) { mockDao.insertGpsPoints(any()) }
    }

    // =========================================================================
    // updateWorkout
    // =========================================================================

    @Test
    fun `updateWorkout delegates to DAO`() = runTest {
        val workout = buildEntity("w-update")

        repository.updateWorkout(workout)

        coVerify { mockDao.updateWorkout(workout) }
    }

    // =========================================================================
    // getActiveWorkout
    // =========================================================================

    @Test
    fun `getActiveWorkout returns entity from DAO`() = runTest {
        val entity = buildEntity("w-active")
        coEvery { mockDao.getActiveWorkout() } returns entity

        val result = repository.getActiveWorkout()

        assertNotNull(result)
        assertEquals("w-active", result!!.id)
    }

    @Test
    fun `getActiveWorkout returns null when no active workout`() = runTest {
        coEvery { mockDao.getActiveWorkout() } returns null

        assertNull(repository.getActiveWorkout())
    }

    // =========================================================================
    // getAllRouteTags
    // =========================================================================

    @Test
    fun `getAllRouteTags delegates to DAO`() = runTest {
        val tags = listOf(
            RouteTagEntity("Park", createdAt = 1000L, lastUsed = 2000L),
        )
        coEvery { mockDao.getAllRouteTags() } returns tags

        val result = repository.getAllRouteTags()

        assertEquals(1, result.size)
        assertEquals("Park", result[0].name)
    }

    // =========================================================================
    // saveRouteTag
    // =========================================================================

    @Test
    fun `saveRouteTag delegates to DAO`() = runTest {
        val tag = RouteTagEntity("New Route", createdAt = 1000L, lastUsed = 1000L)

        repository.saveRouteTag(tag)

        coVerify { mockDao.insertRouteTag(tag) }
    }

    // =========================================================================
    // touchRouteTag
    // =========================================================================

    @Test
    fun `touchRouteTag delegates to DAO with current time`() = runTest {
        repository.touchRouteTag("Park Loop")

        coVerify { mockDao.updateRouteTagLastUsed("Park Loop", any()) }
    }

    // =========================================================================
    // saveGpsPoints
    // =========================================================================

    @Test
    fun `saveGpsPoints delegates to DAO`() = runTest {
        val points = listOf(buildGpsEntity("w-1"), buildGpsEntity("w-1"))

        repository.saveGpsPoints(points)

        coVerify { mockDao.insertGpsPoints(points) }
    }

    // =========================================================================
    // savePhase
    // =========================================================================

    @Test
    fun `savePhase delegates to DAO`() = runTest {
        val phase = buildPhaseEntity("p-1", "w-1")

        repository.savePhase(phase)

        coVerify { mockDao.insertPhase(phase) }
    }

    // =========================================================================
    // getWorkoutsWithGpsForRouteTag
    // =========================================================================

    @Test
    fun `getWorkoutsWithGpsForRouteTag loads GPS for first 5 workouts`() = runTest {
        val entities = (1..7).map { buildEntity("w-$it", activityType = ActivityType.DOG_WALK) }
        every { mockDao.getDogWalksByRoute("Trail") } returns flowOf(entities)
        (1..5).forEach { i ->
            coEvery { mockDao.getGpsPointsForWorkout("w-$i") } returns listOf(buildGpsEntity("w-$i"))
        }

        val result = repository.getWorkoutsWithGpsForRouteTag("Trail")

        assertEquals(5, result.size)
        // Each should have GPS points loaded
        result.forEach { workout ->
            assertEquals(1, workout.gpsPoints.size)
        }
    }

    @Test
    fun `getWorkoutsWithGpsForRouteTag returns empty when no walks`() = runTest {
        every { mockDao.getDogWalksByRoute("Empty") } returns flowOf(emptyList())

        val result = repository.getWorkoutsWithGpsForRouteTag("Empty")

        assertTrue(result.isEmpty())
    }

    // =========================================================================
    // getAllRunsWithLaps
    // =========================================================================

    @Test
    fun `getAllRunsWithLaps loads phases with nested laps`() = runTest {
        val entity = buildEntity("w-run")
        val phase = buildPhaseEntity("p-1", "w-run")
        val lap = LapEntity("lap-1", "p-1", 1, 1000L, 2000L, 400.0, 0, null, null)
        coEvery { mockDao.getAllCompletedRunsOnce() } returns listOf(entity)
        coEvery { mockDao.getPhasesForWorkouts(listOf("w-run")) } returns listOf(phase)
        coEvery { mockDao.getLapsForPhases(listOf("p-1")) } returns listOf(lap)

        val result = repository.getAllRunsWithLaps()

        assertEquals(1, result.size)
        assertEquals(1, result[0].phases.size)
        assertEquals(1, result[0].phases[0].laps.size)
        assertEquals(400.0, result[0].phases[0].laps[0].distanceMeters, 0.001)
    }

    @Test
    fun `getAllRunsWithLaps returns empty when no runs`() = runTest {
        coEvery { mockDao.getAllCompletedRunsOnce() } returns emptyList()

        assertTrue(repository.getAllRunsWithLaps().isEmpty())
    }

    // =========================================================================
    // getRecentWorkoutsWithLaps
    // =========================================================================

    @Test
    fun `getRecentWorkoutsWithLaps passes limit and loads laps`() = runTest {
        val entity = buildEntity("w-recent")
        val phase = buildPhaseEntity("p-1", "w-recent")
        coEvery { mockDao.getRecentWorkoutsWithLaps(5) } returns listOf(entity)
        coEvery { mockDao.getPhasesForWorkouts(listOf("w-recent")) } returns listOf(phase)
        coEvery { mockDao.getLapsForPhases(listOf("p-1")) } returns emptyList()

        val result = repository.getRecentWorkoutsWithLaps(5)

        assertEquals(1, result.size)
        coVerify { mockDao.getRecentWorkoutsWithLaps(5) }
    }

    @Test
    fun `getRecentWorkoutsWithLaps returns empty for zero limit result`() = runTest {
        coEvery { mockDao.getRecentWorkoutsWithLaps(10) } returns emptyList()

        assertTrue(repository.getRecentWorkoutsWithLaps(10).isEmpty())
    }
}
