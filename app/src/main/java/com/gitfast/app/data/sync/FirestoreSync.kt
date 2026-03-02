package com.gitfast.app.data.sync

import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSync @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val workoutDao: WorkoutDao,
    private val characterDao: CharacterDao,
    private val settingsStore: SettingsStore,
    private val syncStatusStore: SyncStatusStore,
) {
    private fun userDocRef() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it)
    }

    /** Push a single completed workout (with phases, laps, and GPS points) to Firestore. */
    suspend fun pushWorkout(workoutId: String) {
        val userDoc = userDocRef() ?: return
        try {
            val workout = workoutDao.getWorkoutById(workoutId) ?: return
            val phases = workoutDao.getPhasesForWorkout(workoutId)
            val laps = phases.flatMap { phase -> workoutDao.getLapsForPhase(phase.id) }
            val gpsPoints = workoutDao.getGpsPointsForWorkout(workoutId)

            val dogWalkEvents = workoutDao.getDogWalkEventsForWorkout(workoutId)

            val workoutMap = workout.toFirestoreMap().toMutableMap()
            workoutMap["phases"] = phases.map { it.toFirestoreMap() }
            workoutMap["laps"] = laps.map { it.toFirestoreMap() }
            workoutMap["dogWalkEvents"] = dogWalkEvents.map { it.toFirestoreMap() }
            workoutMap["gpsPointCount"] = gpsPoints.size
            workoutMap["syncedAt"] = System.currentTimeMillis()

            // Top-level aggregate fields for querying
            workoutMap["totalDistanceMeters"] = workout.distanceMeters
            val durationMs = if (workout.endTime != null) workout.endTime - workout.startTime else 0L
            workoutMap["activeDurationMs"] = durationMs
            val distanceMiles = workout.distanceMeters / 1609.344
            workoutMap["averagePaceMs"] = if (distanceMiles > 0) (durationMs / distanceMiles).toLong() else 0L

            userDoc.collection("workouts").document(workoutId).set(workoutMap).await()

            // GPS points in separate doc to avoid loading on list
            if (gpsPoints.isNotEmpty()) {
                userDoc.collection("gpsPoints").document(workoutId).set(
                    mapOf("points" to gpsPoints.map { it.toFirestoreMap() })
                ).await()
            }

            Timber.d("Pushed workout %s with %d GPS points", workoutId, gpsPoints.size)
        } catch (e: Exception) {
            Timber.e(e, "Failed to push workout %s", workoutId)
        }
    }

    /** Push both character profiles, all XP transactions, and all achievements. */
    suspend fun pushCharacterData() {
        val userDoc = userDocRef() ?: return
        try {
            for (profileId in listOf(1, 2)) {
                val profile = characterDao.getProfileOnce(profileId) ?: continue
                userDoc.collection("characterProfiles")
                    .document(profileId.toString())
                    .set(profile.toFirestoreMap())
                    .await()
            }

            // Push XP transactions for both profiles
            for (profileId in listOf(1, 2)) {
                val txs = characterDao.getXpTransactionsOnce(profileId)
                for (tx in txs) {
                    userDoc.collection("xpTransactions")
                        .document(tx.id)
                        .set(tx.toFirestoreMap())
                        .await()
                }
            }

            // Push achievements for both profiles
            for (profileId in listOf(1, 2)) {
                val achievements = characterDao.getUnlockedAchievementsOnce(profileId)
                for (achievement in achievements) {
                    userDoc.collection("achievements")
                        .document("${achievement.achievementId}_$profileId")
                        .set(achievement.toFirestoreMap())
                        .await()
                }
            }

            Timber.d("Pushed character data")
        } catch (e: Exception) {
            Timber.e(e, "Failed to push character data")
        }
    }

    /** Push settings to the user document. */
    suspend fun pushSettings() {
        val userDoc = userDocRef() ?: return
        try {
            val settingsMap = settingsToFirestoreMap(
                autoPauseEnabled = settingsStore.autoPauseEnabled,
                distanceUnit = settingsStore.distanceUnit.name,
                keepScreenOn = settingsStore.keepScreenOn,
                autoLapEnabled = settingsStore.autoLapEnabled,
                autoLapAnchorRadiusMeters = SettingsStore.AUTO_LAP_ANCHOR_RADIUS_METERS,
                homeArrivalEnabled = settingsStore.homeArrivalEnabled,
                homeLatitude = settingsStore.homeLatitude,
                homeLongitude = settingsStore.homeLongitude,
                homeArrivalRadiusMeters = settingsStore.homeArrivalRadiusMeters,
                lapStartLatitude = settingsStore.lapStartLatitude,
                lapStartLongitude = settingsStore.lapStartLongitude,
            )
            userDoc.set(mapOf("settings" to settingsMap)).await()
            Timber.d("Pushed settings")
        } catch (e: Exception) {
            Timber.e(e, "Failed to push settings")
        }
    }

    /** Push all route tags. */
    suspend fun pushRouteTags() {
        val userDoc = userDocRef() ?: return
        try {
            val tags = workoutDao.getAllRouteTags()
            for (tag in tags) {
                userDoc.collection("routeTags")
                    .document(tag.name)
                    .set(tag.toFirestoreMap())
                    .await()
            }
            Timber.d("Pushed %d route tags", tags.size)
        } catch (e: Exception) {
            Timber.e(e, "Failed to push route tags")
        }
    }

    /** Pull workouts from Firestore that don't exist in Room (new device restore). */
    suspend fun pullWorkouts() {
        val userDoc = userDocRef() ?: return
        try {
            val remoteWorkouts = userDoc.collection("workouts").get().await()
            var pulled = 0

            for (doc in remoteWorkouts.documents) {
                val workoutId = doc.id
                val existing = workoutDao.getWorkoutById(workoutId)
                if (existing != null) continue

                @Suppress("UNCHECKED_CAST")
                val data = doc.data ?: continue
                val workout = data.toWorkoutEntity()
                val phases = (data["phases"] as? List<Map<String, Any?>>)
                    ?.map { it.toWorkoutPhaseEntity() } ?: emptyList()
                val laps = (data["laps"] as? List<Map<String, Any?>>)
                    ?.map { it.toLapEntity() } ?: emptyList()

                // Pull GPS points from separate doc
                var gpsPoints = emptyList<GpsPointEntity>()
                try {
                    val gpsDoc = userDoc.collection("gpsPoints").document(workoutId).get().await()
                    @Suppress("UNCHECKED_CAST")
                    val pointsList = gpsDoc.data?.get("points") as? List<Map<String, Any?>>
                    gpsPoints = pointsList?.map { it.toGpsPointEntity() } ?: emptyList()
                } catch (e: Exception) {
                    Timber.w(e, "No GPS points for workout %s", workoutId)
                }

                workoutDao.saveWorkoutTransaction(workout, phases, laps, gpsPoints)

                // Pull dog walk events from the workout doc
                @Suppress("UNCHECKED_CAST")
                val dogWalkEvents = (data["dogWalkEvents"] as? List<Map<String, Any?>>)
                    ?.map { it.toDogWalkEventEntity() } ?: emptyList()
                if (dogWalkEvents.isNotEmpty()) {
                    workoutDao.insertDogWalkEvents(dogWalkEvents)
                }

                pulled++
            }

            Timber.d("Pulled %d workouts from Firestore", pulled)
        } catch (e: Exception) {
            Timber.e(e, "Failed to pull workouts")
        }
    }

    /** Pull character data, merging (higher XP wins, union achievements). */
    suspend fun pullCharacterData() {
        val userDoc = userDocRef() ?: return
        try {
            for (profileId in listOf(1, 2)) {
                val remoteDoc = userDoc.collection("characterProfiles")
                    .document(profileId.toString()).get().await()
                val remoteData = remoteDoc.data ?: continue
                val remoteProfile = remoteData.toCharacterProfileEntity()
                val localProfile = characterDao.getProfileOnce(profileId)

                if (localProfile == null) {
                    characterDao.insertProfile(remoteProfile)
                } else if (remoteProfile.totalXp > localProfile.totalXp) {
                    characterDao.updateProfile(remoteProfile)
                }
            }

            // Pull XP transactions (insert missing ones)
            val remoteTxs = userDoc.collection("xpTransactions").get().await()
            for (doc in remoteTxs.documents) {
                val data = doc.data ?: continue
                val tx = data.toXpTransactionEntity()
                val existing = characterDao.getXpTransactionForWorkout(tx.workoutId, tx.profileId)
                if (existing == null) {
                    try {
                        characterDao.insertXpTransaction(tx)
                    } catch (e: Exception) {
                        // Foreign key constraint may fail if workout not yet pulled
                        Timber.w("Skipped XP transaction %s: %s", tx.id, e.message)
                    }
                }
            }

            // Pull achievements (union)
            val remoteAchievements = userDoc.collection("achievements").get().await()
            for (doc in remoteAchievements.documents) {
                val data = doc.data ?: continue
                val achievement = data.toUnlockedAchievementEntity()
                characterDao.insertUnlockedAchievement(achievement) // IGNORE conflict
            }

            Timber.d("Pulled character data")
        } catch (e: Exception) {
            Timber.e(e, "Failed to pull character data")
        }
    }

    /** Pull settings from Firestore and apply to SettingsStore. */
    suspend fun pullSettings() {
        val userDoc = userDocRef() ?: return
        try {
            val doc = userDoc.get().await()
            @Suppress("UNCHECKED_CAST")
            val settings = doc.data?.get("settings") as? Map<String, Any?> ?: return

            (settings["autoPauseEnabled"] as? Boolean)?.let { settingsStore.autoPauseEnabled = it }
            (settings["distanceUnit"] as? String)?.let {
                try {
                    settingsStore.distanceUnit = com.gitfast.app.data.model.DistanceUnit.valueOf(it)
                } catch (_: Exception) {}
            }
            (settings["keepScreenOn"] as? Boolean)?.let { settingsStore.keepScreenOn = it }
            (settings["autoLapEnabled"] as? Boolean)?.let { settingsStore.autoLapEnabled = it }
            // autoLapAnchorRadiusMeters is now hardcoded — ignore synced value
            (settings["homeArrivalEnabled"] as? Boolean)?.let { settingsStore.homeArrivalEnabled = it }
            (settings["homeLatitude"] as? Number)?.let { settingsStore.homeLatitude = it.toDouble() }
            (settings["homeLongitude"] as? Number)?.let { settingsStore.homeLongitude = it.toDouble() }
            (settings["homeArrivalRadiusMeters"] as? Number)?.let { settingsStore.homeArrivalRadiusMeters = it.toInt() }
            (settings["lapStartLatitude"] as? Number)?.let { settingsStore.lapStartLatitude = it.toDouble() }
            (settings["lapStartLongitude"] as? Number)?.let { settingsStore.lapStartLongitude = it.toDouble() }

            Timber.d("Pulled settings")
        } catch (e: Exception) {
            Timber.e(e, "Failed to pull settings")
        }
    }

    /** Full bidirectional sync: push all local data, then pull remote data. */
    suspend fun fullSync() {
        syncStatusStore.setSyncing()
        try {
            // Push all local data
            pushAllWorkouts()
            pushCharacterData()
            pushSettings()
            pushRouteTags()

            // Pull remote data
            pullWorkouts()
            pullCharacterData()
            pullSettings()

            syncStatusStore.setSuccess()
            Timber.d("Full sync complete")
        } catch (e: Exception) {
            syncStatusStore.setError(e.message ?: "Sync failed")
            Timber.e(e, "Full sync failed")
        }
    }

    /** Push all existing local workouts on first sign-in. */
    suspend fun initialMigration() {
        if (syncStatusStore.hasCompletedInitialSync) return
        syncStatusStore.setSyncing()
        try {
            pushAllWorkouts()
            pushCharacterData()
            pushSettings()
            pushRouteTags()

            syncStatusStore.hasCompletedInitialSync = true
            syncStatusStore.setSuccess()
            Timber.d("Initial migration complete")
        } catch (e: Exception) {
            syncStatusStore.setError(e.message ?: "Initial migration failed")
            Timber.e(e, "Initial migration failed")
        }
    }

    private suspend fun pushAllWorkouts() {
        val workouts = workoutDao.getAllCompletedWorkoutsOnce()
        for (workout in workouts) {
            pushWorkout(workout.id)
        }
    }
}
