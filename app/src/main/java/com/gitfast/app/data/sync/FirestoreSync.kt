package com.gitfast.app.data.sync

import android.util.Log
import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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
    private val TAG = "FirestoreSync"

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

            val workoutMap = workout.toFirestoreMap().toMutableMap()
            workoutMap["phases"] = phases.map { it.toFirestoreMap() }
            workoutMap["laps"] = laps.map { it.toFirestoreMap() }
            workoutMap["gpsPointCount"] = gpsPoints.size
            workoutMap["syncedAt"] = System.currentTimeMillis()

            userDoc.collection("workouts").document(workoutId).set(workoutMap).await()

            // GPS points in separate doc to avoid loading on list
            if (gpsPoints.isNotEmpty()) {
                userDoc.collection("gpsPoints").document(workoutId).set(
                    mapOf("points" to gpsPoints.map { it.toFirestoreMap() })
                ).await()
            }

            Log.d(TAG, "Pushed workout $workoutId with ${gpsPoints.size} GPS points")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push workout $workoutId", e)
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

            Log.d(TAG, "Pushed character data")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push character data", e)
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
                autoLapAnchorRadiusMeters = settingsStore.autoLapAnchorRadiusMeters,
                homeArrivalEnabled = settingsStore.homeArrivalEnabled,
                homeLatitude = settingsStore.homeLatitude,
                homeLongitude = settingsStore.homeLongitude,
                homeArrivalRadiusMeters = settingsStore.homeArrivalRadiusMeters,
            )
            userDoc.set(mapOf("settings" to settingsMap)).await()
            Log.d(TAG, "Pushed settings")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push settings", e)
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
            Log.d(TAG, "Pushed ${tags.size} route tags")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push route tags", e)
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
                    Log.w(TAG, "No GPS points for workout $workoutId", e)
                }

                workoutDao.saveWorkoutTransaction(workout, phases, laps, gpsPoints)
                pulled++
            }

            Log.d(TAG, "Pulled $pulled workouts from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull workouts", e)
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
                        Log.w(TAG, "Skipped XP transaction ${tx.id}: ${e.message}")
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

            Log.d(TAG, "Pulled character data")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull character data", e)
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
            (settings["autoLapAnchorRadiusMeters"] as? Number)?.let { settingsStore.autoLapAnchorRadiusMeters = it.toInt() }
            (settings["homeArrivalEnabled"] as? Boolean)?.let { settingsStore.homeArrivalEnabled = it }
            (settings["homeLatitude"] as? Number)?.let { settingsStore.homeLatitude = it.toDouble() }
            (settings["homeLongitude"] as? Number)?.let { settingsStore.homeLongitude = it.toDouble() }
            (settings["homeArrivalRadiusMeters"] as? Number)?.let { settingsStore.homeArrivalRadiusMeters = it.toInt() }

            Log.d(TAG, "Pulled settings")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull settings", e)
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
            Log.d(TAG, "Full sync complete")
        } catch (e: Exception) {
            syncStatusStore.setError(e.message ?: "Sync failed")
            Log.e(TAG, "Full sync failed", e)
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
            Log.d(TAG, "Initial migration complete")
        } catch (e: Exception) {
            syncStatusStore.setError(e.message ?: "Initial migration failed")
            Log.e(TAG, "Initial migration failed", e)
        }
    }

    private suspend fun pushAllWorkouts() {
        val workouts = workoutDao.getAllCompletedWorkoutsOnce()
        for (workout in workouts) {
            pushWorkout(workout.id)
        }
    }
}
