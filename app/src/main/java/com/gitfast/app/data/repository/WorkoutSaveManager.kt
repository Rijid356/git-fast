package com.gitfast.app.data.repository

import android.util.Log
import com.gitfast.app.data.local.WorkoutDao
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.service.WorkoutSnapshot
import com.gitfast.app.util.AchievementChecker
import com.gitfast.app.util.AchievementDef
import com.gitfast.app.util.AchievementSnapshot
import com.gitfast.app.util.DistanceCalculator
import com.gitfast.app.util.StatsCalculator
import com.gitfast.app.util.StreakCalculator
import com.gitfast.app.util.XpCalculator
import java.util.UUID
import javax.inject.Inject

data class SaveResult(
    val workoutId: String,
    val xpEarned: Int,
    val achievementsUnlocked: List<AchievementDef> = emptyList(),
    val streakDays: Int = 0,
    val streakMultiplier: Double = 1.0,
)

class WorkoutSaveManager @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val characterRepository: CharacterRepository,
    private val workoutRepository: WorkoutRepository,
) {

    suspend fun saveCompletedWorkout(snapshot: WorkoutSnapshot): SaveResult? {
        return try {
            val phases = snapshot.phases.map { phaseData ->
                WorkoutPhaseEntity(
                    id = UUID.randomUUID().toString(),
                    workoutId = snapshot.workoutId,
                    type = phaseData.type,
                    startTime = phaseData.startTime.toEpochMilli(),
                    endTime = phaseData.endTime.toEpochMilli(),
                    distanceMeters = phaseData.distanceMeters,
                    steps = phaseData.steps
                )
            }

            // Build lap entities, linking each to its parent phase
            val lapsPhaseEntity = phases.find { it.type == PhaseType.LAPS }
            val lapEntities = if (lapsPhaseEntity != null) {
                val lapsPhaseData = snapshot.phases.find { it.type == PhaseType.LAPS }
                lapsPhaseData?.laps?.map { lapData ->
                    LapEntity(
                        id = UUID.randomUUID().toString(),
                        phaseId = lapsPhaseEntity.id,
                        lapNumber = lapData.lapNumber,
                        startTime = lapData.startTime.toEpochMilli(),
                        endTime = lapData.endTime.toEpochMilli(),
                        distanceMeters = lapData.distanceMeters,
                        steps = lapData.steps,
                        splitLatitude = lapData.splitLatitude,
                        splitLongitude = lapData.splitLongitude
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }

            workoutDao.saveWorkoutTransaction(
                workout = buildWorkoutEntity(snapshot),
                phases = phases,
                laps = lapEntities,
                gpsPoints = buildGpsPointEntities(snapshot)
            )

            // Calculate current streak (includes the just-saved workout)
            val allWorkouts = workoutRepository.getAllCompletedWorkoutsOnce()
            val streakDays = StreakCalculator.getCurrentStreak(allWorkouts)

            // Calculate and award XP to user (profileId=1) with streak multiplier
            val xpResult = XpCalculator.calculateXp(snapshot, streakDays = streakDays)
            val xpAwarded = characterRepository.awardXp(
                workoutId = snapshot.workoutId,
                xpAmount = xpResult.totalXp,
                reason = xpResult.breakdown.joinToString("; "),
            )

            recalculateStats()

            // Check for newly unlocked user achievements
            val newAchievements = checkAchievements()

            // If dog walk, also award XP to Juniper (profileId=2)
            if (snapshot.activityType == ActivityType.DOG_WALK) {
                characterRepository.awardXp(
                    profileId = 2,
                    workoutId = snapshot.workoutId,
                    xpAmount = xpResult.totalXp,
                    reason = xpResult.breakdown.joinToString("; "),
                )
                recalculateJuniperStats()
                checkJuniperAchievements()
            }

            Log.d("WorkoutSaveManager", "Saved workout ${snapshot.workoutId}, awarded $xpAwarded XP, streak=$streakDays, ${newAchievements.size} achievements unlocked")
            SaveResult(
                workoutId = snapshot.workoutId,
                xpEarned = xpAwarded,
                achievementsUnlocked = newAchievements,
                streakDays = streakDays,
                streakMultiplier = xpResult.streakMultiplier,
            )
        } catch (e: Exception) {
            Log.e("WorkoutSaveManager", "Failed to save workout", e)
            null
        }
    }

    private fun buildWorkoutEntity(snapshot: WorkoutSnapshot): WorkoutEntity {
        return WorkoutEntity(
            id = snapshot.workoutId,
            startTime = snapshot.startTime.toEpochMilli(),
            endTime = snapshot.endTime.toEpochMilli(),
            totalSteps = snapshot.totalSteps,
            distanceMeters = snapshot.totalDistanceMeters,
            status = WorkoutStatus.COMPLETED,
            activityType = snapshot.activityType,
            dogName = null,
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = null
        )
    }

    private fun buildGpsPointEntities(snapshot: WorkoutSnapshot): List<GpsPointEntity> {
        return snapshot.gpsPoints.mapIndexed { index, point ->
            GpsPointEntity(
                workoutId = snapshot.workoutId,
                latitude = point.latitude,
                longitude = point.longitude,
                timestamp = point.timestamp.toEpochMilli(),
                accuracy = point.accuracy,
                sortIndex = index
            )
        }
    }

    private suspend fun checkAchievements(): List<AchievementDef> {
        return try {
            val allWorkouts = workoutRepository.getAllCompletedWorkoutsOnce()
            val unlockedIds = characterRepository.getUnlockedAchievementIds()
            val characterLevel = characterRepository.getProfileLevel()
            val totalLaps = workoutRepository.getTotalLapCount()
            val dogWalkCount = workoutRepository.getCompletedDogWalkCount()
            val totalDogDistMeters = workoutRepository.getTotalDogWalkDistanceMeters()

            val snapshot = AchievementSnapshot(
                allWorkouts = allWorkouts,
                totalLapCount = totalLaps,
                dogWalkCount = dogWalkCount,
                characterLevel = characterLevel,
                unlockedIds = unlockedIds,
                totalDogWalkDistanceMiles = DistanceCalculator.metersToMiles(totalDogDistMeters),
            )

            val newAchievements = AchievementChecker.checkNewAchievements(snapshot)
            for (achievement in newAchievements) {
                characterRepository.unlockAchievement(def = achievement)
            }
            newAchievements
        } catch (e: Exception) {
            Log.e("WorkoutSaveManager", "Failed to check achievements", e)
            emptyList()
        }
    }

    private suspend fun checkJuniperAchievements(): List<AchievementDef> {
        return try {
            val allWorkouts = workoutRepository.getAllCompletedWorkoutsOnce()
            val unlockedIds = characterRepository.getUnlockedAchievementIds(profileId = 2)
            val juniperLevel = characterRepository.getProfileLevel(profileId = 2)
            val dogWalkCount = workoutRepository.getCompletedDogWalkCount()
            val totalDogDistMeters = workoutRepository.getTotalDogWalkDistanceMeters()

            val snapshot = AchievementSnapshot(
                allWorkouts = allWorkouts,
                totalLapCount = 0,
                dogWalkCount = dogWalkCount,
                characterLevel = juniperLevel,
                unlockedIds = unlockedIds,
                totalDogWalkDistanceMiles = DistanceCalculator.metersToMiles(totalDogDistMeters),
            )

            val newAchievements = AchievementChecker.checkJuniperAchievements(snapshot)
            for (achievement in newAchievements) {
                characterRepository.unlockAchievement(profileId = 2, def = achievement)
            }
            newAchievements
        } catch (e: Exception) {
            Log.e("WorkoutSaveManager", "Failed to check Juniper achievements", e)
            emptyList()
        }
    }

    private suspend fun recalculateStats() {
        try {
            val allWorkouts = workoutRepository.getAllCompletedWorkoutsOnce()
            val recentRuns = workoutRepository.getRecentCompletedRuns(20)
            val stats = StatsCalculator.calculateAll(allWorkouts, recentRuns)
            characterRepository.updateStats(stats = stats)
        } catch (e: Exception) {
            Log.e("WorkoutSaveManager", "Failed to recalculate stats", e)
        }
    }

    private suspend fun recalculateJuniperStats() {
        try {
            val dogWalks = workoutRepository.getCompletedDogWalks()
            val stats = StatsCalculator.calculateDogStats(dogWalks)
            characterRepository.updateStats(profileId = 2, stats = stats)
        } catch (e: Exception) {
            Log.e("WorkoutSaveManager", "Failed to recalculate Juniper stats", e)
        }
    }

    suspend fun updateDogWalkMetadata(
        workoutId: String,
        dogName: String?,
        routeTag: String?,
        weatherCondition: WeatherCondition?,
        weatherTemp: WeatherTemp?,
        energyLevel: EnergyLevel?,
        notes: String?
    ) {
        val existing = workoutDao.getWorkoutById(workoutId) ?: return
        workoutDao.updateWorkout(
            existing.copy(
                dogName = dogName,
                routeTag = routeTag,
                weatherCondition = weatherCondition,
                weatherTemp = weatherTemp,
                energyLevel = energyLevel,
                notes = notes
            )
        )
    }
}
