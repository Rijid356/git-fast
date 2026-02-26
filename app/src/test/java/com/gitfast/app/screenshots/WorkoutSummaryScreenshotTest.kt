package com.gitfast.app.screenshots

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.gitfast.app.ui.workout.WorkoutSummaryScreen

@RunWith(RobolectricTestRunner::class)
class WorkoutSummaryScreenshotTest : ScreenshotTestBase() {

    @Test
    fun `WorkoutSummary full stats`() {
        captureScreenshot("WorkoutSummary_full", category = "workout") {
            WorkoutSummaryScreen(
                time = "25:30",
                distance = "3.12 mi",
                pace = "8:11 /mi",
                steps = "5,432",
                lapCount = 5,
                bestLapTime = "4:52",
                bestLapNumber = 3,
                trendLabel = "Getting Faster",
                xpEarned = 150,
                achievements = listOf("First 5K", "Speed Demon"),
                streakDays = 3,
                onViewDetails = {},
                onDone = {},
            )
        }
    }

    @Test
    fun `WorkoutSummary minimal`() {
        captureScreenshot("WorkoutSummary_minimal", category = "workout") {
            WorkoutSummaryScreen(
                time = "5:00",
                distance = "0.45 mi",
                pace = "11:07 /mi",
                steps = "890",
                onViewDetails = {},
                onDone = {},
            )
        }
    }
}
