package com.gitfast.app.data.repository

import com.gitfast.app.data.local.ExerciseDao
import com.gitfast.app.data.local.mappers.toDomain
import com.gitfast.app.data.local.mappers.toEntity
import com.gitfast.app.data.model.ExerciseSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
) {

    suspend fun saveSession(session: ExerciseSession) {
        exerciseDao.saveSessionWithSets(
            session = session.toEntity(),
            sets = session.sets.map { it.toEntity() },
        )
    }

    suspend fun getSession(id: String): ExerciseSession? {
        val entity = exerciseDao.getSessionById(id) ?: return null
        val sets = exerciseDao.getSetsForSession(id).map { it.toDomain() }
        return entity.toDomain(sets)
    }

    fun getAllSessions(): Flow<List<ExerciseSession>> {
        return exerciseDao.getAllSessions().map { sessions ->
            sessions.map { it.toDomain() }
        }
    }

    suspend fun getRecentSessions(limit: Int): List<ExerciseSession> {
        return exerciseDao.getRecentSessions(limit).map { it.toDomain() }
    }

    suspend fun getSessionCount(): Int {
        return exerciseDao.getSessionCount()
    }

    suspend fun getTotalSetCount(): Int {
        return exerciseDao.getTotalSetCount()
    }

    suspend fun getTotalRepCount(): Int {
        return exerciseDao.getTotalRepCount() ?: 0
    }

    suspend fun getLast30DaysSessions(): List<ExerciseSession> {
        val thirtyDaysAgo = Instant.now()
            .atZone(ZoneId.systemDefault())
            .minusDays(30)
            .toInstant()
            .toEpochMilli()
        return exerciseDao.getSessionsSince(thirtyDaysAgo).map { it.toDomain() }
    }

    suspend fun getLast30DaysSetsWithReps(): List<Pair<Int, Boolean>> {
        val thirtyDaysAgo = Instant.now()
            .atZone(ZoneId.systemDefault())
            .minusDays(30)
            .toInstant()
            .toEpochMilli()
        val sessions = exerciseDao.getSessionsSince(thirtyDaysAgo)
        val allSets = sessions.flatMap { session ->
            exerciseDao.getSetsForSession(session.id)
        }
        return allSets.map { it.reps to (it.weightLbs != null) }
    }
}
