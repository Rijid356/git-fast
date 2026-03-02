package com.gitfast.app

import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.DogWalkEventEntity
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
import com.gitfast.app.data.sync.FirestoreSync
import com.gitfast.app.service.WorkoutSnapshot
import com.gitfast.app.service.WorkoutStateManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
        phases: List<WorkoutStateManager.PhaseData>? = null,
        activityType: ActivityType = ActivityType.RUN,
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
            activityType = activityType,
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

    // --- Dog Walk Profile Tests ---

    @Test
    fun `dog walk awards XP to both user and Juniper profiles`() = runTest {
        val snapshot = createSnapshot(activityType = ActivityType.DOG_WALK)

        saveManager.saveCompletedWorkout(snapshot)

        val tx1 = fakeCharacterDao.getXpTransactionForWorkout(snapshot.workoutId, 1)
        val tx2 = fakeCharacterDao.getXpTransactionForWorkout(snapshot.workoutId, 2)
        assertNotNull("Profile 1 should receive XP", tx1)
        assertNotNull("Juniper (profile 2) should receive XP for dog walk", tx2)
    }

    @Test
    fun `run workout does not award XP to Juniper`() = runTest {
        val snapshot = createSnapshot(activityType = ActivityType.RUN)

        saveManager.saveCompletedWorkout(snapshot)

        val tx2 = fakeCharacterDao.getXpTransactionForWorkout(snapshot.workoutId, 2)
        assertNull("Juniper should not receive XP for a run", tx2)
    }

    @Test
    fun `dog walk triggers Juniper stats recalculation`() = runTest {
        val mockCharRepo = mockk<CharacterRepository>(relaxed = true)
        coEvery { mockCharRepo.awardXp(any(), any(), any(), any()) } returns 50
        val manager = WorkoutSaveManager(fakeDao, mockCharRepo, WorkoutRepository(fakeDao), null)

        val snapshot = createSnapshot(activityType = ActivityType.DOG_WALK)
        manager.saveCompletedWorkout(snapshot)

        coVerify { mockCharRepo.updateStats(eq(2), any()) }
    }

    // --- updateDogWalkMetadata Tests ---

    @Test
    fun `updateDogWalkMetadata updates workout entity fields`() = runTest {
        fakeDao.workoutToReturn = WorkoutEntity(
            id = "w-dog",
            startTime = 1000L,
            endTime = 5000L,
            totalSteps = 0,
            distanceMeters = 1609.34,
            status = WorkoutStatus.COMPLETED,
            activityType = ActivityType.DOG_WALK,
            dogName = null,
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = null,
        )

        saveManager.updateDogWalkMetadata(
            workoutId = "w-dog",
            dogName = "Juniper",
            routeTag = "Park Loop",
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            notes = "Good walk",
        )

        assertNotNull(fakeDao.updatedWorkout)
        assertEquals("Juniper", fakeDao.updatedWorkout!!.dogName)
        assertEquals("Park Loop", fakeDao.updatedWorkout!!.routeTag)
        assertEquals("Good walk", fakeDao.updatedWorkout!!.notes)
    }

    @Test
    fun `updateDogWalkMetadata returns early when workout not found`() = runTest {
        fakeDao.workoutToReturn = null

        saveManager.updateDogWalkMetadata(
            workoutId = "missing",
            dogName = "Juniper",
            routeTag = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            notes = null,
        )

        assertEquals(0, fakeDao.updateWorkoutCallCount)
    }

    // --- Cloud Sync Tests ---

    @Test
    fun `cloud sync failure does not prevent save result`() = runTest {
        val mockSync = mockk<FirestoreSync>(relaxed = true)
        coEvery { mockSync.pushWorkout(any()) } throws RuntimeException("Sync failed")
        val manager = WorkoutSaveManager(
            fakeDao, CharacterRepository(fakeCharacterDao), WorkoutRepository(fakeDao), mockSync,
        )

        val result = manager.saveCompletedWorkout(createSnapshot())

        assertNotNull("Save should succeed even when cloud sync throws", result)
    }

    // --- SaveResult Content Tests ---

    @Test
    fun `save result has non-negative streak days and multiplier`() = runTest {
        val result = saveManager.saveCompletedWorkout(createSnapshot())

        assertNotNull(result)
        assertTrue("Streak days should be non-negative", result!!.streakDays >= 0)
        assertTrue("Streak multiplier should be at least 1.0", result.streakMultiplier >= 1.0)
    }

    @Test
    fun `save result xp earned is positive for a completed workout`() = runTest {
        val result = saveManager.saveCompletedWorkout(createSnapshot())

        assertNotNull(result)
        assertTrue("XP earned should be positive", result!!.xpEarned > 0)
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
    var workoutToReturn: WorkoutEntity? = null
    var updatedWorkout: WorkoutEntity? = null
    var updateWorkoutCallCount: Int = 0

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
    override suspend fun insertPhases(phases: List<WorkoutPhaseEntity>) { phases.forEach { insertPhase(it) } }
    override suspend fun insertLaps(laps: List<LapEntity>) { laps.forEach { insertLap(it) } }
    override suspend fun updateWorkout(workout: WorkoutEntity) {
        updatedWorkout = workout
        updateWorkoutCallCount++
    }
    override suspend fun updatePhase(phase: WorkoutPhaseEntity) {}
    override suspend fun updateLap(lap: LapEntity) {}
    override suspend fun getWorkoutById(workoutId: String): WorkoutEntity? = workoutToReturn
    override suspend fun getPhasesForWorkout(workoutId: String): List<WorkoutPhaseEntity> = emptyList()
    override suspend fun getLapsForPhase(phaseId: String): List<LapEntity> = emptyList()
    override suspend fun getPhasesForWorkouts(workoutIds: List<String>): List<WorkoutPhaseEntity> = workoutIds.flatMap { getPhasesForWorkout(it) }
    override suspend fun getLapsForPhases(phaseIds: List<String>): List<LapEntity> = phaseIds.flatMap { getLapsForPhase(it) }
    override suspend fun getGpsPointsForWorkout(workoutId: String): List<GpsPointEntity> = emptyList()
    override fun getAllCompletedWorkouts(): Flow<List<WorkoutEntity>> = flowOf(emptyList())
    override suspend fun getAllCompletedWorkoutsOnce(): List<WorkoutEntity> = emptyList()
    override suspend fun getRecentCompletedRuns(limit: Int): List<WorkoutEntity> = emptyList()
    override suspend fun getAllCompletedRunsOnce(): List<WorkoutEntity> = emptyList()
    override fun getCompletedWorkoutsByType(activityType: String): Flow<List<WorkoutEntity>> = flowOf(emptyList())
    override fun getCompletedDogActivityWorkouts(): Flow<List<WorkoutEntity>> = flowOf(emptyList())
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
    override fun getCompletedWorkoutsBetween(startMillis: Long, endMillis: Long): Flow<List<WorkoutEntity>> = flowOf(emptyList())
    override fun getActiveMillisBetween(startMillis: Long, endMillis: Long): Flow<Long> = flowOf(0L)
    override fun getDistanceMetersBetween(startMillis: Long, endMillis: Long): Flow<Double> = flowOf(0.0)
    override fun getActiveDayCountBetween(startMillis: Long, endMillis: Long): Flow<Int> = flowOf(0)
    override fun getCompletedWorkoutCountBetween(startMillis: Long, endMillis: Long): Flow<Int> = flowOf(0)
    override suspend fun insertDogWalkEvent(event: DogWalkEventEntity) {}
    override suspend fun insertDogWalkEvents(events: List<DogWalkEventEntity>) {}
    override suspend fun getDogWalkEventsForWorkout(workoutId: String): List<DogWalkEventEntity> = emptyList()
    override suspend fun getTotalEventCountByType(eventType: String): Int = 0
    override suspend fun getTotalDogWalkEventCount(): Int = 0
    override suspend fun getDistinctEventTypeCountForWorkout(workoutId: String): Int = 0
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
