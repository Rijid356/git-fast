package com.gitfast.app.screenshots.screens

import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.data.model.SorenessLog
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.SorenessRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.soreness.SorenessLogScreen
import com.gitfast.app.ui.soreness.SorenessLogViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class SorenessScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen Soreness Empty`() {
        val viewModel = createMockViewModel(todayLog = null)
        captureScreenshot("Screen_Soreness_Empty", category = "soreness") {
            SorenessLogScreen(onBackClick = {}, viewModel = viewModel)
        }
    }

    @Test
    fun `Screen Soreness Logged`() {
        val log = SorenessLog(
            id = "log-1",
            date = LocalDate.now(),
            muscleIntensities = mapOf(
                MuscleGroup.QUADS to SorenessIntensity.MODERATE,
                MuscleGroup.HAMSTRINGS to SorenessIntensity.MODERATE,
                MuscleGroup.CALVES to SorenessIntensity.SEVERE,
            ),
            notes = "Leg day aftermath",
            xpAwarded = 8,
        )
        val viewModel = createMockViewModel(todayLog = log)
        captureScreenshot("Screen_Soreness_Logged", category = "soreness") {
            SorenessLogScreen(onBackClick = {}, viewModel = viewModel)
        }
    }

    private fun createMockViewModel(todayLog: SorenessLog?): SorenessLogViewModel {
        val sorenessRepo = mockk<SorenessRepository>(relaxed = true) {
            every { observeTodayLog() } returns flowOf(todayLog)
            coEvery { getTodayLog() } returns todayLog
        }
        val characterRepo = mockk<CharacterRepository>(relaxed = true)
        val workoutRepo = mockk<WorkoutRepository>(relaxed = true) {
            every { getCompletedWorkouts() } returns flowOf(emptyList())
        }
        return SorenessLogViewModel(sorenessRepo, characterRepo, workoutRepo)
    }
}
