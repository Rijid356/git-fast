package com.gitfast.app.data.repository

import com.gitfast.app.data.local.ExerciseDao
import com.gitfast.app.data.local.entity.ExerciseSessionEntity
import com.gitfast.app.data.local.entity.ExerciseSetEntity
import com.gitfast.app.data.model.ExerciseSession
import com.gitfast.app.data.model.ExerciseSet
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ExerciseRepositoryTest {

    private lateinit var mockDao: ExerciseDao
    private lateinit var repository: ExerciseRepository

    @Before
    fun setUp() {
        mockDao = mockk(relaxed = true)
        repository = ExerciseRepository(mockDao)
    }

    // --- Helpers ---

    private fun buildSessionEntity(
        id: String = "session-1",
        startTime: Long = 1_700_000_000_000L,
        endTime: Long? = 1_700_003_600_000L,
        notes: String? = "good session",
        xpAwarded: Int = 25,
    ): ExerciseSessionEntity {
        return ExerciseSessionEntity(
            id = id,
            startTime = startTime,
            endTime = endTime,
            notes = notes,
            xpAwarded = xpAwarded,
        )
    }

    private fun buildSetEntity(
        id: String = "set-1",
        sessionId: String = "session-1",
        exerciseId: String = "bench-press",
        setNumber: Int = 1,
        reps: Int = 10,
        weightLbs: Double? = 135.0,
        durationSeconds: Int? = null,
        isWarmup: Boolean = false,
        completedAt: Long = 1_700_000_100_000L,
    ): ExerciseSetEntity {
        return ExerciseSetEntity(
            id = id,
            sessionId = sessionId,
            exerciseId = exerciseId,
            setNumber = setNumber,
            reps = reps,
            weightLbs = weightLbs,
            durationSeconds = durationSeconds,
            isWarmup = isWarmup,
            completedAt = completedAt,
        )
    }

    private fun buildSession(
        id: String = "session-1",
        startTime: Instant = Instant.ofEpochMilli(1_700_000_000_000L),
        endTime: Instant? = Instant.ofEpochMilli(1_700_003_600_000L),
        sets: List<ExerciseSet> = emptyList(),
        notes: String? = "good session",
        xpAwarded: Int = 25,
    ): ExerciseSession {
        return ExerciseSession(
            id = id,
            startTime = startTime,
            endTime = endTime,
            sets = sets,
            notes = notes,
            xpAwarded = xpAwarded,
        )
    }

    private fun buildSet(
        id: String = "set-1",
        sessionId: String = "session-1",
        exerciseId: String = "bench-press",
        setNumber: Int = 1,
        reps: Int = 10,
        weightLbs: Double? = 135.0,
        durationSeconds: Int? = null,
        isWarmup: Boolean = false,
        completedAt: Instant = Instant.ofEpochMilli(1_700_000_100_000L),
    ): ExerciseSet {
        return ExerciseSet(
            id = id,
            sessionId = sessionId,
            exerciseId = exerciseId,
            setNumber = setNumber,
            reps = reps,
            weightLbs = weightLbs,
            durationSeconds = durationSeconds,
            isWarmup = isWarmup,
            completedAt = completedAt,
        )
    }

    // =========================================================================
    // saveSession
    // =========================================================================

    @Test
    fun `saveSession calls dao with mapped entities`() = runTest {
        val session = buildSession(
            sets = listOf(buildSet(id = "s1"), buildSet(id = "s2")),
        )
        val sessionSlot = slot<ExerciseSessionEntity>()
        val setsSlot = slot<List<ExerciseSetEntity>>()
        coEvery {
            mockDao.saveSessionWithSets(capture(sessionSlot), capture(setsSlot))
        } returns Unit

        repository.saveSession(session)

        assertEquals("session-1", sessionSlot.captured.id)
        assertEquals(2, setsSlot.captured.size)
    }

    @Test
    fun `saveSession with empty sets passes empty list`() = runTest {
        val session = buildSession(sets = emptyList())
        val setsSlot = slot<List<ExerciseSetEntity>>()
        coEvery {
            mockDao.saveSessionWithSets(any(), capture(setsSlot))
        } returns Unit

        repository.saveSession(session)

        assertTrue(setsSlot.captured.isEmpty())
    }

    // =========================================================================
    // getSession
    // =========================================================================

    @Test
    fun `getSession returns null when not found`() = runTest {
        coEvery { mockDao.getSessionById("missing") } returns null

        val result = repository.getSession("missing")

        assertNull(result)
    }

    @Test
    fun `getSession returns domain with sets when found`() = runTest {
        val entity = buildSessionEntity(id = "s-1")
        val setEntities = listOf(
            buildSetEntity(id = "set-a", sessionId = "s-1"),
            buildSetEntity(id = "set-b", sessionId = "s-1"),
        )
        coEvery { mockDao.getSessionById("s-1") } returns entity
        coEvery { mockDao.getSetsForSession("s-1") } returns setEntities

        val result = repository.getSession("s-1")

        assertNotNull(result)
        assertEquals("s-1", result!!.id)
        assertEquals(2, result.sets.size)
        assertEquals("set-a", result.sets[0].id)
        assertEquals("set-b", result.sets[1].id)
    }

    @Test
    fun `getSession does not query sets when session missing`() = runTest {
        coEvery { mockDao.getSessionById("nope") } returns null

        repository.getSession("nope")

        coVerify(exactly = 0) { mockDao.getSetsForSession(any()) }
    }

    @Test
    fun `getSession returns session with empty sets`() = runTest {
        val entity = buildSessionEntity(id = "s-empty")
        coEvery { mockDao.getSessionById("s-empty") } returns entity
        coEvery { mockDao.getSetsForSession("s-empty") } returns emptyList()

        val result = repository.getSession("s-empty")

        assertNotNull(result)
        assertTrue(result!!.sets.isEmpty())
    }

    // =========================================================================
    // getAllSessions
    // =========================================================================

    @Test
    fun `getAllSessions returns mapped domain flow`() = runTest {
        val entities = listOf(
            buildSessionEntity(id = "a"),
            buildSessionEntity(id = "b"),
        )
        every { mockDao.getAllSessions() } returns flowOf(entities)

        val result = repository.getAllSessions().first()

        assertEquals(2, result.size)
        assertEquals("a", result[0].id)
        assertEquals("b", result[1].id)
    }

    @Test
    fun `getAllSessions returns empty flow`() = runTest {
        every { mockDao.getAllSessions() } returns flowOf(emptyList())

        val result = repository.getAllSessions().first()

        assertTrue(result.isEmpty())
    }

    // =========================================================================
    // getRecentSessions
    // =========================================================================

    @Test
    fun `getRecentSessions passes limit and maps`() = runTest {
        val entities = listOf(buildSessionEntity(id = "recent-1"))
        coEvery { mockDao.getRecentSessions(5) } returns entities

        val result = repository.getRecentSessions(5)

        assertEquals(1, result.size)
        assertEquals("recent-1", result[0].id)
        coVerify { mockDao.getRecentSessions(5) }
    }

    // =========================================================================
    // Delegate methods
    // =========================================================================

    @Test
    fun `getSessionCount delegates to dao`() = runTest {
        coEvery { mockDao.getSessionCount() } returns 12

        val result = repository.getSessionCount()

        assertEquals(12, result)
    }

    @Test
    fun `getTotalSetCount delegates to dao`() = runTest {
        coEvery { mockDao.getTotalSetCount() } returns 48

        val result = repository.getTotalSetCount()

        assertEquals(48, result)
    }

    // =========================================================================
    // getTotalRepCount
    // =========================================================================

    @Test
    fun `getTotalRepCount returns value when non-null`() = runTest {
        coEvery { mockDao.getTotalRepCount() } returns 350

        val result = repository.getTotalRepCount()

        assertEquals(350, result)
    }

    @Test
    fun `getTotalRepCount returns 0 when null`() = runTest {
        coEvery { mockDao.getTotalRepCount() } returns null

        val result = repository.getTotalRepCount()

        assertEquals(0, result)
    }

    // =========================================================================
    // getLast30DaysSessions
    // =========================================================================

    @Test
    fun `getLast30DaysSessions returns mapped list`() = runTest {
        val entities = listOf(
            buildSessionEntity(id = "30d-1"),
            buildSessionEntity(id = "30d-2"),
        )
        coEvery { mockDao.getSessionsSince(any()) } returns entities

        val result = repository.getLast30DaysSessions()

        assertEquals(2, result.size)
        assertEquals("30d-1", result[0].id)
    }

    @Test
    fun `getLast30DaysSessions returns empty when none`() = runTest {
        coEvery { mockDao.getSessionsSince(any()) } returns emptyList()

        val result = repository.getLast30DaysSessions()

        assertTrue(result.isEmpty())
    }

    // =========================================================================
    // getLast30DaysSetsWithReps
    // =========================================================================

    @Test
    fun `getLast30DaysSetsWithReps empty when no sessions`() = runTest {
        coEvery { mockDao.getSessionsSince(any()) } returns emptyList()

        val result = repository.getLast30DaysSetsWithReps()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getLast30DaysSetsWithReps hasWeight true when weightLbs set`() = runTest {
        val session = buildSessionEntity(id = "hw-1")
        val sets = listOf(buildSetEntity(sessionId = "hw-1", reps = 8, weightLbs = 100.0))
        coEvery { mockDao.getSessionsSince(any()) } returns listOf(session)
        coEvery { mockDao.getSetsForSession("hw-1") } returns sets

        val result = repository.getLast30DaysSetsWithReps()

        assertEquals(1, result.size)
        assertEquals(8, result[0].first)
        assertTrue(result[0].second)
    }

    @Test
    fun `getLast30DaysSetsWithReps hasWeight false when weightLbs null`() = runTest {
        val session = buildSessionEntity(id = "nw-1")
        val sets = listOf(buildSetEntity(sessionId = "nw-1", reps = 15, weightLbs = null))
        coEvery { mockDao.getSessionsSince(any()) } returns listOf(session)
        coEvery { mockDao.getSetsForSession("nw-1") } returns sets

        val result = repository.getLast30DaysSetsWithReps()

        assertEquals(1, result.size)
        assertEquals(15, result[0].first)
        assertFalse(result[0].second)
    }

    @Test
    fun `getLast30DaysSetsWithReps flatMaps across sessions`() = runTest {
        val sessions = listOf(
            buildSessionEntity(id = "fm-1"),
            buildSessionEntity(id = "fm-2"),
        )
        coEvery { mockDao.getSessionsSince(any()) } returns sessions
        coEvery { mockDao.getSetsForSession("fm-1") } returns listOf(
            buildSetEntity(id = "s1", sessionId = "fm-1", reps = 5),
            buildSetEntity(id = "s2", sessionId = "fm-1", reps = 5),
        )
        coEvery { mockDao.getSetsForSession("fm-2") } returns listOf(
            buildSetEntity(id = "s3", sessionId = "fm-2", reps = 8),
        )

        val result = repository.getLast30DaysSetsWithReps()

        assertEquals(3, result.size)
    }

    @Test
    fun `getLast30DaysSetsWithReps handles session with no sets`() = runTest {
        val session = buildSessionEntity(id = "empty-sets")
        coEvery { mockDao.getSessionsSince(any()) } returns listOf(session)
        coEvery { mockDao.getSetsForSession("empty-sets") } returns emptyList()

        val result = repository.getLast30DaysSetsWithReps()

        assertTrue(result.isEmpty())
    }
}
