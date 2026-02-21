package com.gitfast.app

import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.local.entity.UnlockedAchievementEntity
import com.gitfast.app.data.local.entity.XpTransactionEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.data.repository.WorkoutSaveManager
import com.gitfast.app.service.WorkoutSnapshot
import com.gitfast.app.service.WorkoutStateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class WorkoutSaveManagerTest {

    private lateinit var fakeDao: FakeWorkoutDao
    private lateinit var fakeCharacterDao: FakeCharacterDao
    private lateinit var saveManager: WorkoutSaveManager

    @Before
    fun setUp() {
        fakeDao = FakeWorkoutDao()
        fakeCharacterDao = FakeCharacterDao()
        saveManager = WorkoutSaveManager(fakeDao, CharacterRepository(fakeCharacterDao), WorkoutRepository(fakeDao), null)
    }

    private fun createSnapshot(
        workoutId: String = "workout-123",
        startTime: Instant = Instant.ofEpochMilli(1000L),
        endTime: Instant = Instant.ofEpochMilli(5000L),
        gpsPoints: List<GpsPoint> = emptyList(),
        totalDistanceMeters: Double = 1609.34,
        totalPausedDurationMillis: Long = 0L,
        phases: List<WorkoutStateManager.PhaseData>? = null
    ): WorkoutSnapshot {
        val defaultPhases = phases ?: listOf(
            WorkoutStateManager.PhaseData(
                type = PhaseType.WARMUP,
                startTime = startTime,
                endTime = endTime,
                distanceMeters = totalDistanceMeters,
                steps = 0,
                laps = emptyList()
            )
        )
        return WorkoutSnapshot(
            workoutId = workoutId,
            startTime = startTime,
            endTime = endTime,
            gpsPoints = gpsPoints,
            totalDistanceMeters = totalDistanceMeters,
            totalPausedDurationMillis = totalPausedDurationMillis,
            phases = defaultPhases,
            activityType = ActivityType.RUN
        )
    }

    // --- saveCompletedWorkout returns correct ID ---

    @Test
    fun `saveCompletedWorkout returns SaveResult on success`() = runTest {
        val snapshot = createSnapshot(workoutId = "w-abc")

        val result = saveManager.saveCompletedWorkout(snapshot)

        assertNotNull(result)
        assertEquals("w-abc", result!!.workoutId)
        assertTrue(result.xpEarned > 0)
    }

    @Test
    fun `saveCompletedWorkout returns null on database error`() = runTest {
        fakeDao.shouldThrowOnSave = true
        val snapshot = createSnapshot()

        val result = saveManager.saveCompletedWorkout(snapshot)

        assertNull(result)
    }

    // --- WorkoutEntity correctness ---

    @Test
    fun `saveCompletedWorkout builds WorkoutEntity with COMPLETED status`() = runTest {
        val snapshot = createSnapshot(
            workoutId = "w-1",
            startTime = Instant.ofEpochMilli(2000L),
            endTime = Instant.ofEpochMilli(8000L),
            totalDistanceMeters = 3218.68
        )

        saveManager.saveCompletedWorkout(snapshot)

        val workout = fakeDao.savedWorkout
        assertNotNull(workout)
        assertEquals("w-1", workout!!.id)
        assertEquals(2000L, workout.startTime)
        assertEquals(8000L, workout.endTime)
        assertEquals(3218.68, workout.distanceMeters, 0.001)
        assertEquals(WorkoutStatus.COMPLETED, workout.status)
        assertEquals(ActivityType.RUN, workout.activityType)
        assertEquals(0, workout.totalSteps)
        assertNull(workout.dogName)
        assertNull(workout.notes)
        assertNull(workout.weatherCondition)
        assertNull(workout.weatherTemp)
        assertNull(workout.energyLevel)
        assertNull(workout.routeTag)
    }

    // --- WorkoutPhaseEntity correctness ---

    @Test
    fun `saveCompletedWorkout builds single WARMUP phase`() = runTest {
        val snapshot = createSnapshot(
            workoutId = "w-1",
            startTime = Instant.ofEpochMilli(2000L),
            endTime = Instant.ofEpochMilli(8000L),
            totalDistanceMeters = 1500.0
        )

        saveManager.saveCompletedWorkout(snapshot)

        val phases = fakeDao.savedPhases
        assertEquals(1, phases.size)
        val phase = phases[0]
        assertEquals("w-1", phase.workoutId)
        assertEquals(PhaseType.WARMUP, phase.type)
        assertEquals(2000L, phase.startTime)
        assertEquals(8000L, phase.endTime)
        assertEquals(1500.0, phase.distanceMeters, 0.001)
        assertEquals(0, phase.steps)
    }

    // --- GpsPointEntity correctness ---

    @Test
    fun `saveCompletedWorkout builds GpsPointEntities with sequential sortIndex`() = runTest {
        val points = listOf(
            GpsPoint(40.7128, -74.0060, Instant.ofEpochMilli(1000L), 5.0f),
            GpsPoint(40.7130, -74.0058, Instant.ofEpochMilli(2000L), 3.5f),
            GpsPoint(40.7132, -74.0056, Instant.ofEpochMilli(3000L), 4.0f)
        )
        val snapshot = createSnapshot(workoutId = "w-1", gpsPoints = points)

        saveManager.saveCompletedWorkout(snapshot)

        val gpsEntities = fakeDao.savedGpsPoints
        assertEquals(3, gpsEntities.size)

        assertEquals(0, gpsEntities[0].sortIndex)
        assertEquals(40.7128, gpsEntities[0].latitude, 0.0001)
        assertEquals(-74.0060, gpsEntities[0].longitude, 0.0001)
        assertEquals(1000L, gpsEntities[0].timestamp)
        assertEquals(5.0f, gpsEntities[0].accuracy, 0.01f)
        assertEquals("w-1", gpsEntities[0].workoutId)

        assertEquals(1, gpsEntities[1].sortIndex)
        assertEquals(40.7130, gpsEntities[1].latitude, 0.0001)
        assertEquals(2000L, gpsEntities[1].timestamp)
        assertEquals(3.5f, gpsEntities[1].accuracy, 0.01f)

        assertEquals(2, gpsEntities[2].sortIndex)
        assertEquals(40.7132, gpsEntities[2].latitude, 0.0001)
        assertEquals(3000L, gpsEntities[2].timestamp)
        assertEquals(4.0f, gpsEntities[2].accuracy, 0.01f)
    }

    // --- Edge cases ---

    @Test
    fun `saveCompletedWorkout with empty GPS points saves without error`() = runTest {
        val snapshot = createSnapshot(gpsPoints = emptyList())

        val result = saveManager.saveCompletedWorkout(snapshot)

        assertNotNull(result)
        assertEquals("workout-123", result!!.workoutId)
        assertTrue(fakeDao.savedGpsPoints.isEmpty())
        assertNotNull(fakeDao.savedWorkout)
    }

    @Test
    fun `saveCompletedWorkout with zero distance saves correctly`() = runTest {
        val snapshot = createSnapshot(totalDistanceMeters = 0.0)

        val result = saveManager.saveCompletedWorkout(snapshot)

        assertNotNull(result)
        assertEquals("workout-123", result!!.workoutId)
        assertEquals(0.0, fakeDao.savedWorkout!!.distanceMeters, 0.001)
        assertEquals(0.0, fakeDao.savedPhases[0].distanceMeters, 0.001)
    }

    @Test
    fun `saveCompletedWorkout passes empty laps list to DAO`() = runTest {
        val snapshot = createSnapshot()

        saveManager.saveCompletedWorkout(snapshot)

        assertTrue(fakeDao.savedLaps.isEmpty())
    }
}

