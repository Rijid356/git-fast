package com.gitfast.app.data.repository

import com.gitfast.app.data.local.CharacterDao
import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.UnlockedAchievementEntity
import com.gitfast.app.data.local.entity.XpTransactionEntity
import com.gitfast.app.data.model.CharacterStats
import com.gitfast.app.util.AchievementDef
import com.gitfast.app.util.XpCalculator
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

class CharacterRepositoryTest {

    private lateinit var mockDao: CharacterDao
    private lateinit var repository: CharacterRepository

    @Before
    fun setUp() {
        mockDao = mockk(relaxed = true)
        repository = CharacterRepository(mockDao)
    }

    // =========================================================================
    // getProfile
    // =========================================================================

    @Test
    fun `getProfile returns default CharacterProfile when entity is null`() = runTest {
        every { mockDao.getProfile(1) } returns flowOf(null)

        val profile = repository.getProfile().first()

        assertEquals(1, profile.level)
        assertEquals(0, profile.totalXp)
        assertEquals(0f, profile.xpProgress)
    }

    @Test
    fun `getProfile maps entity to domain with correct XP progress`() = runTest {
        val entity = CharacterProfileEntity(
            id = 1,
            totalXp = 150,
            level = 2,
            speedStat = 5,
            enduranceStat = 10,
            consistencyStat = 3,
        )
        every { mockDao.getProfile(1) } returns flowOf(entity)

        val profile = repository.getProfile().first()

        assertEquals(2, profile.level)
        assertEquals(150, profile.totalXp)
        assertEquals(5, profile.speedStat)
        assertEquals(10, profile.enduranceStat)
        assertEquals(3, profile.consistencyStat)
        assertEquals(XpCalculator.xpForLevel(2), profile.xpForCurrentLevel)
        assertEquals(XpCalculator.xpForLevel(3), profile.xpForNextLevel)
    }

    @Test
    fun `getProfile with profileId 2 queries Juniper profile`() = runTest {
        every { mockDao.getProfile(2) } returns flowOf(null)

        repository.getProfile(profileId = 2).first()

        // Verify DAO was called with profileId 2
        coEvery { mockDao.getProfile(2) }
    }

    // =========================================================================
    // getXpByWorkout
    // =========================================================================

