package com.gitfast.app.screenshots

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.gitfast.app.ui.workout.PaceDisplay

@RunWith(RobolectricTestRunner::class)
class PaceDisplayScreenshotTest : ScreenshotTestBase() {

    @Test
    fun `PaceDisplay with pace`() {
        captureScreenshot("PaceDisplay_with_pace", category = "pace") {
            PaceDisplay(currentPaceFormatted = "7:45 /mi")
        }
    }

    @Test
    fun `PaceDisplay no pace`() {
        captureScreenshot("PaceDisplay_no_pace", category = "pace") {
            PaceDisplay(currentPaceFormatted = null)
        }
    }
}