/**
 * Fake implementation of WorkoutDao that captures saved entities for verification.
 */
class FakeWorkoutDao : WorkoutDao {

    var savedWorkout: WorkoutEntity? = null
    var savedPhases: List<WorkoutPhaseEntity> = emptyList()
    var savedLaps: List<LapEntity> = emptyList()
    var savedGpsPoints: List<GpsPointEntity> = emptyList()
    var shouldThrowOnSave: Boolean = false

    override suspend fun saveWorkoutTransaction(
        workout: WorkoutEntity,
        phases: List<WorkoutPhaseEntity>,
        laps: List<LapEntity>,
        gpsPoints: List<GpsPointEntity>
    ) {
        if (shouldThrowOnSave) throw RuntimeException("Simulated database error")
        savedWorkout = workout
        savedPhases = phases
        savedLaps = laps
        savedGpsPoints = gpsPoints
    }

    // --- Stubs for remaining DAO methods (not used in these tests) ---

    override suspend fun insertWorkout(workout: WorkoutEntity) {}
    override suspend fun insertPhase(phase: WorkoutPhaseEntity) {}
    override suspend fun insertLap(lap: LapEntity) {}
    override suspend fun insertGpsPoint(point: GpsPointEntity) {}
    override suspend fun insertGpsPoints(points: List<GpsPointEntity>) {}
    override suspend fun updateWorkout(workout: WorkoutEntity) {}
    override suspend fun updatePhase(phase: WorkoutPhaseEntity) {}
    override suspend fun updateLap(lap: LapEntity) {}
    override suspend fun getWorkoutById(workoutId: String): WorkoutEntity? = null
    override suspend fun getPhasesForWorkout(workoutId: String): List<WorkoutPhaseEntity> = emptyList()
    override suspend fun getLapsForPhase(phaseId: String): List<LapEntity> = emptyList()
    override suspend fun getGpsPointsForWorkout(workoutId: String): List<GpsPointEntity> = emptyList()
    override fun getAllCompletedWorkouts(): Flow<List<WorkoutEntity>> = flowOf(emptyList())
    override suspend fun getAllCompletedWorkoutsOnce(): List<WorkoutEntity> = emptyList()
    override suspend fun getRecentCompletedRuns(limit: Int): List<WorkoutEntity> = emptyList()
    override suspend fun getAllCompletedRunsOnce(): List<WorkoutEntity> = emptyList()
    override fun getCompletedWorkoutsByType(activityType: String): Flow<List<WorkoutEntity>> = flowOf(emptyList())
    override fun getDogWalksByRoute(routeTag: String): Flow<List<WorkoutEntity>> = flowOf(emptyList())
    override suspend fun getCompletedWorkoutCount(): Int = 0
    override suspend fun getTotalLapCount(): Int = 0
    override suspend fun getCompletedDogWalkCount(): Int = 0
    override suspend fun getActiveWorkout(): WorkoutEntity? = null
    override suspend fun insertRouteTag(tag: RouteTagEntity) {}
    override suspend fun getAllRouteTags(): List<RouteTagEntity> = emptyList()
    override suspend fun getDistinctRouteTags(): List<String> = emptyList()
    override suspend fun updateRouteTagLastUsed(name: String, timestamp: Long) {}
    override suspend fun deleteWorkout(workoutId: String) {}
    override suspend fun deleteLap(lapId: String) {}
    override suspend fun getRecentWorkoutsWithLaps(limit: Int): List<WorkoutEntity> = emptyList()
    override suspend fun getCompletedDogWalksOnce(): List<WorkoutEntity> = emptyList()
    override suspend fun getTotalDogWalkDistanceMeters(): Double = 0.0
    override suspend fun getTotalDistanceMeters(): Double = 0.0
    override suspend fun getTotalDurationMillis(): Long = 0L
}

