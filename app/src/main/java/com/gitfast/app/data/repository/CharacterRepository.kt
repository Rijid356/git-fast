package com.gitfast.app.data.repository

import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.XpTransactionEntity
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.util.XpCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class CharacterRepository @Inject constructor(
    private val characterDao: CharacterDao,
) {

    fun getProfile(): Flow<CharacterProfile> {
        return characterDao.getProfile().map { entity ->
            entity?.toCharacterProfile() ?: CharacterProfile()
        }
    }

    fun getRecentXpTransactions(limit: Int = 10): Flow<List<XpTransaction>> {
        return characterDao.getRecentXpTransactions(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * Award XP for a workout. Idempotent — won't double-award if called again
     * for the same workoutId.
     *
     * @return the XP amount awarded, or 0 if already awarded
     */
    suspend fun awardXp(workoutId: String, xpAmount: Int, reason: String): Int {
        // Prevent double-award
        val existing = characterDao.getXpTransactionForWorkout(workoutId)
        if (existing != null) return 0

        // Insert transaction
        val tx = XpTransactionEntity(
            id = UUID.randomUUID().toString(),
            workoutId = workoutId,
            xpAmount = xpAmount,
            reason = reason,
            timestamp = System.currentTimeMillis(),
        )
        characterDao.insertXpTransaction(tx)

        // Update profile
        val profile = characterDao.getProfileOnce()
            ?: CharacterProfileEntity(id = 1, totalXp = 0, level = 1).also {
                characterDao.insertProfile(it)
            }
        val newTotalXp = profile.totalXp + xpAmount
        val newLevel = XpCalculator.levelForXp(newTotalXp)
        characterDao.updateProfile(profile.copy(totalXp = newTotalXp, level = newLevel))

        return xpAmount
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
