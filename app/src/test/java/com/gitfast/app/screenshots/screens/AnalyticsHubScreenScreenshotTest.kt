package com.gitfast.app.screenshots.screens

import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.analytics.AnalyticsHubScreen
import com.gitfast.app.ui.analytics.AnalyticsHubViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AnalyticsHubScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen AnalyticsHub`() {
        val viewModel = mockk<AnalyticsHubViewModel>(relaxed = true) {
            every { stats } returns MutableStateFlow(
                AnalyticsHubViewModel.AnalyticsStats(
                    totalWorkouts = 87,
                    totalDistanceFormatted = "142.3 mi",
                    totalDurationFormatted = "19h 45m",
                    bestStreak = 12,
                ),
            )
        }

        captureScreenshot("Screen_AnalyticsHub") {
            AnalyticsHubScreen(
                onBackClick = {},
                onRouteMapClick = {},
                onRouteStatsClick = {},
                onRecordsClick = {},
                onTrendsClick = {},
                onBodyCompClick = {},
                viewModel = viewModel,
            )
        }
    }
}
