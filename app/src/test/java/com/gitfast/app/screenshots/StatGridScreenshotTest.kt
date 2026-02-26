package com.gitfast.app.screenshots

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.gitfast.app.ui.workout.StatGrid

@RunWith(RobolectricTestRunner::class)
class StatGridScreenshotTest : ScreenshotTestBase() {

    @Test
    fun `StatGrid with active data`() {
        captureScreenshot("StatGrid_active", category = "stats") {
            StatGrid(
                elapsedTimeFormatted = "12:34",
                distanceFormatted = "1.52 mi",
                averagePaceFormatted = "8:14 /mi",
                stepCount = 2847,
            )
        }
    }

    @Test
    fun `StatGrid empty state`() {
        captureScreenshot("StatGrid_empty", category = "stats") {
            StatGrid(
                elapsedTimeFormatted = "00:00",
                distanceFormatted = "0.00 mi",
                averagePaceFormatted = null,
                stepCount = 0,
            )
        }
    }
}
