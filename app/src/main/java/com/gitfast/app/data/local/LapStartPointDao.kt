package com.gitfast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gitfast.app.data.local.entity.LapStartPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LapStartPointDao {

    @Insert
    suspend fun insert(point: LapStartPointEntity)

    @Query("SELECT * FROM lap_start_points")
    suspend fun getAll(): List<LapStartPointEntity>

    @Query("DELETE FROM lap_start_points")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM lap_start_points")
    fun observeCount(): Flow<Int>
}
