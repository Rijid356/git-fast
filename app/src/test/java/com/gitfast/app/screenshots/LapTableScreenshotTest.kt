package com.gitfast.app.screenshots

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.gitfast.app.ui.detail.LapDisplayItem
import com.gitfast.app.ui.detail.LapTable

@RunWith(RobolectricTestRunner::class)
class LapTableScreenshotTest : ScreenshotTestBase() {

    @Test
    fun `LapTable with markers`() {
        captureScreenshot("LapTable_with_markers") {
            LapTable(
                laps = listOf(
                    LapDisplayItem(
                        id = "1",
                        lapNumber = 1,
                        timeFormatted = "2:15",
                        distanceFormatted = "0.25 mi",
                        paceFormatted = "9:00 /mi",
                        deltaFormatted = null,
                        deltaSeconds = null,
                        isFastest = false,
                        isSlowest = false,
                    ),
                    LapDisplayItem(
                        id = "2",
                        lapNumber = 2,
                        timeFormatted = "2:02",
                        distanceFormatted = "0.25 mi",
                        paceFormatted = "8:08 /mi",
                        deltaFormatted = "▼ -13s",
                        deltaSeconds = -13,
                        isFastest = true,
                        isSlowest = false,
                    ),
                    LapDisplayItem(
                        id = "3",
                        lapNumber = 3,
                        timeFormatted = "2:22",
                        distanceFormatted = "0.25 mi",
                        paceFormatted = "9:28 /mi",
                        deltaFormatted = "▲ +20s",
                        deltaSeconds = 20,
                        isFastest = false,
                        isSlowest = true,
                    ),
                ),
            )
        }
    }

    @Test
    fun `LapTable empty`() {
        captureScreenshot("LapTable_empty") {
            LapTable(laps = emptyList())
        }
    }

    @Test
    fun `LapTable single lap`() {
        captureScreenshot("LapTable_single") {
            LapTable(
                laps = listOf(
                    LapDisplayItem(
                        id = "1",
                        lapNumber = 1,
                        timeFormatted = "2:15",
                        distanceFormatted = "0.25 mi",
                        paceFormatted = "9:00 /mi",
                        deltaFormatted = null,
                        deltaSeconds = null,
                        isFastest = true,
                        isSlowest = true,
                    ),
                ),
            )
        }
    }
}
