package com.gitfast.app.screenshots

import com.gitfast.app.data.model.DailyActivityMetrics
import com.gitfast.app.ui.components.ActivityRings
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActivityRingsScreenshotTest : ScreenshotTestBase() {

    @Test
    fun `ActivityRings zero progress`() {
        captureScreenshot("ActivityRings_zero", category = "components") {
            ActivityRings(
                metrics = DailyActivityMetrics(
                    activeMinutes = 0,
                    activeMinutesGoal = 30,
                    distanceMiles = 0.0,
                    distanceGoal = 2.0,
                    activeDaysThisWeek = 0,
                    activeDaysGoal = 5,
                ),
            )
        }
    }

    @Test
    fun `ActivityRings partial progress`() {
        captureScreenshot("ActivityRings_partial", category = "components") {
            ActivityRings(
                metrics = DailyActivityMetrics(
                    activeMinutes = 15,
                    activeMinutesGoal = 30,
                    distanceMiles = 1.0,
                    distanceGoal = 2.0,
                    activeDaysThisWeek = 2,
                    activeDaysGoal = 5,
                ),
            )
        }
    }

    @Test
    fun `ActivityRings full progress`() {
        captureScreenshot("ActivityRings_full", category = "components") {
            ActivityRings(
                metrics = DailyActivityMetrics(
                    activeMinutes = 30,
                    activeMinutesGoal = 30,
                    distanceMiles = 2.0,
                    distanceGoal = 2.0,
                    activeDaysThisWeek = 5,
                    activeDaysGoal = 5,
                ),
            )
        }
    }

    @Test
    fun `ActivityRings overflow`() {
        captureScreenshot("ActivityRings_overflow", category = "components") {
            ActivityRings(
                metrics = DailyActivityMetrics(
                    activeMinutes = 55,
                    activeMinutesGoal = 30,
                    distanceMiles = 3.5,
                    distanceGoal = 2.0,
                    activeDaysThisWeek = 7,
                    activeDaysGoal = 5,
                ),
            )
        }
    }

    @Test
    fun `ActivityRings mixed progress`() {
        captureScreenshot("ActivityRings_mixed", category = "components") {
            ActivityRings(
                metrics = DailyActivityMetrics(
                    activeMinutes = 9,
                    activeMinutesGoal = 30,
                    distanceMiles = 1.6,
                    distanceGoal = 2.0,
                    activeDaysThisWeek = 6,
                    activeDaysGoal = 5,
                ),
            )
        }
    }
}