class FakeCharacterDao : CharacterDao {
    private val profiles = mutableMapOf<Int, CharacterProfileEntity>(
        1 to CharacterProfileEntity(id = 1, totalXp = 0, level = 1)
    )
    private val transactions = mutableListOf<XpTransactionEntity>()

    override fun getProfile(profileId: Int): Flow<CharacterProfileEntity?> = flowOf(profiles[profileId])
    override suspend fun getProfileOnce(profileId: Int): CharacterProfileEntity? = profiles[profileId]
    override suspend fun updateProfile(profile: CharacterProfileEntity) { profiles[profile.id] = profile }
    override suspend fun insertProfile(profile: CharacterProfileEntity) { profiles[profile.id] = profile }
    override suspend fun insertXpTransaction(tx: XpTransactionEntity) { transactions.add(tx) }
    override fun getRecentXpTransactions(profileId: Int, limit: Int): Flow<List<XpTransactionEntity>> = flowOf(transactions.filter { it.profileId == profileId }.take(limit))
    override suspend fun getXpTransactionForWorkout(workoutId: String, profileId: Int): XpTransactionEntity? = transactions.find { it.workoutId == workoutId && it.profileId == profileId }
    override fun getTotalTransactionCount(profileId: Int): Flow<Int> = flowOf(transactions.count { it.profileId == profileId })
    override fun getAllXpTransactions(profileId: Int): Flow<List<XpTransactionEntity>> = flowOf(transactions.filter { it.profileId == profileId })
    override fun getUnlockedAchievements(profileId: Int): Flow<List<UnlockedAchievementEntity>> = flowOf(emptyList())
    override suspend fun getUnlockedAchievementIds(profileId: Int): List<String> = emptyList()
    override suspend fun getUnlockedAchievementsOnce(profileId: Int): List<UnlockedAchievementEntity> = emptyList()
    override suspend fun getXpTransactionsOnce(profileId: Int): List<XpTransactionEntity> = transactions.filter { it.profileId == profileId }
    override suspend fun insertUnlockedAchievement(entity: UnlockedAchievementEntity) {}
}
