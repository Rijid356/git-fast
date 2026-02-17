package com.gitfast.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gitfast.app.data.local.GitFastDatabase
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.WorkoutRepository
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
class WorkoutRepositoryTest {

    private lateinit var database: GitFastDatabase
    private lateinit var dao: WorkoutDao
    private lateinit var repository: WorkoutRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GitFastDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.workoutDao()
        repository = WorkoutRepository(dao)
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
        status: WorkoutStatus = WorkoutStatus.COMPLETED,
        activityType: ActivityType = ActivityType.RUN,
        dogName: String? = null,
        notes: String? = null,
        weatherCondition: WeatherCondition? = null,
        weatherTemp: WeatherTemp? = null,
        energyLevel: EnergyLevel? = null,
        routeTag: String? = null
    ) = WorkoutEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        totalSteps = totalSteps,
        distanceMeters = distanceMeters,
        status = status,
        activityType = activityType,
        dogName = dogName,
        notes = notes,
        weatherCondition = weatherCondition,
        weatherTemp = weatherTemp,
        energyLevel = energyLevel,
        routeTag = routeTag
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
    fun saveWorkoutPersistsWorkoutPhasesLapsAndGpsPoints() = runTest {
        val workout = createWorkout()
        val phases = listOf(createPhase())
        val laps = listOf(createLap())
        val gpsPoints = listOf(
            createGpsPoint(sortIndex = 0),
            createGpsPoint(sortIndex = 1, latitude = 40.72)
        )

        repository.saveWorkout(workout, phases, laps, gpsPoints)

        // Verify all data was persisted via DAO
        assertNotNull(dao.getWorkoutById("workout-1"))
        assertEquals(1, dao.getPhasesForWorkout("workout-1").size)
        assertEquals(1, dao.getLapsForPhase("phase-1").size)
        assertEquals(2, dao.getGpsPointsForWorkout("workout-1").size)
    }

    @Test
    fun getWorkoutWithDetailsAssemblesFullDomainModelCorrectly() = runTest {
        val workout = createWorkout()
        val phase = createPhase()
        val lap = createLap()
        val gpsPoints = listOf(
            createGpsPoint(sortIndex = 0, latitude = 40.71),
            createGpsPoint(sortIndex = 1, latitude = 40.72)
        )

        repository.saveWorkout(workout, listOf(phase), listOf(lap), gpsPoints)

        val result = repository.getWorkoutWithDetails("workout-1")

        assertNotNull(result)
        assertEquals("workout-1", result!!.id)
        assertEquals(500, result.totalSteps)
        assertEquals(1609.34, result.distanceMeters, 0.01)
        assertEquals(WorkoutStatus.COMPLETED, result.status)

        // Verify phases are assembled
        assertEquals(1, result.phases.size)
        assertEquals(PhaseType.WARMUP, result.phases[0].type)

        // Verify laps are nested inside phase
        assertEquals(1, result.phases[0].laps.size)
        assertEquals(1, result.phases[0].laps[0].lapNumber)

        // Verify GPS points are assembled
        assertEquals(2, result.gpsPoints.size)
        assertEquals(40.71, result.gpsPoints[0].latitude, 0.001)
        assertEquals(40.72, result.gpsPoints[1].latitude, 0.001)
    }

    @Test
    fun getWorkoutWithDetailsReturnsNullForNonexistentId() = runTest {
        val result = repository.getWorkoutWithDetails("nonexistent")
        assertNull(result)
    }

    @Test
    fun getCompletedWorkoutsEmitsUpdatesWhenNewWorkoutIsSaved() = runTest {
        // Initial state: no completed workouts
        val initial = repository.getCompletedWorkouts().first()
        assertTrue(initial.isEmpty())

        // Save a completed workout
        repository.saveWorkout(
            createWorkout(id = "w-1", startTime = 1000L),
            listOf(createPhase(id = "p-1", workoutId = "w-1")),
            emptyList(),
            emptyList()
        )

        // Flow should now emit the workout
        val afterFirst = repository.getCompletedWorkouts().first()
        assertEquals(1, afterFirst.size)
        assertEquals("w-1", afterFirst[0].id)

        // Save another completed workout
        repository.saveWorkout(
            createWorkout(id = "w-2", startTime = 3000L),
            listOf(createPhase(id = "p-2", workoutId = "w-2")),
            emptyList(),
            emptyList()
        )

        val afterSecond = repository.getCompletedWorkouts().first()
        assertEquals(2, afterSecond.size)
        // Ordered by startTime DESC
        assertEquals("w-2", afterSecond[0].id)
        assertEquals("w-1", afterSecond[1].id)
    }

    @Test
    fun deleteWorkoutRemovesAllAssociatedData() = runTest {
        // Insert full hierarchy via repository
        repository.saveWorkout(
            createWorkout(),
            listOf(createPhase()),
            listOf(createLap()),
            listOf(createGpsPoint())
        )

        // Verify it exists
        assertNotNull(repository.getWorkoutWithDetails("workout-1"))

        // Delete
        repository.deleteWorkout("workout-1")

        // Verify everything is gone (including cascaded children)
        assertNull(repository.getWorkoutWithDetails("workout-1"))
        assertTrue(dao.getPhasesForWorkout("workout-1").isEmpty())
        assertTrue(dao.getLapsForPhase("phase-1").isEmpty())
        assertTrue(dao.getGpsPointsForWorkout("workout-1").isEmpty())
    }

    // --- Activity type filtering tests ---

    @Test
    fun getCompletedWorkoutsByTypeReturnsOnlyMatchingType() = runTest {
        repository.saveWorkout(
            createWorkout(id = "run-1", activityType = ActivityType.RUN),
            emptyList(), emptyList(), emptyList()
        )
        repository.saveWorkout(
            createWorkout(id = "walk-1", activityType = ActivityType.DOG_WALK, dogName = "Buddy"),
            emptyList(), emptyList(), emptyList()
        )
        repository.saveWorkout(
            createWorkout(id = "walk-2", activityType = ActivityType.DOG_WALK, dogName = "Rex"),
            emptyList(), emptyList(), emptyList()
        )

        val walks = repository.getCompletedWorkoutsByType(ActivityType.DOG_WALK).first()
        assertEquals(2, walks.size)
        assertTrue(walks.all { it.activityType == ActivityType.DOG_WALK })

        val runs = repository.getCompletedWorkoutsByType(ActivityType.RUN).first()
        assertEquals(1, runs.size)
        assertEquals(ActivityType.RUN, runs[0].activityType)
    }

    @Test
    fun getDogWalksByRouteReturnsOnlyMatchingRoute() = runTest {
        repository.saveWorkout(
            createWorkout(id = "walk-1", activityType = ActivityType.DOG_WALK, routeTag = "park"),
            emptyList(), emptyList(), emptyList()
        )
        repository.saveWorkout(
            createWorkout(id = "walk-2", activityType = ActivityType.DOG_WALK, routeTag = "park"),
            emptyList(), emptyList(), emptyList()
        )
        repository.saveWorkout(
            createWorkout(id = "walk-3", activityType = ActivityType.DOG_WALK, routeTag = "lake"),
            emptyList(), emptyList(), emptyList()
        )

        val parkWalks = repository.getDogWalksByRoute("park").first()
        assertEquals(2, parkWalks.size)
    }

    // --- Route tag tests ---

    @Test
    fun saveAndRetrieveRouteTags() = runTest {
        repository.saveRouteTag(RouteTagEntity(name = "park-loop", createdAt = 1000L, lastUsed = 1000L))
        repository.saveRouteTag(RouteTagEntity(name = "neighborhood", createdAt = 2000L, lastUsed = 3000L))

        val tags = repository.getAllRouteTags()
        assertEquals(2, tags.size)
        assertEquals("neighborhood", tags[0].name)
        assertEquals("park-loop", tags[1].name)
    }

    @Test
    fun touchRouteTagUpdatesLastUsed() = runTest {
        repository.saveRouteTag(RouteTagEntity(name = "park-loop", createdAt = 1000L, lastUsed = 1000L))

        repository.touchRouteTag("park-loop")

        val tags = repository.getAllRouteTags()
        assertEquals(1, tags.size)
        assertTrue(tags[0].lastUsed > 1000L)
    }

    @Test
    fun getWorkoutWithDetailsIncludesDogWalkMetadata() = runTest {
        repository.saveWorkout(
            createWorkout(
                id = "walk-1",
                activityType = ActivityType.DOG_WALK,
                dogName = "Buddy",
                notes = "Great walk",
                weatherCondition = WeatherCondition.SUNNY,
                weatherTemp = WeatherTemp.MILD,
                energyLevel = EnergyLevel.NORMAL,
                routeTag = "park-loop"
            ),
            emptyList(), emptyList(), emptyList()
        )

        val result = repository.getWorkoutWithDetails("walk-1")
        assertNotNull(result)
        assertEquals(ActivityType.DOG_WALK, result!!.activityType)
        assertEquals("Buddy", result.dogName)
        assertEquals("Great walk", result.notes)
        assertEquals(WeatherCondition.SUNNY, result.weatherCondition)
        assertEquals(WeatherTemp.MILD, result.weatherTemp)
        assertEquals(EnergyLevel.NORMAL, result.energyLevel)
        assertEquals("park-loop", result.routeTag)
    }
}
