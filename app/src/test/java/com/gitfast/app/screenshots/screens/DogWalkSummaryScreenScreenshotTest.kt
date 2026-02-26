package com.gitfast.app.screenshots.screens

import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.dogwalk.DogWalkSummaryScreen
import com.gitfast.app.ui.dogwalk.DogWalkSummaryUiState
import com.gitfast.app.ui.dogwalk.DogWalkSummaryViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DogWalkSummaryScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen DogWalkSummary`() {
        val viewModel = mockk<DogWalkSummaryViewModel>(relaxed = true) {
            every { uiState } returns MutableStateFlow(
                DogWalkSummaryUiState(
                    timeFormatted = "22:45",
                    distanceFormatted = "1.05 mi",
                    paceFormatted = "21:40 /mi",
                    routeTags = listOf("Park Loop", "Neighborhood", "River Trail"),
                    selectedRouteTag = "Park Loop",
                    weatherCondition = null,
                    weatherTemp = null,
                    energyLevel = null,
                    notes = "",
                ),
            )
            every { workoutId } returns "walk-001"
        }

        captureScreenshot("Screen_DogWalkSummary", category = "summary") {
            DogWalkSummaryScreen(
                onSaved = {},
                onDiscarded = {},
                viewModel = viewModel,
            )
        }
    }
}
