package com.gitfast.app

import com.gitfast.app.data.healthconnect.HealthConnectManager
import com.gitfast.app.data.model.BodyCompReading
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.UnlockedAchievement
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.repository.BodyCompRepository
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.ExerciseRepository
import com.gitfast.app.data.repository.SorenessRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.ui.character.CharacterSheetViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterSheetViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val bodyCompRepo = mockk<BodyCompRepository>()
    private val sorenessRepo = mockk<SorenessRepository>()
    private val exerciseRepo = mockk<ExerciseRepository>()
    private val healthConnectManager = mockk<HealthConnectManager>()

    private fun mockRepos(
        profile: CharacterProfile = CharacterProfile(),
        transactions: List<XpTransaction> = emptyList(),
    ): Pair<CharacterRepository, WorkoutRepository> {
        val repo = mockk<CharacterRepository>()
        every { repo.getProfile(1) } returns flowOf(profile)
        every { repo.getRecentXpTransactions(profileId = 1, limit = 20) } returns flowOf(transactions)
        every { repo.getUnlockedAchievements(1) } returns flowOf(emptyList())
        every { repo.getProfile(2) } returns flowOf(CharacterProfile())
        every { repo.getRecentXpTransactions(profileId = 2, limit = 20) } returns flowOf(emptyList())
        every { repo.getUnlockedAchievements(2) } returns flowOf(emptyList())

        val workoutRepo = mockk<WorkoutRepository>()
        every { workoutRepo.getCompletedWorkouts() } returns flowOf(emptyList())
        every { workoutRepo.getCompletedDogActivityWorkouts() } returns flowOf(emptyList())

        // Default: Health Connect not available
        every { healthConnectManager.isAvailable() } returns false
        coEvery { healthConnectManager.hasPermissions() } returns false

        // Default: no soreness logs
        coEvery { sorenessRepo.getLast30DaysLogs() } returns emptyList()

        // Default: no dog walk events
        coEvery { workoutRepo.getTotalDogWalkEventCount() } returns 0

        // Default: no exercise sets
        coEvery { exerciseRepo.getLast30DaysSetsWithReps() } returns emptyList()

        return repo to workoutRepo
    }

    @Test
    fun `profile emits default when repository returns default`() = runTest {
        val (repo, workoutRepo) = mockRepos()
        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.profile.collect {}
        }

        assertEquals(CharacterProfile(), viewModel.profile.value)
    }

    @Test
    fun `profile emits data from repository`() = runTest {
        val profile = CharacterProfile(level = 5, totalXp = 450, xpProgress = 0.5f)
        val (repo, workoutRepo) = mockRepos(profile = profile)
        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.profile.collect {}
        }

        assertEquals(5, viewModel.profile.value.level)
        assertEquals(450, viewModel.profile.value.totalXp)
    }

    @Test
    fun `recentXpTransactions emits transactions from repository`() = runTest {
        val transactions = listOf(
            XpTransaction("1", "w1", 50, "Run completed", Instant.ofEpochMilli(1000)),
            XpTransaction("2", "w2", 30, "Dog walk completed", Instant.ofEpochMilli(2000)),
        )
        val (repo, workoutRepo) = mockRepos(transactions = transactions)
        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.recentXpTransactions.collect {}
        }

        assertEquals(2, viewModel.recentXpTransactions.value.size)
        assertEquals(50, viewModel.recentXpTransactions.value[0].xpAmount)
    }

    @Test
    fun `recentXpTransactions emits empty list when no transactions`() = runTest {
        val (repo, workoutRepo) = mockRepos()
        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.recentXpTransactions.collect {}
        }

        assertEquals(emptyList<XpTransaction>(), viewModel.recentXpTransactions.value)
    }

    // --- selectTab ---

    @Test
    fun `selectTab updates selectedTab state`() = runTest {
        val (repo, workoutRepo) = mockRepos()
        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        assertEquals(0, viewModel.selectedTab.value)
        viewModel.selectTab(1)
        assertEquals(1, viewModel.selectedTab.value)
    }

    @Test
    fun `selectTab back to ME tab`() = runTest {
        val (repo, workoutRepo) = mockRepos()
        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        viewModel.selectTab(1)
        viewModel.selectTab(0)
        assertEquals(0, viewModel.selectedTab.value)
    }

    // --- vitalityState ---

    @Test
    fun `vitalityState defaults when Health Connect unavailable`() = runTest {
        val (repo, workoutRepo) = mockRepos()
        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        val state = viewModel.vitalityState.value
        assertFalse(state.healthConnectConnected)
        assertEquals(1, state.vitalityStat)
        assertNull(state.breakdown)
    }

    @Test
    fun `vitalityState loads when Health Connect connected with body fat readings`() = runTest {
        val (repo, workoutRepo) = mockRepos()

        // Override HC to connected
        every { healthConnectManager.isAvailable() } returns true
        coEvery { healthConnectManager.hasPermissions() } returns true

        // Mock body comp data
        coEvery { bodyCompRepo.getWeighInCount(30) } returns 10

        val now = Instant.now()
        val readings = listOf(
            BodyCompReading("r1", now.minus(20, ChronoUnit.DAYS), 80.0, 176.4, 25.0, null, null, null, null, null, null, null, "hc"),
            BodyCompReading("r2", now.minus(10, ChronoUnit.DAYS), 79.0, 174.2, 24.0, null, null, null, null, null, null, null, "hc"),
            BodyCompReading("r3", now.minus(1, ChronoUnit.DAYS), 78.0, 171.9, 23.0, null, null, null, null, null, null, null, "hc"),
        )
        every { bodyCompRepo.getReadingsInRange(any(), any()) } returns flowOf(readings)

        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        val state = viewModel.vitalityState.value
        assertTrue(state.healthConnectConnected)
        assertEquals(10, state.weighInCount30d)
        // Body fat trend: 23.0 - 25.0 = -2.0
        assertNotNull(state.bodyFatTrendPercent)
        assertEquals(-2.0, state.bodyFatTrendPercent!!, 0.01)
        assertTrue(state.vitalityStat > 1)
        assertNotNull(state.breakdown)
    }

    @Test
    fun `vitalityState handles connected but no body fat data`() = runTest {
        val (repo, workoutRepo) = mockRepos()

        every { healthConnectManager.isAvailable() } returns true
        coEvery { healthConnectManager.hasPermissions() } returns true
        coEvery { bodyCompRepo.getWeighInCount(30) } returns 5

        // Readings without body fat
        val now = Instant.now()
        val readings = listOf(
            BodyCompReading("r1", now.minus(10, ChronoUnit.DAYS), 80.0, 176.4, null, null, null, null, null, null, null, null, "hc"),
            BodyCompReading("r2", now.minus(1, ChronoUnit.DAYS), 79.0, 174.2, null, null, null, null, null, null, null, null, "hc"),
        )
        every { bodyCompRepo.getReadingsInRange(any(), any()) } returns flowOf(readings)

        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        val state = viewModel.vitalityState.value
        assertTrue(state.healthConnectConnected)
        assertEquals(5, state.weighInCount30d)
        assertNull(state.bodyFatTrendPercent)
    }

    @Test
    fun `vitalityState handles connected but single body fat reading`() = runTest {
        val (repo, workoutRepo) = mockRepos()

        every { healthConnectManager.isAvailable() } returns true
        coEvery { healthConnectManager.hasPermissions() } returns true
        coEvery { bodyCompRepo.getWeighInCount(30) } returns 1

        val now = Instant.now()
        val readings = listOf(
            BodyCompReading("r1", now.minus(5, ChronoUnit.DAYS), 80.0, 176.4, 25.0, null, null, null, null, null, null, null, "hc"),
        )
        every { bodyCompRepo.getReadingsInRange(any(), any()) } returns flowOf(readings)

        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        val state = viewModel.vitalityState.value
        assertTrue(state.healthConnectConnected)
        // Single reading — not enough for trend
        assertNull(state.bodyFatTrendPercent)
    }

    @Test
    fun `vitalityState when Health Connect available but no permissions`() = runTest {
        val (repo, workoutRepo) = mockRepos()

        every { healthConnectManager.isAvailable() } returns true
        coEvery { healthConnectManager.hasPermissions() } returns false

        val viewModel = CharacterSheetViewModel(repo, workoutRepo, bodyCompRepo, sorenessRepo, exerciseRepo, healthConnectManager)

        val state = viewModel.vitalityState.value
        assertFalse(state.healthConnectConnected)
        assertEquals(1, state.vitalityStat)
    }

    private fun assertFalse(value: Boolean) = org.junit.Assert.assertFalse(value)
}
