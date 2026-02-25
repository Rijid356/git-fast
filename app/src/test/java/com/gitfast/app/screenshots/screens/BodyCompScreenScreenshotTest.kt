package com.gitfast.app.screenshots.screens

import com.gitfast.app.data.model.BodyCompReading
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.analytics.bodycomp.BodyCompChartBar
import com.gitfast.app.ui.analytics.bodycomp.BodyCompScreen
import com.gitfast.app.ui.analytics.bodycomp.BodyCompUiState
import com.gitfast.app.ui.analytics.bodycomp.BodyCompViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class BodyCompScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen BodyComp`() {
        val viewModel = mockk<BodyCompViewModel>(relaxed = true) {
            every { uiState } returns MutableStateFlow(
                BodyCompUiState(
                    isLoading = false,
                    isEmpty = false,
                    latestReading = BodyCompReading(
                        id = "bc1",
                        timestamp = Instant.parse("2026-02-24T08:00:00Z"),
                        weightKg = 79.4,
                        weightLbs = 175.0,
                        bodyFatPercent = 18.5,
                        leanBodyMassKg = 64.7,
                        leanBodyMassLbs = 142.6,
                        boneMassKg = 3.2,
                        boneMassLbs = 7.1,
                        bmrKcalPerDay = 1820.0,
                        heightMeters = 1.78,
                        bmi = 25.1,
                        source = "Health Connect",
                    ),
                    latestDateFormatted = "Feb 24, 2026",
                    totalWeighIns = 24,
                    avgWeightLbs = "176.2",
                    weightDelta = "-1.8 lbs",
                    weightDeltaPositive = false,
                    minWeightLbs = "173.5",
                    maxWeightLbs = "179.0",
                    weighInStreak = 5,
                    fatMassLbs = "32.4",
                    leanMassLbs = "142.6",
                    boneMassLbs = "7.1",
                    weightBars = listOf(
                        BodyCompChartBar("W1", 178.2f, "178.2", false),
                        BodyCompChartBar("W2", 177.5f, "177.5", false),
                        BodyCompChartBar("W3", 176.8f, "176.8", false),
                        BodyCompChartBar("W4", 175.0f, "175.0", true),
                    ),
                    bodyFatBars = listOf(
                        BodyCompChartBar("W1", 19.2f, "19.2%", false),
                        BodyCompChartBar("W2", 19.0f, "19.0%", false),
                        BodyCompChartBar("W3", 18.8f, "18.8%", false),
                        BodyCompChartBar("W4", 18.5f, "18.5%", true),
                    ),
                ),
            )
        }

        captureScreenshot("Screen_BodyComp") {
            BodyCompScreen(
                onBackClick = {},
                viewModel = viewModel,
            )
        }
    }
}
