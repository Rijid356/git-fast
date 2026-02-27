package com.gitfast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gitfast.app.data.local.entity.ExerciseSessionEntity
import com.gitfast.app.data.local.entity.ExerciseSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ExerciseSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: ExerciseSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<ExerciseSetEntity>)

    @Query("SELECT * FROM exercise_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): ExerciseSessionEntity?

    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    suspend fun getSetsForSession(sessionId: String): List<ExerciseSetEntity>

    @Query("SELECT * FROM exercise_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT COUNT(*) FROM exercise_sessions")
    suspend fun getSessionCount(): Int

    @Query("SELECT COUNT(*) FROM exercise_sets")
    suspend fun getTotalSetCount(): Int

    @Query("SELECT COUNT(*) FROM exercise_sets WHERE completedAt >= :timestamp")
    suspend fun getSetCountSince(timestamp: Long): Int

    @Query("SELECT * FROM exercise_sessions WHERE startTime >= :timestamp ORDER BY startTime DESC")
    suspend fun getSessionsSince(timestamp: Long): List<ExerciseSessionEntity>

    @Query("SELECT * FROM exercise_sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<ExerciseSessionEntity>

    @Query("SELECT SUM(reps) FROM exercise_sets")
    suspend fun getTotalRepCount(): Int?

    @Transaction
    suspend fun saveSessionWithSets(session: ExerciseSessionEntity, sets: List<ExerciseSetEntity>) {
        insertSession(session)
        insertSets(sets)
    }
}
