package com.gitfast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gitfast.app.data.local.entity.ScreenshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {

    @Insert
    suspend fun insert(screenshot: ScreenshotEntity)

    @Query("SELECT * FROM screenshots ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE workoutId = :workoutId ORDER BY timestamp DESC")
    fun getByWorkoutId(workoutId: String): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ScreenshotEntity>

    @Query("DELETE FROM screenshots WHERE id = :id")
    suspend fun deleteById(id: Long)
}
