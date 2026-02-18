package com.gitfast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.UnlockedAchievementEntity
import com.gitfast.app.data.local.entity.XpTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM character_profile WHERE id = 1")
    fun getProfile(): Flow<CharacterProfileEntity?>

    @Query("SELECT * FROM character_profile WHERE id = 1")
    suspend fun getProfileOnce(): CharacterProfileEntity?

    @Update
    suspend fun updateProfile(profile: CharacterProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CharacterProfileEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertXpTransaction(tx: XpTransactionEntity)

    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentXpTransactions(limit: Int): Flow<List<XpTransactionEntity>>

    @Query("SELECT * FROM xp_transactions WHERE workoutId = :workoutId LIMIT 1")
    suspend fun getXpTransactionForWorkout(workoutId: String): XpTransactionEntity?

    @Query("SELECT COUNT(*) FROM xp_transactions")
    fun getTotalTransactionCount(): Flow<Int>

    @Query("SELECT * FROM xp_transactions")
    fun getAllXpTransactions(): Flow<List<XpTransactionEntity>>

    // --- Achievements ---

    @Query("SELECT * FROM unlocked_achievements ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<UnlockedAchievementEntity>>

    @Query("SELECT achievementId FROM unlocked_achievements")
    suspend fun getUnlockedAchievementIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedAchievement(entity: UnlockedAchievementEntity)
}
