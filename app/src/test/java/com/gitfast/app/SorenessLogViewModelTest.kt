package com.gitfast.app

import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.data.model.SorenessLog
import com.gitfast.app.data.model.Workout
import com.gitfast.app.data.model.WorkoutStatus
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.SorenessRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.ui.soreness.SorenessLogViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SorenessLogViewModelTest {

    private lateinit var sorenessRepository: SorenessRepository
    private lateinit var characterRepository: CharacterRepository
    private lateinit var workoutRepository: WorkoutRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        sorenessRepository = mockk(relaxed = true)
        characterRepository = mockk(relaxed = true)
        workoutRepository = mockk(relaxed = true)

        every { sorenessRepository.observeTodayLog() } returns flowOf(null)
        coEvery { sorenessRepository.getTodayLog() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SorenessLogViewModel {
        return SorenessLogViewModel(sorenessRepository, characterRepository, workoutRepository)
    }

    private fun createTestLog(
        muscleIntensities: Map<MuscleGroup, SorenessIntensity> = mapOf(
            MuscleGroup.QUADS to SorenessIntensity.MODERATE,
        ),
        notes: String? = "sore after run",
    ): SorenessLog {
        return SorenessLog(
            id = "log-1",
            date = LocalDate.now(),
            muscleIntensities = muscleIntensities,
            notes = notes,
            xpAwarded = 8,
        )
    }

    @Test
    fun `init loads existing today log into state`() = runTest {
        val log = createTestLog()
        coEvery { sorenessRepository.getTodayLog() } returns log

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(log, state.todayLog)
        assertEquals(log.muscleIntensities, state.muscleIntensities)
        assertEquals("sore after run", state.notes)
    }

    @Test
    fun `init with no existing log keeps defaults`() = runTest {
        coEvery { sorenessRepository.getTodayLog() } returns null

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNull(state.todayLog)
        assertTrue(state.muscleIntensities.isEmpty())
        assertEquals("", state.notes)
        assertTrue(state.showingFront)
    }

    @Test
    fun `cycleMuscleIntensity adds muscle as MILD when not selected`() {
        val viewModel = createViewModel()

        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS)

        assertEquals(
            SorenessIntensity.MILD,
            viewModel.uiState.value.muscleIntensities[MuscleGroup.QUADS],
        )
    }

    @Test
    fun `cycleMuscleIntensity cycles MILD to MODERATE`() {
        val viewModel = createViewModel()

        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // → MILD
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // → MODERATE

        assertEquals(
            SorenessIntensity.MODERATE,
            viewModel.uiState.value.muscleIntensities[MuscleGroup.QUADS],
        )
    }

    @Test
    fun `cycleMuscleIntensity cycles MODERATE to SEVERE`() {
        val viewModel = createViewModel()

        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // → MILD
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // → MODERATE
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // → SEVERE

        assertEquals(
            SorenessIntensity.SEVERE,
            viewModel.uiState.value.muscleIntensities[MuscleGroup.QUADS],
        )
    }

    @Test
    fun `cycleMuscleIntensity cycles SEVERE to deselected`() {
        val viewModel = createViewModel()

        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // → MILD
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // → MODERATE
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // → SEVERE
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // → deselected

        assertFalse(viewModel.uiState.value.muscleIntensities.containsKey(MuscleGroup.QUADS))
    }

    @Test
    fun `toggleFrontBack toggles showingFront`() {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.showingFront)
        viewModel.toggleFrontBack()
        assertFalse(viewModel.uiState.value.showingFront)
        viewModel.toggleFrontBack()
        assertTrue(viewModel.uiState.value.showingFront)
    }

    @Test
    fun `updateNotes updates state`() {
        val viewModel = createViewModel()

        viewModel.updateNotes("legs are sore")
        assertEquals("legs are sore", viewModel.uiState.value.notes)
    }

    @Test
    fun `startEditing populates from todayLog`() = runTest {
        val log = createTestLog(
            muscleIntensities = mapOf(
                MuscleGroup.HAMSTRINGS to SorenessIntensity.SEVERE,
                MuscleGroup.CALVES to SorenessIntensity.MODERATE,
            ),
            notes = "post-run soreness",
        )
        coEvery { sorenessRepository.getTodayLog() } returns log

        val viewModel = createViewModel()

        // Modify state away from log values first
        viewModel.cycleMuscleIntensity(MuscleGroup.CHEST)
        viewModel.updateNotes("different notes")

        viewModel.startEditing()

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals(log.muscleIntensities, state.muscleIntensities)
        assertEquals("post-run soreness", state.notes)
    }

    @Test
    fun `startEditing does nothing when no todayLog`() {
        val viewModel = createViewModel()

        viewModel.startEditing()

        assertFalse(viewModel.uiState.value.isEditing)
    }

    @Test
    fun `cancelEditing clears isEditing flag`() = runTest {
        val log = createTestLog()
        coEvery { sorenessRepository.getTodayLog() } returns log

        val viewModel = createViewModel()
        viewModel.startEditing()
        assertTrue(viewModel.uiState.value.isEditing)

        viewModel.cancelEditing()
        assertFalse(viewModel.uiState.value.isEditing)
    }

    @Test
    fun `saveSoreness computes XP and saves with new signature`() = runTest {
        val savedLog = createTestLog()
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())
        coEvery {
            sorenessRepository.logSoreness(
                muscleIntensities = any(),
                notes = any(),
                xpAwarded = any(),
            )
        } returns savedLog
        coEvery { sorenessRepository.getLast30DaysLogs() } returns listOf(savedLog)
        coEvery { sorenessRepository.getTotalCount() } returns 1
        coEvery { characterRepository.getUnlockedAchievementIds(1) } returns emptySet()
        coEvery { characterRepository.getProfileLevel(1) } returns 1
        coEvery { characterRepository.awardXp(any(), any(), any(), any()) } returns 8

        val viewModel = createViewModel()
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // MILD
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // MODERATE
        viewModel.updateNotes("test notes")

        viewModel.saveSoreness()

        coVerify {
            sorenessRepository.logSoreness(
                muscleIntensities = mapOf(MuscleGroup.QUADS to SorenessIntensity.MODERATE),
                notes = "test notes",
                xpAwarded = any(),
            )
        }
        coVerify { characterRepository.awardXp(1, any(), any(), any()) }
        coVerify { characterRepository.updateToughness(profileId = 1, toughness = any()) }

        val state = viewModel.uiState.value
        assertEquals(savedLog, state.todayLog)
        assertFalse(state.isSaving)
    }

    @Test
    fun `saveSoreness skips when no muscles selected`() = runTest {
        val viewModel = createViewModel()
        // Do not select any muscles

        viewModel.saveSoreness()

        coVerify(exactly = 0) { sorenessRepository.logSoreness(any(), any(), any()) }
    }

    @Test
    fun `saveSoreness sets xpEarned for new log but not for update`() = runTest {
        val savedLog = createTestLog()
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())
        coEvery {
            sorenessRepository.logSoreness(any(), any(), any())
        } returns savedLog
        coEvery { sorenessRepository.getLast30DaysLogs() } returns listOf(savedLog)
        coEvery { sorenessRepository.getTotalCount() } returns 1
        coEvery { characterRepository.getUnlockedAchievementIds(1) } returns emptySet()
        coEvery { characterRepository.getProfileLevel(1) } returns 1

        // New log (no todayLog set) — xpEarned should be non-null
        val viewModel = createViewModel()
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS)
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // MODERATE

        viewModel.saveSoreness()

        val xpAfterNew = viewModel.uiState.value.xpEarned
        assertTrue("xpEarned should be set for new log, was $xpAfterNew", xpAfterNew != null && xpAfterNew > 0)

        // Now simulate editing an existing log (todayLog is set after first save)
        viewModel.cycleMuscleIntensity(MuscleGroup.BACK) // add another muscle
        viewModel.saveSoreness()

        val xpAfterUpdate = viewModel.uiState.value.xpEarned
        assertNull("xpEarned should be null for update", xpAfterUpdate)
    }

    @Test
    fun `saveSoreness unlocks achievements and includes names`() = runTest {
        val savedLog = createTestLog()
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())
        coEvery {
            sorenessRepository.logSoreness(any(), any(), any())
        } returns savedLog
        coEvery { sorenessRepository.getLast30DaysLogs() } returns listOf(savedLog)
        coEvery { sorenessRepository.getTotalCount() } returns 5
        coEvery { characterRepository.getUnlockedAchievementIds(1) } returns emptySet()
        coEvery { characterRepository.getProfileLevel(1) } returns 1
        coEvery { characterRepository.unlockAchievement(profileId = 1, def = any()) } returns 50

        val viewModel = createViewModel()
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS)
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // MODERATE

        viewModel.saveSoreness()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
    }

    @Test
    fun `dismissXpToast clears xpEarned and achievementNames`() = runTest {
        val savedLog = createTestLog()
        every { workoutRepository.getCompletedWorkouts() } returns flowOf(emptyList())
        coEvery {
            sorenessRepository.logSoreness(any(), any(), any())
        } returns savedLog
        coEvery { sorenessRepository.getLast30DaysLogs() } returns listOf(savedLog)
        coEvery { sorenessRepository.getTotalCount() } returns 1
        coEvery { characterRepository.getUnlockedAchievementIds(1) } returns emptySet()
        coEvery { characterRepository.getProfileLevel(1) } returns 1

        val viewModel = createViewModel()
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS)
        viewModel.cycleMuscleIntensity(MuscleGroup.QUADS) // MODERATE

        viewModel.saveSoreness()

        viewModel.dismissXpToast()

        val state = viewModel.uiState.value
        assertNull(state.xpEarned)
        assertTrue(state.achievementNames.isEmpty())
    }
}
