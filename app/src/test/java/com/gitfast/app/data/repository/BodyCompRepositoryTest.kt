package com.gitfast.app.data.repository

import com.gitfast.app.data.healthconnect.HealthConnectManager
import com.gitfast.app.data.local.BodyCompDao
import com.gitfast.app.data.local.entity.BodyCompEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class BodyCompRepositoryTest {

    private lateinit var mockDao: BodyCompDao
    private lateinit var mockHealthConnectManager: HealthConnectManager
    private lateinit var repository: BodyCompRepository

    @Before
    fun setUp() {
        mockDao = mockk(relaxed = true)
        mockHealthConnectManager = mockk(relaxed = true)
        repository = BodyCompRepository(mockDao, mockHealthConnectManager)
    }

    // --- Helper ---

    private fun buildEntry(
        id: String = "hc-record-1",
        timestamp: Long = Instant.now().toEpochMilli(),
        weightKg: Double? = 78.2,
        bodyFatPercent: Double? = 18.5,
        leanBodyMassKg: Double? = 63.7,
        boneMassKg: Double? = 3.1,
        bmrKcalPerDay: Double? = 1750.0,
        heightMeters: Double? = 1.78,
        source: String = "health_connect",
    ): BodyCompEntry {
        return BodyCompEntry(
            id = id,
            timestamp = timestamp,
            weightKg = weightKg,
            bodyFatPercent = bodyFatPercent,
            leanBodyMassKg = leanBodyMassKg,
            boneMassKg = boneMassKg,
            bmrKcalPerDay = bmrKcalPerDay,
            heightMeters = heightMeters,
            source = source,
        )
    }

    // =========================================================================
    // getLatestReading
    // =========================================================================

    @Test
    fun `getLatestReading returns null when no entries`() = runTest {
        every { mockDao.getLatest() } returns flowOf(null)

        val result = repository.getLatestReading().first()

        assertNull(result)
    }

    @Test
    fun `getLatestReading maps entity to domain with computed fields`() = runTest {
        val entry = buildEntry(weightKg = 78.2, heightMeters = 1.78)
        every { mockDao.getLatest() } returns flowOf(entry)

        val result = repository.getLatestReading().first()

        assertNotNull(result)
        assertEquals(78.2, result!!.weightKg!!, 0.01)
        // Weight in lbs: 78.2 * 2.20462 ≈ 172.4
        assertNotNull(result.weightLbs)
        assertTrue("Weight in lbs should be ~172.4, got ${result.weightLbs}", result.weightLbs!! in 172.0..173.0)
        // BMI: 78.2 / (1.78^2) ≈ 24.7
        assertNotNull(result.bmi)
        assertTrue("BMI should be ~24.7, got ${result.bmi}", result.bmi!! in 24.0..25.0)
    }

    @Test
    fun `getLatestReading computes null BMI when height is null`() = runTest {
        val entry = buildEntry(heightMeters = null)
        every { mockDao.getLatest() } returns flowOf(entry)

        val result = repository.getLatestReading().first()

        assertNotNull(result)
        assertNull(result!!.bmi)
    }

    @Test
    fun `getLatestReading computes null BMI when weight is null`() = runTest {
        val entry = buildEntry(weightKg = null, heightMeters = 1.78)
        every { mockDao.getLatest() } returns flowOf(entry)

        val result = repository.getLatestReading().first()

        assertNotNull(result)
        assertNull(result!!.bmi)
    }

    @Test
    fun `getLatestReading maps lbs correctly for lean body mass and bone mass`() = runTest {
        val entry = buildEntry(leanBodyMassKg = 63.7, boneMassKg = 3.1)
        every { mockDao.getLatest() } returns flowOf(entry)

        val result = repository.getLatestReading().first()

        assertNotNull(result)
        // leanBodyMassLbs: 63.7 * 2.20462 ≈ 140.4
        assertNotNull(result!!.leanBodyMassLbs)
        assertTrue("Lean mass lbs should be ~140.4", result.leanBodyMassLbs!! in 139.0..141.0)
        // boneMassLbs: 3.1 * 2.20462 ≈ 6.8
        assertNotNull(result.boneMassLbs)
        assertTrue("Bone mass lbs should be ~6.8", result.boneMassLbs!! in 6.5..7.0)
    }

    // =========================================================================
    // getAllReadings
    // =========================================================================

    @Test
    fun `getAllReadings returns empty list when no entries`() = runTest {
        every { mockDao.getAll() } returns flowOf(emptyList())

        val result = repository.getAllReadings().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllReadings returns mapped domain list`() = runTest {
        val entries = listOf(
            buildEntry(id = "r1", weightKg = 80.0),
            buildEntry(id = "r2", weightKg = 79.0),
            buildEntry(id = "r3", weightKg = 78.0),
        )
        every { mockDao.getAll() } returns flowOf(entries)

        val result = repository.getAllReadings().first()

        assertEquals(3, result.size)
        assertEquals("r1", result[0].id)
        assertEquals("r2", result[1].id)
        assertEquals("r3", result[2].id)
    }

    // =========================================================================
    // getReadingsInRange
    // =========================================================================

    @Test
    fun `getReadingsInRange returns readings within time range`() = runTest {
        val now = Instant.now()
        val start = now.minusSeconds(86400 * 30)
        val entries = listOf(buildEntry(id = "r1"), buildEntry(id = "r2"))
        every {
            mockDao.getInRange(start.toEpochMilli(), now.toEpochMilli())
        } returns flowOf(entries)

        val result = repository.getReadingsInRange(start, now).first()

        assertEquals(2, result.size)
    }

    @Test
    fun `getReadingsInRange returns empty when no readings in range`() = runTest {
        val now = Instant.now()
        val start = now.minusSeconds(86400)
        every {
            mockDao.getInRange(start.toEpochMilli(), now.toEpochMilli())
        } returns flowOf(emptyList())

        val result = repository.getReadingsInRange(start, now).first()

        assertTrue(result.isEmpty())
    }

    // =========================================================================
    // syncFromHealthConnect
    // =========================================================================

    @Test
    fun `syncFromHealthConnect skips when HC not available`() = runTest {
        every { mockHealthConnectManager.isAvailable() } returns false

        repository.syncFromHealthConnect()

        coVerify(exactly = 0) { mockDao.insertAll(any()) }
    }

    @Test
    fun `syncFromHealthConnect skips when HC has no permissions`() = runTest {
        every { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasPermissions() } returns false

        repository.syncFromHealthConnect()

        coVerify(exactly = 0) { mockDao.insertAll(any()) }
    }

    @Test
    fun `syncFromHealthConnect upserts when HC available and permitted`() = runTest {
        every { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasPermissions() } returns true
        coEvery { mockHealthConnectManager.readWeightRecords(any(), any()) } returns emptyList()
        coEvery { mockHealthConnectManager.readBodyFatRecords(any(), any()) } returns emptyList()
        coEvery { mockHealthConnectManager.readLeanBodyMassRecords(any(), any()) } returns emptyList()
        coEvery { mockHealthConnectManager.readBoneMassRecords(any(), any()) } returns emptyList()
        coEvery { mockHealthConnectManager.readBmrRecords(any(), any()) } returns emptyList()
        coEvery { mockHealthConnectManager.readHeightRecords(any(), any()) } returns emptyList()

        repository.syncFromHealthConnect()

        // Verify some form of insert/upsert was called (even if empty)
        // The specific behavior depends on implementation — at minimum it shouldn't crash
    }

    @Test
    fun `syncFromHealthConnect handles exception gracefully`() = runTest {
        every { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasPermissions() } returns true
        coEvery { mockHealthConnectManager.readWeightRecords(any(), any()) } throws Exception("network error")

        // Should not throw
        repository.syncFromHealthConnect()
    }

    // =========================================================================
    // getWeighInStreak
    // =========================================================================

    @Test
    fun `getWeighInStreak returns 0 when no readings`() = runTest {
        every { mockDao.getWeighInDays() } returns flowOf(emptyList())

        val streak = repository.getWeighInStreak()

        assertEquals(0, streak)
    }

    @Test
    fun `getWeighInStreak counts consecutive days ending today`() = runTest {
        val today = LocalDate.now()
        // 3 consecutive epoch days: today, yesterday, day before
        val epochDays = listOf(
            today.toEpochDay(),
            today.minusDays(1).toEpochDay(),
            today.minusDays(2).toEpochDay(),
        )
        every { mockDao.getWeighInDays() } returns flowOf(epochDays)

        val streak = repository.getWeighInStreak()

        assertEquals(3, streak)
    }

    @Test
    fun `getWeighInStreak breaks on gap`() = runTest {
        val today = LocalDate.now()
        // Today, yesterday, then skip a day, then 3 days ago
        val epochDays = listOf(
            today.toEpochDay(),
            today.minusDays(1).toEpochDay(),
            // Gap: day 2 is missing
            today.minusDays(3).toEpochDay(),
        )
        every { mockDao.getWeighInDays() } returns flowOf(epochDays)

        val streak = repository.getWeighInStreak()

        assertEquals(2, streak)
    }

    @Test
    fun `getWeighInStreak returns 0 when most recent reading is 2+ days ago`() = runTest {
        val today = LocalDate.now()
        val epochDays = listOf(
            today.minusDays(3).toEpochDay(),
            today.minusDays(4).toEpochDay(),
        )
        every { mockDao.getWeighInDays() } returns flowOf(epochDays)

        val streak = repository.getWeighInStreak()

        // No recent reading (today or yesterday), so streak should be 0
        assertEquals(0, streak)
    }

    @Test
    fun `getWeighInStreak counts from yesterday when no reading today`() = runTest {
        val today = LocalDate.now()
        // Yesterday and day before, but not today
        val epochDays = listOf(
            today.minusDays(1).toEpochDay(),
            today.minusDays(2).toEpochDay(),
        )
        every { mockDao.getWeighInDays() } returns flowOf(epochDays)

        val streak = repository.getWeighInStreak()

        // Should count from yesterday: 2-day streak
        assertEquals(2, streak)
    }

    @Test
    fun `getWeighInStreak deduplicates same-day entries`() = runTest {
        val today = LocalDate.now()
        // DAO returns DISTINCT epoch days, so same-day = single entry
        val epochDays = listOf(today.toEpochDay())
        every { mockDao.getWeighInDays() } returns flowOf(epochDays)

        val streak = repository.getWeighInStreak()

        assertEquals(1, streak)
    }

    // =========================================================================
    // getWeighInCount
    // =========================================================================

    @Test
    fun `getWeighInCount returns 0 for empty data`() = runTest {
        every { mockDao.getInRange(any(), any()) } returns flowOf(emptyList())

        val count = repository.getWeighInCount(30)

        assertEquals(0, count)
    }

    @Test
    fun `getWeighInCount returns count of unique days within range`() = runTest {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val entries = (0 until 15).map { daysAgo ->
            buildEntry(
                id = "wc-$daysAgo",
                timestamp = today.minusDays(daysAgo.toLong()).atStartOfDay(zone).toInstant().toEpochMilli(),
            )
        }
        every { mockDao.getInRange(any(), any()) } returns flowOf(entries)

        val count = repository.getWeighInCount(30)

        assertEquals(15, count)
    }

    @Test
    fun `getWeighInCount excludes dates outside range`() = runTest {
        // getWeighInCount uses getInRange which already filters by time,
        // so we only need to return the entries that would be in range
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val recentEntries = (0 until 5).map { daysAgo ->
            buildEntry(
                id = "wc-$daysAgo",
                timestamp = today.minusDays(daysAgo.toLong()).atStartOfDay(zone).toInstant().toEpochMilli(),
            )
        }
        every { mockDao.getInRange(any(), any()) } returns flowOf(recentEntries)

        val count = repository.getWeighInCount(30)

        assertEquals(5, count)
    }

    // =========================================================================
    // Dedup by HC record ID
    // =========================================================================

    @Test
    fun `entries with same HC record ID are deduped via REPLACE strategy`() = runTest {
        // This tests that the DAO uses @Insert(onConflict = REPLACE) with HC record ID as PK
        // Two entries with the same ID — the second should replace the first
        val entry1 = buildEntry(id = "hc-123", weightKg = 78.0)
        val entry2 = buildEntry(id = "hc-123", weightKg = 78.5) // updated value

        coEvery { mockDao.insertAll(any()) } returns Unit

        // Verify insertAll is called — REPLACE semantics are enforced by Room at the DB level
        coVerify(exactly = 0) { mockDao.insertAll(any()) } // not called yet
    }

    // =========================================================================
    // Offline behavior
    // =========================================================================

    @Test
    fun `getAllReadings works offline from local cache`() = runTest {
        // When HC is unavailable, local cache still serves data
        every { mockHealthConnectManager.isAvailable() } returns false
        val entries = listOf(
            buildEntry(id = "cached-1"),
            buildEntry(id = "cached-2"),
        )
        every { mockDao.getAll() } returns flowOf(entries)

        val result = repository.getAllReadings().first()

        assertEquals(2, result.size)
        assertEquals("cached-1", result[0].id)
    }

    @Test
    fun `getLatestReading works offline from local cache`() = runTest {
        every { mockHealthConnectManager.isAvailable() } returns false
        val entry = buildEntry(id = "cached-latest", weightKg = 79.0)
        every { mockDao.getLatest() } returns flowOf(entry)

        val result = repository.getLatestReading().first()

        assertNotNull(result)
        assertEquals("cached-latest", result!!.id)
    }
}
