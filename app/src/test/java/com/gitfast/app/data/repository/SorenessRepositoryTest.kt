package com.gitfast.app.data.repository

import com.gitfast.app.data.local.SorenessDao
import com.gitfast.app.data.local.entity.SorenessLogEntity
import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class SorenessRepositoryTest {

    private lateinit var mockDao: SorenessDao
    private lateinit var repository: SorenessRepository

    @Before
    fun setUp() {
        mockDao = mockk(relaxed = true)
        repository = SorenessRepository(mockDao)
    }

    // --- Helper ---

    private fun buildEntity(
        id: String = "log-1",
        date: Long = todayEpoch(),
        muscleGroups: String = "CHEST,BACK",
        intensity: String = "MODERATE",
        notes: String? = "felt sore",
        xpAwarded: Int = 10,
        createdAt: Long = 1_700_000_000_000L,
    ): SorenessLogEntity {
        return SorenessLogEntity(
            id = id,
            date = date,
            muscleGroups = muscleGroups,
            intensity = intensity,
            notes = notes,
            xpAwarded = xpAwarded,
            createdAt = createdAt,
        )
    }

    private fun todayEpoch(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    // =========================================================================
    // observeTodayLog
    // =========================================================================

    @Test
    fun `observeTodayLog returns mapped domain when entity exists`() = runTest {
        val entity = buildEntity()
        every { mockDao.observeByDate(any()) } returns flowOf(entity)

        val result = repository.observeTodayLog().first()

        assertNotNull(result)
        assertEquals("log-1", result!!.id)
        assertEquals(setOf(MuscleGroup.CHEST, MuscleGroup.BACK), result.muscleGroups)
        assertEquals(SorenessIntensity.MODERATE, result.intensity)
        assertEquals("felt sore", result.notes)
        assertEquals(10, result.xpAwarded)
    }

    @Test
    fun `observeTodayLog returns null when no entry`() = runTest {
        every { mockDao.observeByDate(any()) } returns flowOf(null)

        val result = repository.observeTodayLog().first()

        assertNull(result)
    }

    // =========================================================================
    // getTodayLog
    // =========================================================================

    @Test
    fun `getTodayLog returns mapped domain when entity exists`() = runTest {
        val entity = buildEntity()
        coEvery { mockDao.getByDate(any()) } returns entity

        val result = repository.getTodayLog()

        assertNotNull(result)
        assertEquals("log-1", result!!.id)
        assertEquals(SorenessIntensity.MODERATE, result.intensity)
    }

    @Test
    fun `getTodayLog returns null when no entry`() = runTest {
        coEvery { mockDao.getByDate(any()) } returns null

        val result = repository.getTodayLog()

        assertNull(result)
    }

    // =========================================================================
    // logSoreness — insert branch
    // =========================================================================

    @Test
    fun `logSoreness inserts new entry when no existing log`() = runTest {
        coEvery { mockDao.getByDate(any()) } returns null

        repository.logSoreness(
            muscleGroups = setOf(MuscleGroup.QUADS),
            intensity = SorenessIntensity.MILD,
            notes = null,
            xpAwarded = 5,
        )

        coVerify(exactly = 1) { mockDao.insert(any()) }
        coVerify(exactly = 0) { mockDao.update(any()) }
    }

    @Test
    fun `logSoreness generates UUID id for new entry`() = runTest {
        coEvery { mockDao.getByDate(any()) } returns null
        val entitySlot = slot<SorenessLogEntity>()
        coEvery { mockDao.insert(capture(entitySlot)) } returns Unit

        repository.logSoreness(
            muscleGroups = setOf(MuscleGroup.CORE),
            intensity = SorenessIntensity.MODERATE,
            notes = null,
            xpAwarded = 5,
        )

        assertEquals(36, entitySlot.captured.id.length)
    }

    @Test
    fun `logSoreness sets today date on new entry`() = runTest {
        coEvery { mockDao.getByDate(any()) } returns null
        val entitySlot = slot<SorenessLogEntity>()
        coEvery { mockDao.insert(capture(entitySlot)) } returns Unit

        repository.logSoreness(
            muscleGroups = setOf(MuscleGroup.GLUTES),
            intensity = SorenessIntensity.SEVERE,
            notes = null,
            xpAwarded = 5,
        )

        assertEquals(todayEpoch(), entitySlot.captured.date)
    }

    // =========================================================================
    // logSoreness — update branch
    // =========================================================================

    @Test
    fun `logSoreness updates existing entry when log exists`() = runTest {
        val existing = buildEntity(id = "existing-id")
        coEvery { mockDao.getByDate(any()) } returns existing

        repository.logSoreness(
            muscleGroups = setOf(MuscleGroup.SHOULDERS),
            intensity = SorenessIntensity.SEVERE,
            notes = "updated",
            xpAwarded = 15,
        )

        coVerify(exactly = 1) { mockDao.update(any()) }
        coVerify(exactly = 0) { mockDao.insert(any()) }
    }

    @Test
    fun `logSoreness update preserves original id`() = runTest {
        val existing = buildEntity(id = "keep-this-id")
        coEvery { mockDao.getByDate(any()) } returns existing
        val entitySlot = slot<SorenessLogEntity>()
        coEvery { mockDao.update(capture(entitySlot)) } returns Unit

        repository.logSoreness(
            muscleGroups = setOf(MuscleGroup.BICEPS),
            intensity = SorenessIntensity.MILD,
            notes = null,
            xpAwarded = 0,
        )

        assertEquals("keep-this-id", entitySlot.captured.id)
    }

    @Test
    fun `logSoreness update calls dao with correct entity`() = runTest {
        val existing = buildEntity(id = "upd-id")
        coEvery { mockDao.getByDate(any()) } returns existing
        val entitySlot = slot<SorenessLogEntity>()
        coEvery { mockDao.update(capture(entitySlot)) } returns Unit

        repository.logSoreness(
            muscleGroups = setOf(MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
            intensity = SorenessIntensity.SEVERE,
            notes = "leg day",
            xpAwarded = 20,
        )

        val captured = entitySlot.captured
        assertEquals("upd-id", captured.id)
        assertTrue(captured.muscleGroups.contains("HAMSTRINGS"))
        assertTrue(captured.muscleGroups.contains("CALVES"))
        assertEquals("SEVERE", captured.intensity)
        assertEquals("leg day", captured.notes)
        assertEquals(20, captured.xpAwarded)
    }

    // =========================================================================
    // getTotalCount
    // =========================================================================

    @Test
    fun `getTotalCount delegates to dao`() = runTest {
        coEvery { mockDao.getTotalCount() } returns 42

        val result = repository.getTotalCount()

        assertEquals(42, result)
    }

    // =========================================================================
    // getLast30DaysLogs
    // =========================================================================

    @Test
    fun `getLast30DaysLogs returns mapped domain list`() = runTest {
        val entities = listOf(
            buildEntity(id = "log-a"),
            buildEntity(id = "log-b"),
        )
        coEvery { mockDao.getLogsSince(any()) } returns entities

        val result = repository.getLast30DaysLogs()

        assertEquals(2, result.size)
        assertEquals("log-a", result[0].id)
        assertEquals("log-b", result[1].id)
    }

    @Test
    fun `getLast30DaysLogs returns empty list when no logs`() = runTest {
        coEvery { mockDao.getLogsSince(any()) } returns emptyList()

        val result = repository.getLast30DaysLogs()

        assertTrue(result.isEmpty())
    }
}
