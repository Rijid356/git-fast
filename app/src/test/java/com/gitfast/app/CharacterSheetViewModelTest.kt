package com.gitfast.app

import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.UnlockedAchievement
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.ui.character.CharacterSheetViewModel
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
import org.junit.Before
import org.junit.Test
import java.time.Instant

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
        every { workoutRepo.getCompletedWorkoutsByType(ActivityType.DOG_WALK) } returns flowOf(emptyList())

        return repo to workoutRepo
    }

    @Test
    fun `profile emits default when repository returns default`() = runTest {
        val (repo, workoutRepo) = mockRepos()
        val viewModel = CharacterSheetViewModel(repo, workoutRepo)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.profile.collect {}
        }

        assertEquals(CharacterProfile(), viewModel.profile.value)
    }

    @Test
    fun `profile emits data from repository`() = runTest {
        val profile = CharacterProfile(level = 5, totalXp = 450, xpProgress = 0.5f)
        val (repo, workoutRepo) = mockRepos(profile = profile)
        val viewModel = CharacterSheetViewModel(repo, workoutRepo)

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
        val viewModel = CharacterSheetViewModel(repo, workoutRepo)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.recentXpTransactions.collect {}
        }

        assertEquals(2, viewModel.recentXpTransactions.value.size)
        assertEquals(50, viewModel.recentXpTransactions.value[0].xpAmount)
    }

    @Test
    fun `recentXpTransactions emits empty list when no transactions`() = runTest {
        val (repo, workoutRepo) = mockRepos()
        val viewModel = CharacterSheetViewModel(repo, workoutRepo)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.recentXpTransactions.collect {}
        }

        assertEquals(emptyList<XpTransaction>(), viewModel.recentXpTransactions.value)
    }
}
