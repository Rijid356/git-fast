package com.gitfast.app.data.sync

import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.UnlockedAchievementEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.local.entity.XpTransactionEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.DistanceUnit
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WorkoutStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirestoreSyncTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var workoutDao: WorkoutDao
    private lateinit var characterDao: CharacterDao
    private lateinit var settingsStore: SettingsStore
    private lateinit var syncStatusStore: SyncStatusStore
    private lateinit var sync: FirestoreSync

    private lateinit var user: FirebaseUser
    private lateinit var usersCollection: CollectionReference
    private lateinit var userDoc: DocumentReference

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        workoutDao = mockk(relaxed = true)
        characterDao = mockk(relaxed = true)
        settingsStore = mockk(relaxed = true)
        syncStatusStore = mockk(relaxed = true)

        user = mockk(relaxed = true)
        usersCollection = mockk(relaxed = true)
        userDoc = mockk(relaxed = true)

        every { auth.currentUser } returns user
        every { user.uid } returns "test-uid"
        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("test-uid") } returns userDoc

        sync = FirestoreSync(firestore, auth, workoutDao, characterDao, settingsStore, syncStatusStore)
    }

    // --- Helpers to build completed Firebase Tasks ---

    private fun <T> completedTask(result: T): Task<T> {
        val task = mockk<Task<T>>()
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        @Suppress("UNCHECKED_CAST")
        every { task.result } returns result
        return task
    }

    private fun completedVoidTask(): Task<Void> {
        val task = mockk<Task<Void>>()
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns null
        return task
    }

    private fun <T> failedTask(exception: Exception): Task<T> {
        val task = mockk<Task<T>>()
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
        return task
    }

    private fun mockSubCollection(name: String): CollectionReference {
        val collection = mockk<CollectionReference>(relaxed = true)
        every { userDoc.collection(name) } returns collection
        return collection
    }

    private fun mockDocRef(collection: CollectionReference, id: String): DocumentReference {
        val ref = mockk<DocumentReference>(relaxed = true)
        every { collection.document(id) } returns ref
        return ref
    }

    private fun stubSetAwait(docRef: DocumentReference) {
        every { docRef.set(any()) } returns completedVoidTask()
    }

    private fun stubGetAwait(docRef: DocumentReference, data: Map<String, Any?>?, id: String = "doc") {
        val snapshot = mockk<DocumentSnapshot>(relaxed = true)
        every { snapshot.id } returns id
        every { snapshot.data } returns data
        every { docRef.get() } returns completedTask(snapshot)
    }

    private fun stubCollectionGet(collection: CollectionReference, docs: List<DocumentSnapshot>) {
        val qs = mockk<QuerySnapshot>(relaxed = true)
        every { qs.documents } returns docs
        every { collection.get() } returns completedTask(qs)
    }

    private fun mockDocSnapshot(id: String, data: Map<String, Any?>?): DocumentSnapshot {
        val doc = mockk<DocumentSnapshot>(relaxed = true)
        every { doc.id } returns id
        every { doc.data } returns data
        return doc
    }

    private fun sampleWorkout(id: String = "w-1") = WorkoutEntity(
        id = id, startTime = 1000L, endTime = 5000L, totalSteps = 200,
        distanceMeters = 1609.34, status = WorkoutStatus.COMPLETED,
        activityType = ActivityType.RUN, dogName = null, notes = null,
        weatherCondition = null, weatherTemp = null, energyLevel = null, routeTag = null
    )

    private fun samplePhase(workoutId: String = "w-1") = WorkoutPhaseEntity(
        id = "p-1", workoutId = workoutId, type = PhaseType.LAPS,
        startTime = 1000L, endTime = 5000L, distanceMeters = 800.0, steps = 100
    )

    private fun sampleLap() = LapEntity(
        id = "l-1", phaseId = "p-1", lapNumber = 1,
        startTime = 1000L, endTime = 3000L, distanceMeters = 400.0, steps = 50
    )

    private fun sampleGpsPoint(workoutId: String = "w-1") = GpsPointEntity(
        id = 1L, workoutId = workoutId, latitude = 40.7, longitude = -74.0,
        timestamp = 1000L, accuracy = 5.0f, sortIndex = 0, speed = 3.5f
    )

    private fun sampleProfile(id: Int = 1) = CharacterProfileEntity(
        id = id, totalXp = 500, level = 5, createdAt = 1000L,
        speedStat = 10, enduranceStat = 15, consistencyStat = 12
    )

    private fun sampleXpTransaction(profileId: Int = 1) = XpTransactionEntity(
        id = "tx-1", workoutId = "w-1", xpAmount = 100, reason = "Run",
        timestamp = 1000L, profileId = profileId
    )

    private fun stubSettingsStore() {
        every { settingsStore.autoPauseEnabled } returns true
        every { settingsStore.distanceUnit } returns DistanceUnit.MILES
        every { settingsStore.keepScreenOn } returns true
        every { settingsStore.autoLapEnabled } returns false
        every { settingsStore.homeArrivalEnabled } returns false
        every { settingsStore.homeLatitude } returns null
        every { settingsStore.homeLongitude } returns null
        every { settingsStore.homeArrivalRadiusMeters } returns 15
    }

    // ===== Auth guard =====

    @Test
    fun `pushWorkout returns early when not authenticated`() = runTest {
        every { auth.currentUser } returns null

        sync.pushWorkout("w-1")

        coVerify(exactly = 0) { workoutDao.getWorkoutById(any()) }
    }

    @Test
    fun `fullSync completes without error when not authenticated`() = runTest {
        every { auth.currentUser } returns null

        // fullSync doesn't short-circuit on auth — each sub-method returns early individually
        // pushAllWorkouts still calls getAllCompletedWorkoutsOnce, but pushWorkout returns early per-workout
        coEvery { workoutDao.getAllCompletedWorkoutsOnce() } returns listOf(sampleWorkout())
        stubSettingsStore()
        coEvery { workoutDao.getAllRouteTags() } returns emptyList()

        sync.fullSync()

        // Sub-methods return early: no Firestore writes happen
        verify(exactly = 0) { firestore.collection(any()) }
        verify { syncStatusStore.setSuccess() }
    }

    // ===== pushWorkout =====

    @Test
    fun `pushWorkout pushes workout with GPS points`() = runTest {
        coEvery { workoutDao.getWorkoutById("w-1") } returns sampleWorkout()
        coEvery { workoutDao.getPhasesForWorkout("w-1") } returns listOf(samplePhase())
        coEvery { workoutDao.getLapsForPhase("p-1") } returns listOf(sampleLap())
        coEvery { workoutDao.getGpsPointsForWorkout("w-1") } returns listOf(sampleGpsPoint())

        val workoutsCol = mockSubCollection("workouts")
        val workoutDocRef = mockDocRef(workoutsCol, "w-1")
        stubSetAwait(workoutDocRef)

        val gpsCol = mockSubCollection("gpsPoints")
        val gpsDocRef = mockDocRef(gpsCol, "w-1")
        stubSetAwait(gpsDocRef)

        sync.pushWorkout("w-1")

        verify { workoutDocRef.set(match<Map<String, Any?>> { it["id"] == "w-1" && it["gpsPointCount"] == 1 }) }
        verify { gpsDocRef.set(any()) }
    }

    @Test
    fun `pushWorkout skips GPS doc when no GPS points`() = runTest {
        coEvery { workoutDao.getWorkoutById("w-1") } returns sampleWorkout()
        coEvery { workoutDao.getPhasesForWorkout("w-1") } returns listOf(samplePhase())
        coEvery { workoutDao.getLapsForPhase("p-1") } returns emptyList()
        coEvery { workoutDao.getGpsPointsForWorkout("w-1") } returns emptyList()

        val workoutsCol = mockSubCollection("workouts")
        val workoutDocRef = mockDocRef(workoutsCol, "w-1")
        stubSetAwait(workoutDocRef)

        sync.pushWorkout("w-1")

        verify { workoutDocRef.set(match<Map<String, Any?>> { it["gpsPointCount"] == 0 }) }
        verify(exactly = 0) { userDoc.collection("gpsPoints") }
    }

    @Test
    fun `pushWorkout returns early when workout not found`() = runTest {
        coEvery { workoutDao.getWorkoutById("w-missing") } returns null

        sync.pushWorkout("w-missing")

        verify(exactly = 0) { userDoc.collection("workouts") }
    }

    @Test
    fun `pushWorkout catches exception without crashing`() = runTest {
        coEvery { workoutDao.getWorkoutById("w-1") } throws RuntimeException("DB error")

        sync.pushWorkout("w-1")
    }

    // ===== pushCharacterData =====

    @Test
    fun `pushCharacterData pushes both profiles`() = runTest {
        coEvery { characterDao.getProfileOnce(1) } returns sampleProfile(1)
        coEvery { characterDao.getProfileOnce(2) } returns sampleProfile(2)
        coEvery { characterDao.getXpTransactionsOnce(any()) } returns emptyList()
        coEvery { characterDao.getUnlockedAchievementsOnce(any()) } returns emptyList()

        val profilesCol = mockSubCollection("characterProfiles")
        val doc1 = mockDocRef(profilesCol, "1")
        val doc2 = mockDocRef(profilesCol, "2")
        stubSetAwait(doc1)
        stubSetAwait(doc2)

        mockSubCollection("xpTransactions")
        mockSubCollection("achievements")

        sync.pushCharacterData()

        verify { doc1.set(match<Map<String, Any?>> { it["id"] == 1 }) }
        verify { doc2.set(match<Map<String, Any?>> { it["id"] == 2 }) }
    }

    @Test
    fun `pushCharacterData skips missing profile`() = runTest {
        coEvery { characterDao.getProfileOnce(1) } returns sampleProfile(1)
        coEvery { characterDao.getProfileOnce(2) } returns null
        coEvery { characterDao.getXpTransactionsOnce(any()) } returns emptyList()
        coEvery { characterDao.getUnlockedAchievementsOnce(any()) } returns emptyList()

        val profilesCol = mockSubCollection("characterProfiles")
        val doc1 = mockDocRef(profilesCol, "1")
        stubSetAwait(doc1)

        mockSubCollection("xpTransactions")
        mockSubCollection("achievements")

        sync.pushCharacterData()

        verify { doc1.set(any()) }
        verify(exactly = 0) { profilesCol.document("2") }
    }

    @Test
    fun `pushCharacterData catches exception without crashing`() = runTest {
        coEvery { characterDao.getProfileOnce(1) } throws RuntimeException("DB error")

        sync.pushCharacterData()
    }

    // ===== pushSettings =====

    @Test
    fun `pushSettings serializes all fields`() = runTest {
        every { settingsStore.autoPauseEnabled } returns true
        every { settingsStore.distanceUnit } returns DistanceUnit.MILES
        every { settingsStore.keepScreenOn } returns false
        every { settingsStore.autoLapEnabled } returns true
        every { settingsStore.homeArrivalEnabled } returns true
        every { settingsStore.homeLatitude } returns 40.7128
        every { settingsStore.homeLongitude } returns -74.006
        every { settingsStore.homeArrivalRadiusMeters } returns 50

        stubSetAwait(userDoc)

        sync.pushSettings()

        verify {
            userDoc.set(match<Map<String, Any?>> { outer ->
                @Suppress("UNCHECKED_CAST")
                val s = outer["settings"] as Map<String, Any?>
                s["autoPauseEnabled"] == true &&
                    s["distanceUnit"] == "MILES" &&
                    s["keepScreenOn"] == false &&
                    s["autoLapEnabled"] == true &&
                    s["autoLapAnchorRadiusMeters"] == 5 &&
                    s["homeArrivalEnabled"] == true &&
                    s["homeLatitude"] == 40.7128 &&
                    s["homeLongitude"] == -74.006 &&
                    s["homeArrivalRadiusMeters"] == 50
            })
        }
    }

    @Test
    fun `pushSettings catches exception without crashing`() = runTest {
        stubSettingsStore()
        every { userDoc.set(any()) } returns failedTask<Void>(RuntimeException("Network error"))

        sync.pushSettings()
    }

    // ===== pushRouteTags =====

    @Test
    fun `pushRouteTags pushes all tags`() = runTest {
        val tags = listOf(
            RouteTagEntity("Park Loop", 1000L, 5000L),
            RouteTagEntity("River Trail", 2000L, 6000L)
        )
        coEvery { workoutDao.getAllRouteTags() } returns tags

        val tagsCol = mockSubCollection("routeTags")
        val doc1 = mockDocRef(tagsCol, "Park Loop")
        val doc2 = mockDocRef(tagsCol, "River Trail")
        stubSetAwait(doc1)
        stubSetAwait(doc2)

        sync.pushRouteTags()

        verify { doc1.set(match<Map<String, Any?>> { it["name"] == "Park Loop" }) }
        verify { doc2.set(match<Map<String, Any?>> { it["name"] == "River Trail" }) }
    }

    @Test
    fun `pushRouteTags catches exception without crashing`() = runTest {
        coEvery { workoutDao.getAllRouteTags() } throws RuntimeException("DB error")

        sync.pushRouteTags()
    }

    // ===== pullWorkouts =====

    @Test
    fun `pullWorkouts pulls new workouts and skips existing`() = runTest {
        val workoutData = sampleWorkout().toFirestoreMap().toMutableMap()
        workoutData["phases"] = listOf(samplePhase().toFirestoreMap())
        workoutData["laps"] = listOf(sampleLap().toFirestoreMap())

        val doc = mockDocSnapshot("w-1", workoutData)

        val workoutsCol = mockSubCollection("workouts")
        stubCollectionGet(workoutsCol, listOf(doc))

        coEvery { workoutDao.getWorkoutById("w-1") } returns null

        val gpsCol = mockSubCollection("gpsPoints")
        val gpsDocRef = mockDocRef(gpsCol, "w-1")
        stubGetAwait(gpsDocRef, mapOf(
            "points" to listOf(sampleGpsPoint().toFirestoreMap())
        ), "w-1")

        sync.pullWorkouts()

        coVerify { workoutDao.saveWorkoutTransaction(any(), any(), any(), any()) }
    }

    @Test
    fun `pullWorkouts skips existing workout`() = runTest {
        val workoutData = sampleWorkout().toFirestoreMap()
        val doc = mockDocSnapshot("w-1", workoutData)

        val workoutsCol = mockSubCollection("workouts")
        stubCollectionGet(workoutsCol, listOf(doc))

        coEvery { workoutDao.getWorkoutById("w-1") } returns sampleWorkout()

        sync.pullWorkouts()

        coVerify(exactly = 0) { workoutDao.saveWorkoutTransaction(any(), any(), any(), any()) }
    }

    @Test
    fun `pullWorkouts handles missing GPS doc gracefully`() = runTest {
        val workoutData = sampleWorkout().toFirestoreMap().toMutableMap()
        workoutData["phases"] = emptyList<Map<String, Any?>>()
        workoutData["laps"] = emptyList<Map<String, Any?>>()

        val doc = mockDocSnapshot("w-1", workoutData)

        val workoutsCol = mockSubCollection("workouts")
        stubCollectionGet(workoutsCol, listOf(doc))
        coEvery { workoutDao.getWorkoutById("w-1") } returns null

        // GPS doc throws
        val gpsCol = mockSubCollection("gpsPoints")
        val gpsDocRef = mockDocRef(gpsCol, "w-1")
        every { gpsDocRef.get() } returns failedTask(RuntimeException("Not found"))

        sync.pullWorkouts()

        coVerify {
            workoutDao.saveWorkoutTransaction(any(), any(), any(), match { it.isEmpty() })
        }
    }

    @Test
    fun `pullWorkouts catches top-level exception without crashing`() = runTest {
        val workoutsCol = mockSubCollection("workouts")
        every { workoutsCol.get() } returns failedTask(RuntimeException("Network error"))

        sync.pullWorkouts()
    }

    // ===== pullCharacterData =====

    @Test
    fun `pullCharacterData inserts new profile`() = runTest {
        val remoteProfile = sampleProfile(1)

        val profilesCol = mockSubCollection("characterProfiles")
        val docRef1 = mockDocRef(profilesCol, "1")
        val docRef2 = mockDocRef(profilesCol, "2")
        stubGetAwait(docRef1, remoteProfile.toFirestoreMap(), "1")
        stubGetAwait(docRef2, null, "2")

        coEvery { characterDao.getProfileOnce(1) } returns null

        val xpCol = mockSubCollection("xpTransactions")
        val achievementsCol = mockSubCollection("achievements")
        stubCollectionGet(xpCol, emptyList())
        stubCollectionGet(achievementsCol, emptyList())

        sync.pullCharacterData()

        coVerify { characterDao.insertProfile(match { it.id == 1 && it.totalXp == 500 }) }
    }

    @Test
    fun `pullCharacterData updates only if remote XP higher`() = runTest {
        val remoteProfile = sampleProfile(1).copy(totalXp = 1000)
        val localProfile = sampleProfile(1).copy(totalXp = 500)

        val profilesCol = mockSubCollection("characterProfiles")
        val docRef1 = mockDocRef(profilesCol, "1")
        val docRef2 = mockDocRef(profilesCol, "2")
        stubGetAwait(docRef1, remoteProfile.toFirestoreMap(), "1")
        stubGetAwait(docRef2, null, "2")

        coEvery { characterDao.getProfileOnce(1) } returns localProfile

        val xpCol = mockSubCollection("xpTransactions")
        val achievementsCol = mockSubCollection("achievements")
        stubCollectionGet(xpCol, emptyList())
        stubCollectionGet(achievementsCol, emptyList())

        sync.pullCharacterData()

        coVerify { characterDao.updateProfile(match { it.totalXp == 1000 }) }
    }

    @Test
    fun `pullCharacterData does not update if local XP higher`() = runTest {
        val remoteProfile = sampleProfile(1).copy(totalXp = 200)
        val localProfile = sampleProfile(1).copy(totalXp = 500)

        val profilesCol = mockSubCollection("characterProfiles")
        val docRef1 = mockDocRef(profilesCol, "1")
        val docRef2 = mockDocRef(profilesCol, "2")
        stubGetAwait(docRef1, remoteProfile.toFirestoreMap(), "1")
        stubGetAwait(docRef2, null, "2")

        coEvery { characterDao.getProfileOnce(1) } returns localProfile

        val xpCol = mockSubCollection("xpTransactions")
        val achievementsCol = mockSubCollection("achievements")
        stubCollectionGet(xpCol, emptyList())
        stubCollectionGet(achievementsCol, emptyList())

        sync.pullCharacterData()

        coVerify(exactly = 0) { characterDao.updateProfile(any()) }
        coVerify(exactly = 0) { characterDao.insertProfile(any()) }
    }

    @Test
    fun `pullCharacterData skips existing XP transaction`() = runTest {
        val profilesCol = mockSubCollection("characterProfiles")
        val docRef1 = mockDocRef(profilesCol, "1")
        val docRef2 = mockDocRef(profilesCol, "2")
        stubGetAwait(docRef1, null, "1")
        stubGetAwait(docRef2, null, "2")

        val tx = sampleXpTransaction()
        val txDoc = mockDocSnapshot("tx-1", tx.toFirestoreMap())
        val xpCol = mockSubCollection("xpTransactions")
        stubCollectionGet(xpCol, listOf(txDoc))

        coEvery { characterDao.getXpTransactionForWorkout("w-1", 1) } returns tx

        val achievementsCol = mockSubCollection("achievements")
        stubCollectionGet(achievementsCol, emptyList())

        sync.pullCharacterData()

        coVerify(exactly = 0) { characterDao.insertXpTransaction(any()) }
    }

    @Test
    fun `pullCharacterData catches exception without crashing`() = runTest {
        val profilesCol = mockSubCollection("characterProfiles")
        val docRef = mockDocRef(profilesCol, "1")
        every { docRef.get() } returns failedTask(RuntimeException("Network error"))

        sync.pullCharacterData()
    }

    // ===== pullSettings =====

    @Test
    fun `pullSettings applies all settings`() = runTest {
        val settingsMap = mapOf<String, Any?>(
            "autoPauseEnabled" to false,
            "distanceUnit" to "KILOMETERS",
            "keepScreenOn" to true,
            "autoLapEnabled" to true,
            "autoLapAnchorRadiusMeters" to 25,
            "homeArrivalEnabled" to true,
            "homeLatitude" to 40.7128,
            "homeLongitude" to -74.006,
            "homeArrivalRadiusMeters" to 30
        )
        stubGetAwait(userDoc, mapOf("settings" to settingsMap), "test-uid")

        sync.pullSettings()

        verify { settingsStore.autoPauseEnabled = false }
        verify { settingsStore.distanceUnit = DistanceUnit.KILOMETERS }
        verify { settingsStore.keepScreenOn = true }
        verify { settingsStore.autoLapEnabled = true }
        verify { settingsStore.homeArrivalEnabled = true }
        verify { settingsStore.homeLatitude = 40.7128 }
        verify { settingsStore.homeLongitude = -74.006 }
        verify { settingsStore.homeArrivalRadiusMeters = 30 }
    }

    @Test
    fun `pullSettings handles missing settings doc`() = runTest {
        stubGetAwait(userDoc, null, "test-uid")

        sync.pullSettings()

        verify(exactly = 0) { settingsStore.autoPauseEnabled = any() }
    }

    // ===== fullSync =====

    @Test
    fun `fullSync sets syncing then success status`() = runTest {
        // Push stubs
        coEvery { workoutDao.getAllCompletedWorkoutsOnce() } returns emptyList()
        coEvery { characterDao.getProfileOnce(any()) } returns null
        coEvery { characterDao.getXpTransactionsOnce(any()) } returns emptyList()
        coEvery { characterDao.getUnlockedAchievementsOnce(any()) } returns emptyList()
        stubSettingsStore()
        stubSetAwait(userDoc)
        coEvery { workoutDao.getAllRouteTags() } returns emptyList()

        mockSubCollection("characterProfiles")
        mockSubCollection("routeTags")

        // Pull stubs
        val workoutsCol = mockSubCollection("workouts")
        stubCollectionGet(workoutsCol, emptyList())

        val profilesCol = mockSubCollection("characterProfiles")
        val docRef1 = mockDocRef(profilesCol, "1")
        val docRef2 = mockDocRef(profilesCol, "2")
        stubGetAwait(docRef1, null, "1")
        stubGetAwait(docRef2, null, "2")

        val xpCol = mockSubCollection("xpTransactions")
        stubCollectionGet(xpCol, emptyList())
        val achievementsCol = mockSubCollection("achievements")
        stubCollectionGet(achievementsCol, emptyList())

        stubGetAwait(userDoc, null, "test-uid")

        sync.fullSync()

        verify { syncStatusStore.setSyncing() }
        verify { syncStatusStore.setSuccess() }
    }

    @Test
    fun `fullSync sets error status on exception`() = runTest {
        coEvery { workoutDao.getAllCompletedWorkoutsOnce() } throws RuntimeException("Sync failed")

        sync.fullSync()

        verify { syncStatusStore.setSyncing() }
        verify { syncStatusStore.setError("Sync failed") }
    }

    // ===== initialMigration =====

    @Test
    fun `initialMigration skips if already synced`() = runTest {
        every { syncStatusStore.hasCompletedInitialSync } returns true

        sync.initialMigration()

        verify(exactly = 0) { syncStatusStore.setSyncing() }
        coVerify(exactly = 0) { workoutDao.getAllCompletedWorkoutsOnce() }
    }

    @Test
    fun `initialMigration sets hasCompletedInitialSync on success`() = runTest {
        every { syncStatusStore.hasCompletedInitialSync } returns false

        coEvery { workoutDao.getAllCompletedWorkoutsOnce() } returns emptyList()
        coEvery { characterDao.getProfileOnce(any()) } returns null
        coEvery { characterDao.getXpTransactionsOnce(any()) } returns emptyList()
        coEvery { characterDao.getUnlockedAchievementsOnce(any()) } returns emptyList()
        stubSettingsStore()
        stubSetAwait(userDoc)
        coEvery { workoutDao.getAllRouteTags() } returns emptyList()

        mockSubCollection("characterProfiles")
        mockSubCollection("routeTags")

        sync.initialMigration()

        verify { syncStatusStore.setSyncing() }
        verify { syncStatusStore.hasCompletedInitialSync = true }
        verify { syncStatusStore.setSuccess() }
    }

    @Test
    fun `initialMigration sets error on exception`() = runTest {
        every { syncStatusStore.hasCompletedInitialSync } returns false
        coEvery { workoutDao.getAllCompletedWorkoutsOnce() } throws RuntimeException("DB error")

        sync.initialMigration()

        verify { syncStatusStore.setSyncing() }
        verify { syncStatusStore.setError("DB error") }
    }
}
