package com.gitfast.app

import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.XpTransactionEntity
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.CharacterStats
import com.gitfast.app.data.repository.CharacterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterRepositoryTest {

    private lateinit var characterDao: CharacterDao
    private lateinit var repository: CharacterRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        characterDao = mockk(relaxed = true)
        repository = CharacterRepository(characterDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- getProfile ---

    @Test
    fun `getProfile returns default when dao returns null`() = runTest {
        every { characterDao.getProfile() } returns flowOf(null)

        val profile = repository.getProfile().first()

        assertEquals(CharacterProfile(), profile)
    }

    @Test
    fun `getProfile maps entity to domain with level progress`() = runTest {
        val entity = CharacterProfileEntity(id = 1, totalXp = 75, level = 2)
        every { characterDao.getProfile() } returns flowOf(entity)

        val profile = repository.getProfile().first()

        assertEquals(2, profile.level)
        assertEquals(75, profile.totalXp)
        // Level 2 starts at 50 XP, level 3 at 150 XP
        assertEquals(50, profile.xpForCurrentLevel)
        assertEquals(150, profile.xpForNextLevel)
        assertEquals(25, profile.xpProgressInLevel) // 75 - 50
        assertEquals(0.25f, profile.xpProgress, 0.01f) // 25 / 100
    }

    @Test
    fun `getProfile maps stat fields`() = runTest {
        val entity = CharacterProfileEntity(
            id = 1, totalXp = 0, level = 1,
            speedStat = 5, enduranceStat = 3, consistencyStat = 7,
        )
        every { characterDao.getProfile() } returns flowOf(entity)

        val profile = repository.getProfile().first()

        assertEquals(5, profile.speedStat)
        assertEquals(3, profile.enduranceStat)
        assertEquals(7, profile.consistencyStat)
    }

    // --- getXpByWorkout ---

    @Test
    fun `getXpByWorkout returns empty map when no transactions`() = runTest {
        every { characterDao.getAllXpTransactions() } returns flowOf(emptyList())

        val map = repository.getXpByWorkout().first()

        assertEquals(emptyMap<String, Int>(), map)
    }

    @Test
    fun `getXpByWorkout maps transactions to workoutId-xp pairs`() = runTest {
        val txs = listOf(
            XpTransactionEntity("t1", "w1", 50, "Run", 1000L),
            XpTransactionEntity("t2", "w2", 30, "Walk", 2000L),
        )
        every { characterDao.getAllXpTransactions() } returns flowOf(txs)

        val map = repository.getXpByWorkout().first()

        assertEquals(50, map["w1"])
        assertEquals(30, map["w2"])
    }

    // --- getXpTransactionForWorkout ---

    @Test
    fun `getXpTransactionForWorkout returns null when not found`() = runTest {
        coEvery { characterDao.getXpTransactionForWorkout("w1") } returns null

        val result = repository.getXpTransactionForWorkout("w1")

        assertNull(result)
    }

    @Test
    fun `getXpTransactionForWorkout maps entity to domain`() = runTest {
        val entity = XpTransactionEntity("t1", "w1", 50, "Run completed", 1000L)
        coEvery { characterDao.getXpTransactionForWorkout("w1") } returns entity

        val result = repository.getXpTransactionForWorkout("w1")!!

        assertEquals("t1", result.id)
        assertEquals("w1", result.workoutId)
        assertEquals(50, result.xpAmount)
        assertEquals("Run completed", result.reason)
        assertEquals(1000L, result.timestamp.toEpochMilli())
    }

    // --- getRecentXpTransactions ---

    @Test
    fun `getRecentXpTransactions maps entities to domain list`() = runTest {
        val entities = listOf(
            XpTransactionEntity("t1", "w1", 50, "Run", 2000L),
            XpTransactionEntity("t2", "w2", 30, "Walk", 1000L),
        )
        every { characterDao.getRecentXpTransactions(10) } returns flowOf(entities)

        val result = repository.getRecentXpTransactions(10).first()

        assertEquals(2, result.size)
        assertEquals("t1", result[0].id)
        assertEquals("t2", result[1].id)
    }

    // --- awardXp ---

    @Test
    fun `awardXp returns 0 when already awarded for workout`() = runTest {
        val existing = XpTransactionEntity("t1", "w1", 50, "Run", 1000L)
        coEvery { characterDao.getXpTransactionForWorkout("w1") } returns existing

        val result = repository.awardXp("w1", 50, "Run completed")

        assertEquals(0, result)
        coVerify(exactly = 0) { characterDao.insertXpTransaction(any()) }
    }

    @Test
    fun `awardXp inserts transaction and updates profile`() = runTest {
        coEvery { characterDao.getXpTransactionForWorkout("w1") } returns null
        val existingProfile = CharacterProfileEntity(id = 1, totalXp = 40, level = 1)
        coEvery { characterDao.getProfileOnce() } returns existingProfile

        val result = repository.awardXp("w1", 50, "Run completed")

        assertEquals(50, result)
        coVerify { characterDao.insertXpTransaction(any()) }
        val profileSlot = slot<CharacterProfileEntity>()
        coVerify { characterDao.updateProfile(capture(profileSlot)) }
        assertEquals(90, profileSlot.captured.totalXp)
        // 90 XP = level 2 (level 2 starts at 50)
        assertEquals(2, profileSlot.captured.level)
    }

    @Test
    fun `awardXp creates new profile when none exists`() = runTest {
        coEvery { characterDao.getXpTransactionForWorkout("w1") } returns null
        coEvery { characterDao.getProfileOnce() } returns null

        val result = repository.awardXp("w1", 100, "First run")

        assertEquals(100, result)
        coVerify { characterDao.insertProfile(any()) }
        val profileSlot = slot<CharacterProfileEntity>()
        coVerify { characterDao.updateProfile(capture(profileSlot)) }
        assertEquals(100, profileSlot.captured.totalXp)
    }

    @Test
    fun `awardXp calculates level correctly after XP increase`() = runTest {
        coEvery { characterDao.getXpTransactionForWorkout("w1") } returns null
        // At 140 XP, level 2 (needs 150 for level 3)
        val existingProfile = CharacterProfileEntity(id = 1, totalXp = 140, level = 2)
        coEvery { characterDao.getProfileOnce() } returns existingProfile

        repository.awardXp("w1", 20, "Run completed")

        val profileSlot = slot<CharacterProfileEntity>()
        coVerify { characterDao.updateProfile(capture(profileSlot)) }
        assertEquals(160, profileSlot.captured.totalXp)
        // 160 XP → level 3 (level 3 starts at 150)
        assertEquals(3, profileSlot.captured.level)
    }

    // --- updateStats ---

    @Test
    fun `updateStats updates existing profile with new stats`() = runTest {
        val existingProfile = CharacterProfileEntity(
            id = 1, totalXp = 100, level = 2,
            speedStat = 1, enduranceStat = 1, consistencyStat = 1,
        )
        coEvery { characterDao.getProfileOnce() } returns existingProfile

        repository.updateStats(CharacterStats(speed = 5, endurance = 3, consistency = 7))

        val profileSlot = slot<CharacterProfileEntity>()
        coVerify { characterDao.updateProfile(capture(profileSlot)) }
        assertEquals(5, profileSlot.captured.speedStat)
        assertEquals(3, profileSlot.captured.enduranceStat)
        assertEquals(7, profileSlot.captured.consistencyStat)
        // XP/level should be preserved
        assertEquals(100, profileSlot.captured.totalXp)
        assertEquals(2, profileSlot.captured.level)
    }

    @Test
    fun `updateStats creates new profile when none exists`() = runTest {
        coEvery { characterDao.getProfileOnce() } returns null

        repository.updateStats(CharacterStats(speed = 3, endurance = 4, consistency = 5))

        coVerify { characterDao.insertProfile(any()) }
        val profileSlot = slot<CharacterProfileEntity>()
        coVerify { characterDao.updateProfile(capture(profileSlot)) }
        assertEquals(3, profileSlot.captured.speedStat)
        assertEquals(4, profileSlot.captured.enduranceStat)
        assertEquals(5, profileSlot.captured.consistencyStat)
    }
}
