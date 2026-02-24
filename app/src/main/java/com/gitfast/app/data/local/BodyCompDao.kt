package com.gitfast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gitfast.app.data.local.entity.BodyCompEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyCompDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<BodyCompEntry>)

    @Query("SELECT * FROM body_comp_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BodyCompEntry>>

    @Query("SELECT * FROM body_comp_entries ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<BodyCompEntry?>

    @Query("SELECT * FROM body_comp_entries WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getInRange(start: Long, end: Long): Flow<List<BodyCompEntry>>

    @Query("SELECT DISTINCT timestamp / 86400000 AS day FROM body_comp_entries ORDER BY day DESC")
    fun getWeighInDays(): Flow<List<Long>>

    @Query("DELETE FROM body_comp_entries")
    suspend fun deleteAll()
}
