package com.gitfast.app.screenshots.screens

import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.analytics.trends.ChartBar
import com.gitfast.app.ui.analytics.trends.ComparisonDisplay
import com.gitfast.app.ui.analytics.trends.TrendPeriod
import com.gitfast.app.ui.analytics.trends.TrendsScreen
import com.gitfast.app.ui.analytics.trends.TrendsUiState
import com.gitfast.app.ui.analytics.trends.TrendsViewModel
import com.gitfast.app.ui.analytics.trends.ActivityFilter
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrendsScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen Trends`() {
        val viewModel = mockk<TrendsViewModel>(relaxed = true) {
            every { uiState } returns MutableStateFlow(
                TrendsUiState(
                    period = TrendPeriod.WEEK,
                    filter = ActivityFilter.ALL,
                    isLoading = false,
                    isEmpty = false,
                    comparison = ComparisonDisplay(
                        currentDistance = "12.4 mi",
                        previousDistance = "10.8 mi",
                        distanceDelta = "+1.6 mi",
                        distanceDeltaPositive = true,
                        currentWorkouts = "5",
                        previousWorkouts = "4",
                        workoutsDelta = "+1",
                        workoutsDeltaPositive = true,
                        currentDuration = "1h 42m",
                        previousDuration = "1h 28m",
                        durationDelta = "+14m",
                        durationDeltaPositive = true,
                        currentPace = "8:14 /mi",
                        previousPace = "8:32 /mi",
                        paceDelta = "-18s",
                        paceDeltaPositive = true,
                    ),
                    distanceBars = listOf(
                        ChartBar("Mon", 2.1f, "2.1", false),
                        ChartBar("Tue", 0f, "0.0", false),
                        ChartBar("Wed", 3.2f, "3.2", false),
                        ChartBar("Thu", 1.0f, "1.0", false),
                        ChartBar("Fri", 2.8f, "2.8", false),
                        ChartBar("Sat", 0f, "0.0", false),
                        ChartBar("Sun", 3.3f, "3.3", true),
                    ),
                    workoutBars = listOf(
                        ChartBar("Mon", 1f, "1", false),
                        ChartBar("Tue", 0f, "0", false),
                        ChartBar("Wed", 1f, "1", false),
                        ChartBar("Thu", 1f, "1", false),
                        ChartBar("Fri", 1f, "1", false),
                        ChartBar("Sat", 0f, "0", false),
                        ChartBar("Sun", 1f, "1", true),
                    ),
                ),
            )
        }

        captureScreenshot("Screen_Trends", category = "analytics") {
            TrendsScreen(
                onBackClick = {},
                viewModel = viewModel,
            )
        }
    }
}