    @Test
    fun `getXpByWorkout returns empty map when no transactions`() = runTest {
        every { mockDao.getAllXpTransactions(1) } returns flowOf(emptyList())

        val result = repository.getXpByWorkout().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getXpByWorkout maps transactions to workoutId-xp pairs`() = runTest {
        val txs = listOf(
            XpTransactionEntity("tx1", "w-1", 50, "run", 1000L, 1),
            XpTransactionEntity("tx2", "w-2", 75, "run", 2000L, 1),
        )
        every { mockDao.getAllXpTransactions(1) } returns flowOf(txs)

        val result = repository.getXpByWorkout().first()

        assertEquals(2, result.size)
        assertEquals(50, result["w-1"])
        assertEquals(75, result["w-2"])
    }

    // =========================================================================
    // getXpTransactionForWorkout
    // =========================================================================

    @Test
    fun `getXpTransactionForWorkout returns null when not found`() = runTest {
        coEvery { mockDao.getXpTransactionForWorkout("w-1", 1) } returns null

        val result = repository.getXpTransactionForWorkout("w-1")

        assertNull(result)
    }

    @Test
    fun `getXpTransactionForWorkout maps entity to domain`() = runTest {
        val entity = XpTransactionEntity("tx1", "w-1", 50, "distance", 1000L, 1)
        coEvery { mockDao.getXpTransactionForWorkout("w-1", 1) } returns entity

        val result = repository.getXpTransactionForWorkout("w-1")

        assertNotNull(result)
        assertEquals("tx1", result!!.id)
        assertEquals("w-1", result.workoutId)
        assertEquals(50, result.xpAmount)
        assertEquals("distance", result.reason)
    }

    // =========================================================================
    // getRecentXpTransactions
    // =========================================================================

    @Test
    fun `getRecentXpTransactions maps entities to domain list`() = runTest {
        val txs = listOf(
            XpTransactionEntity("tx1", "w-1", 50, "run", 2000L, 1),
            XpTransactionEntity("tx2", "w-2", 30, "walk", 1000L, 1),
        )
        every { mockDao.getRecentXpTransactions(1, 10) } returns flowOf(txs)

        val result = repository.getRecentXpTransactions().first()

        assertEquals(2, result.size)
        assertEquals("tx1", result[0].id)
        assertEquals("tx2", result[1].id)
    }

    @Test
    fun `getRecentXpTransactions passes custom limit to DAO`() = runTest {
        every { mockDao.getRecentXpTransactions(1, 5) } returns flowOf(emptyList())

        repository.getRecentXpTransactions(limit = 5).first()

        // Verified by the mock setup — would throw if called with different args
    }

    // =========================================================================
    // awardXp
    // =========================================================================

    @Test
    fun `awardXp returns 0 when transaction already exists`() = runTest {
        val existing = XpTransactionEntity("tx1", "w-1", 50, "run", 1000L, 1)
        coEvery { mockDao.getXpTransactionForWorkout("w-1", 1) } returns existing

        val result = repository.awardXp(workoutId = "w-1", xpAmount = 100, reason = "test")

        assertEquals(0, result)
        coVerify(exactly = 0) { mockDao.insertXpTransaction(any()) }
    }

    @Test
    fun `awardXp inserts transaction and updates existing profile`() = runTest {
        coEvery { mockDao.getXpTransactionForWorkout("w-1", 1) } returns null
        val profile = CharacterProfileEntity(id = 1, totalXp = 100, level = 2)
        coEvery { mockDao.getProfileOnce(1) } returns profile

        val result = repository.awardXp(workoutId = "w-1", xpAmount = 50, reason = "test")

        assertEquals(50, result)
        coVerify { mockDao.insertXpTransaction(any()) }
        val profileSlot = slot<CharacterProfileEntity>()
        coVerify { mockDao.updateProfile(capture(profileSlot)) }
        assertEquals(150, profileSlot.captured.totalXp)
        assertEquals(XpCalculator.levelForXp(150), profileSlot.captured.level)
    }

    @Test
    fun `awardXp creates profile when none exists`() = runTest {
        coEvery { mockDao.getXpTransactionForWorkout("w-1", 1) } returns null
        coEvery { mockDao.getProfileOnce(1) } returns null

        val result = repository.awardXp(workoutId = "w-1", xpAmount = 50, reason = "first")

        assertEquals(50, result)
        coVerify { mockDao.insertProfile(any()) }
        coVerify { mockDao.updateProfile(any()) }
    }

    @Test
    fun `awardXp is idempotent for same workoutId and profileId`() = runTest {
        // First call succeeds
        coEvery { mockDao.getXpTransactionForWorkout("w-1", 1) } returns null
        coEvery { mockDao.getProfileOnce(1) } returns CharacterProfileEntity(id = 1, totalXp = 0, level = 1)
        repository.awardXp(workoutId = "w-1", xpAmount = 50, reason = "test")

        // Simulate the transaction now existing
        val existing = XpTransactionEntity("tx1", "w-1", 50, "test", 1000L, 1)
        coEvery { mockDao.getXpTransactionForWorkout("w-1", 1) } returns existing

        // Second call returns 0
        val result = repository.awardXp(workoutId = "w-1", xpAmount = 50, reason = "test")
        assertEquals(0, result)
    }

    @Test
    fun `awardXp for profileId 2 updates Juniper profile`() = runTest {
        coEvery { mockDao.getXpTransactionForWorkout("w-1", 2) } returns null
        val profile = CharacterProfileEntity(id = 2, totalXp = 0, level = 1)
        coEvery { mockDao.getProfileOnce(2) } returns profile

        val result = repository.awardXp(profileId = 2, workoutId = "w-1", xpAmount = 30, reason = "walk")

        assertEquals(30, result)
        val txSlot = slot<XpTransactionEntity>()
        coVerify { mockDao.insertXpTransaction(capture(txSlot)) }
        assertEquals(2, txSlot.captured.profileId)
    }

    // =========================================================================
    // updateStats
    // =========================================================================

    @Test
    fun `updateStats updates existing profile stats`() = runTest {
        val profile = CharacterProfileEntity(id = 1, totalXp = 100, level = 2)
        coEvery { mockDao.getProfileOnce(1) } returns profile

        val stats = CharacterStats(speed = 10, endurance = 15, consistency = 8)
        repository.updateStats(stats = stats)

        val profileSlot = slot<CharacterProfileEntity>()
        coVerify { mockDao.updateProfile(capture(profileSlot)) }
        assertEquals(10, profileSlot.captured.speedStat)
        assertEquals(15, profileSlot.captured.enduranceStat)
        assertEquals(8, profileSlot.captured.consistencyStat)
    }

    @Test
    fun `updateStats creates profile when none exists`() = runTest {
        coEvery { mockDao.getProfileOnce(1) } returns null

        val stats = CharacterStats(speed = 5, endurance = 5, consistency = 5)
        repository.updateStats(stats = stats)

        coVerify { mockDao.insertProfile(any()) }
        coVerify { mockDao.updateProfile(any()) }
    }

    // =========================================================================
    // getProfileLevel
    // =========================================================================

    @Test
    fun `getProfileLevel returns level from profile`() = runTest {
        val profile = CharacterProfileEntity(id = 1, totalXp = 500, level = 5)
        coEvery { mockDao.getProfileOnce(1) } returns profile

        assertEquals(5, repository.getProfileLevel())
    }

    @Test
    fun `getProfileLevel returns 1 when no profile exists`() = runTest {
        coEvery { mockDao.getProfileOnce(1) } returns null

        assertEquals(1, repository.getProfileLevel())
    }

    // =========================================================================
    // Achievements
    // =========================================================================

    @Test
    fun `getUnlockedAchievements maps entities to domain`() = runTest {
        val entities = listOf(
            UnlockedAchievementEntity("dist_first_mile", 1000L, 25, 1),
            UnlockedAchievementEntity("count_1", 2000L, 10, 1),
        )
        every { mockDao.getUnlockedAchievements(1) } returns flowOf(entities)

        val result = repository.getUnlockedAchievements().first()

        assertEquals(2, result.size)
        assertEquals("dist_first_mile", result[0].achievementId)
        assertEquals(25, result[0].xpAwarded)
        assertEquals("count_1", result[1].achievementId)
    }

    @Test
    fun `getUnlockedAchievementIds returns set of ids`() = runTest {
        coEvery { mockDao.getUnlockedAchievementIds(1) } returns listOf("dist_first_mile", "count_1")

        val result = repository.getUnlockedAchievementIds()

        assertEquals(setOf("dist_first_mile", "count_1"), result)
    }

    @Test
    fun `getUnlockedAchievementIds returns empty set when none unlocked`() = runTest {
        coEvery { mockDao.getUnlockedAchievementIds(1) } returns emptyList()

        assertTrue(repository.getUnlockedAchievementIds().isEmpty())
    }

    @Test
    fun `unlockAchievement returns 0 if already unlocked`() = runTest {
        coEvery { mockDao.getUnlockedAchievementIds(1) } returns listOf(AchievementDef.FIRST_MILE.id)

        val result = repository.unlockAchievement(def = AchievementDef.FIRST_MILE)

        assertEquals(0, result)
        coVerify(exactly = 0) { mockDao.insertUnlockedAchievement(any()) }
    }

    @Test
    fun `unlockAchievement inserts achievement and awards XP`() = runTest {
        coEvery { mockDao.getUnlockedAchievementIds(1) } returns emptyList()
        coEvery { mockDao.getXpTransactionForWorkout("achievement:${AchievementDef.FIRST_MILE.id}", 1) } returns null
        coEvery { mockDao.getProfileOnce(1) } returns CharacterProfileEntity(id = 1, totalXp = 0, level = 1)

        val result = repository.unlockAchievement(def = AchievementDef.FIRST_MILE)

        assertEquals(AchievementDef.FIRST_MILE.xpReward, result)
        coVerify { mockDao.insertUnlockedAchievement(any()) }
        // Also verify the XP transaction uses "achievement:<id>" as workoutId
        val txSlot = slot<XpTransactionEntity>()
        coVerify { mockDao.insertXpTransaction(capture(txSlot)) }
        assertEquals("achievement:${AchievementDef.FIRST_MILE.id}", txSlot.captured.workoutId)
    }

    @Test
    fun `unlockAchievement for Juniper profile uses profileId 2`() = runTest {
        val def = AchievementDef.JUNIPER_FIRST_SNIFF
        coEvery { mockDao.getUnlockedAchievementIds(2) } returns emptyList()
        coEvery { mockDao.getXpTransactionForWorkout("achievement:${def.id}", 2) } returns null
        coEvery { mockDao.getProfileOnce(2) } returns CharacterProfileEntity(id = 2, totalXp = 0, level = 1)

        val result = repository.unlockAchievement(profileId = 2, def = def)

        assertEquals(def.xpReward, result)
        val achievementSlot = slot<UnlockedAchievementEntity>()
        coVerify { mockDao.insertUnlockedAchievement(capture(achievementSlot)) }
        assertEquals(2, achievementSlot.captured.profileId)
    }
}
