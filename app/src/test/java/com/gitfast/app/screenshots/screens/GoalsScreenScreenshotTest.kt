package com.gitfast.app.screenshots.screens

import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.goals.GoalsSettingsScreen
import com.gitfast.app.ui.goals.GoalsSettingsViewModel
import com.gitfast.app.ui.goals.GoalsUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GoalsScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen Goals`() {
        val viewModel = mockk<GoalsSettingsViewModel>(relaxed = true) {
            every { uiState } returns MutableStateFlow(
                GoalsUiState(
                    dailyActiveMinutesGoal = 22,
                    dailyDistanceGoalMiles = 1.5,
                    weeklyActiveDaysGoal = 5,
                ),
            )
        }

        captureScreenshot("Screen_Goals", category = "settings") {
            GoalsSettingsScreen(
                onBackClick = {},
                viewModel = viewModel,
            )
        }
    }
}
