package com.gitfast.app.screenshots.screens

import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.workout.WorkoutSummaryScreen
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WorkoutSummaryScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen WorkoutSummary`() {
        captureScreenshot("Screen_WorkoutSummary") {
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
}
