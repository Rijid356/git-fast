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
        coEvery { mockDao.getPhasesForWorkout("w-flow") } returns emptyList()

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
}
