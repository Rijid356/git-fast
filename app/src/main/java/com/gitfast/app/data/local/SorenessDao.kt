package com.gitfast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gitfast.app.data.local.entity.SorenessLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SorenessDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SorenessLogEntity)

    @Update
    suspend fun update(log: SorenessLogEntity)

    @Query("SELECT * FROM soreness_logs WHERE date = :dateEpoch LIMIT 1")
    suspend fun getByDate(dateEpoch: Long): SorenessLogEntity?

    @Query("SELECT * FROM soreness_logs WHERE date = :dateEpoch LIMIT 1")
    fun observeByDate(dateEpoch: Long): Flow<SorenessLogEntity?>

    @Query("SELECT * FROM soreness_logs ORDER BY date DESC")
    fun getAll(): Flow<List<SorenessLogEntity>>

    @Query("SELECT COUNT(*) FROM soreness_logs")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM soreness_logs WHERE date >= :sinceEpoch")
    suspend fun getCountSince(sinceEpoch: Long): Int

    @Query("SELECT * FROM soreness_logs WHERE date >= :sinceEpoch ORDER BY date DESC")
    suspend fun getLogsSince(sinceEpoch: Long): List<SorenessLogEntity>
}
