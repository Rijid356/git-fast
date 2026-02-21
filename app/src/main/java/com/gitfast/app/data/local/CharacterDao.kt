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

    @Query("SELECT * FROM character_profile WHERE id = :profileId")
    fun getProfile(profileId: Int): Flow<CharacterProfileEntity?>

    @Query("SELECT * FROM character_profile WHERE id = :profileId")
    suspend fun getProfileOnce(profileId: Int): CharacterProfileEntity?

    @Update
    suspend fun updateProfile(profile: CharacterProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CharacterProfileEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertXpTransaction(tx: XpTransactionEntity)

    @Query("SELECT * FROM xp_transactions WHERE profileId = :profileId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentXpTransactions(profileId: Int, limit: Int): Flow<List<XpTransactionEntity>>

    @Query("SELECT * FROM xp_transactions WHERE workoutId = :workoutId AND profileId = :profileId LIMIT 1")
    suspend fun getXpTransactionForWorkout(workoutId: String, profileId: Int): XpTransactionEntity?

    @Query("SELECT COUNT(*) FROM xp_transactions WHERE profileId = :profileId")
    fun getTotalTransactionCount(profileId: Int): Flow<Int>

    @Query("SELECT * FROM xp_transactions WHERE profileId = :profileId")
    fun getAllXpTransactions(profileId: Int): Flow<List<XpTransactionEntity>>

    @Query("SELECT * FROM xp_transactions WHERE profileId = :profileId")
    suspend fun getXpTransactionsOnce(profileId: Int): List<XpTransactionEntity>

    // --- Achievements ---

    @Query("SELECT * FROM unlocked_achievements WHERE profileId = :profileId ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(profileId: Int): Flow<List<UnlockedAchievementEntity>>

    @Query("SELECT achievementId FROM unlocked_achievements WHERE profileId = :profileId")
    suspend fun getUnlockedAchievementIds(profileId: Int): List<String>

    @Query("SELECT * FROM unlocked_achievements WHERE profileId = :profileId")
    suspend fun getUnlockedAchievementsOnce(profileId: Int): List<UnlockedAchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlockedAchievement(entity: UnlockedAchievementEntity)
}
