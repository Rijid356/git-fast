package com.gitfast.app.screenshots.screens

import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.analytics.routeperformance.PerformanceRow
import com.gitfast.app.ui.analytics.routeperformance.RoutePerformanceScreen
import com.gitfast.app.ui.analytics.routeperformance.RoutePerformanceUiState
import com.gitfast.app.ui.analytics.routeperformance.RoutePerformanceViewModel
import com.gitfast.app.ui.analytics.routeperformance.TrendSummary
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoutePerformanceScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen RoutePerformance`() {
        val viewModel = mockk<RoutePerformanceViewModel>(relaxed = true) {
            every { uiState } returns MutableStateFlow(
                RoutePerformanceUiState(
                    routeTags = listOf("Park Loop", "River Trail", "Neighborhood"),
                    selectedTag = "Park Loop",
                    sessionCount = 8,
                    personalBest = PerformanceRow(
                        workoutId = "r3",
                        date = "Feb 10",
                        durationFormatted = "19:42",
                        distanceFormatted = "2.51 mi",
                        deltaFormatted = null,
                        deltaMillis = null,
                        isMostRecent = false,
                        isPersonalBest = true,
                    ),
                    trendSummary = TrendSummary(
                        deltaFormatted = "-1:15 faster",
                        isImproving = true,
                        isConsistent = false,
                    ),
                    rows = listOf(
                        PerformanceRow(
                            workoutId = "r1",
                            date = "Feb 24",
                            durationFormatted = "20:15",
                            distanceFormatted = "2.51 mi",
                            deltaFormatted = "+0:33",
                            deltaMillis = 33000,
                            isMostRecent = true,
                            isPersonalBest = false,
                        ),
                        PerformanceRow(
                            workoutId = "r2",
                            date = "Feb 18",
                            durationFormatted = "20:57",
                            distanceFormatted = "2.51 mi",
                            deltaFormatted = "+1:15",
                            deltaMillis = 75000,
                            isMostRecent = false,
                            isPersonalBest = false,
                        ),
                        PerformanceRow(
                            workoutId = "r3",
                            date = "Feb 10",
                            durationFormatted = "19:42",
                            distanceFormatted = "2.51 mi",
                            deltaFormatted = null,
                            deltaMillis = null,
                            isMostRecent = false,
                            isPersonalBest = true,
                        ),
                    ),
                    isLoading = false,
                ),
            )
        }

        captureScreenshot("Screen_RoutePerformance", category = "analytics") {
            RoutePerformanceScreen(
                onBackClick = {},
                onWorkoutClick = {},
                viewModel = viewModel,
            )
        }
    }
}
