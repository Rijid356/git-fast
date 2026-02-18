package com.gitfast.app.data.repository

import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.UnlockedAchievementEntity
import com.gitfast.app.data.local.entity.XpTransactionEntity
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.CharacterStats
import com.gitfast.app.data.model.UnlockedAchievement
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.util.AchievementDef
import com.gitfast.app.util.XpCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class CharacterRepository @Inject constructor(
    private val characterDao: CharacterDao,
) {

    fun getProfile(profileId: Int = 1): Flow<CharacterProfile> {
        return characterDao.getProfile(profileId).map { entity ->
            entity?.toCharacterProfile() ?: CharacterProfile()
        }
    }

    fun getXpByWorkout(profileId: Int = 1): Flow<Map<String, Int>> {
        return characterDao.getAllXpTransactions(profileId).map { list ->
            list.associate { it.workoutId to it.xpAmount }
        }
    }

    suspend fun getXpTransactionForWorkout(workoutId: String, profileId: Int = 1): XpTransaction? {
        return characterDao.getXpTransactionForWorkout(workoutId, profileId)?.toDomain()
    }

    fun getRecentXpTransactions(profileId: Int = 1, limit: Int = 10): Flow<List<XpTransaction>> {
        return characterDao.getRecentXpTransactions(profileId, limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * Award XP for a workout. Idempotent — won't double-award if called again
     * for the same workoutId + profileId.
     *
     * @return the XP amount awarded, or 0 if already awarded
     */
    suspend fun awardXp(profileId: Int = 1, workoutId: String, xpAmount: Int, reason: String): Int {
        // Prevent double-award per profile
        val existing = characterDao.getXpTransactionForWorkout(workoutId, profileId)
        if (existing != null) return 0

        // Insert transaction
        val tx = XpTransactionEntity(
            id = UUID.randomUUID().toString(),
            workoutId = workoutId,
            xpAmount = xpAmount,
            reason = reason,
            timestamp = System.currentTimeMillis(),
            profileId = profileId,
        )
        characterDao.insertXpTransaction(tx)

        // Update profile
        val profile = characterDao.getProfileOnce(profileId)
            ?: CharacterProfileEntity(id = profileId, totalXp = 0, level = 1).also {
                characterDao.insertProfile(it)
            }
        val newTotalXp = profile.totalXp + xpAmount
        val newLevel = XpCalculator.levelForXp(newTotalXp)
        characterDao.updateProfile(profile.copy(totalXp = newTotalXp, level = newLevel))

        return xpAmount
    }

    suspend fun updateStats(profileId: Int = 1, stats: CharacterStats) {
        val profile = characterDao.getProfileOnce(profileId)
            ?: CharacterProfileEntity(id = profileId).also {
                characterDao.insertProfile(it)
            }
        characterDao.updateProfile(
            profile.copy(
                speedStat = stats.speed,
                enduranceStat = stats.endurance,
                consistencyStat = stats.consistency,
            )
        )
    }

    private fun CharacterProfileEntity.toCharacterProfile(): CharacterProfile {
        val currentLevelXp = XpCalculator.xpForLevel(level)
        val nextLevelXp = XpCalculator.xpForLevel(level + 1)
        val xpInLevel = totalXp - currentLevelXp
        val xpNeeded = nextLevelXp - currentLevelXp
        return CharacterProfile(
            level = level,
            totalXp = totalXp,
            xpForCurrentLevel = currentLevelXp,
            xpForNextLevel = nextLevelXp,
            xpProgressInLevel = xpInLevel,
            xpProgress = if (xpNeeded > 0) xpInLevel.toFloat() / xpNeeded else 0f,
            speedStat = speedStat,
            enduranceStat = enduranceStat,
            consistencyStat = consistencyStat,
        )
    }

    suspend fun getProfileLevel(profileId: Int = 1): Int {
        return characterDao.getProfileOnce(profileId)?.level ?: 1
    }

    // --- Achievements ---

    fun getUnlockedAchievements(profileId: Int = 1): Flow<List<UnlockedAchievement>> {
        return characterDao.getUnlockedAchievements(profileId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getUnlockedAchievementIds(profileId: Int = 1): Set<String> {
        return characterDao.getUnlockedAchievementIds(profileId).toSet()
    }

    /**
     * Unlock an achievement and award its bonus XP.
     * Uses "achievement:<id>" as the workoutId for the XP transaction to avoid collisions.
     *
     * @return the XP awarded, or 0 if already unlocked
     */
    suspend fun unlockAchievement(profileId: Int = 1, def: AchievementDef): Int {
        val existing = characterDao.getUnlockedAchievementIds(profileId)
        if (def.id in existing) return 0

        characterDao.insertUnlockedAchievement(
            UnlockedAchievementEntity(
                achievementId = def.id,
                unlockedAt = System.currentTimeMillis(),
                xpAwarded = def.xpReward,
                profileId = profileId,
            )
        )

        return awardXp(
            profileId = profileId,
            workoutId = "achievement:${def.id}",
            xpAmount = def.xpReward,
            reason = "Achievement: ${def.title}",
        )
    }

    private fun UnlockedAchievementEntity.toDomain(): UnlockedAchievement {
        return UnlockedAchievement(
            achievementId = achievementId,
            unlockedAt = Instant.ofEpochMilli(unlockedAt),
            xpAwarded = xpAwarded,
        )
    }

    private fun XpTransactionEntity.toDomain(): XpTransaction {
        return XpTransaction(
            id = id,
            workoutId = workoutId,
            xpAmount = xpAmount,
            reason = reason,
            timestamp = Instant.ofEpochMilli(timestamp),
        )
    }
}
