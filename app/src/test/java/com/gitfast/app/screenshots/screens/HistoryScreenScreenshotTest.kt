package com.gitfast.app.screenshots.screens

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.components.ActivityFilter
import com.gitfast.app.ui.history.HistoryScreen
import com.gitfast.app.ui.history.HistoryUiState
import com.gitfast.app.ui.history.HistoryViewModel
import com.gitfast.app.ui.history.WorkoutHistoryItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class HistoryScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen History`() {
        val viewModel = mockk<HistoryViewModel>(relaxed = true) {
            every { workouts } returns MutableStateFlow(
                HistoryUiState.Loaded(
                    groupedWorkouts = mapOf(
                        "February 2026" to listOf(
                            WorkoutHistoryItem(
                                workoutId = "r1",
                                startTime = Instant.parse("2026-02-24T07:30:00Z"),
                                dateFormatted = "Feb 24",
                                timeFormatted = "7:30 AM",
                                relativeDateFormatted = "Today",
                                distanceFormatted = "3.12 mi",
                                durationFormatted = "25:30",
                                avgPaceFormatted = "8:11 /mi",
                                activityType = ActivityType.RUN,
                                subtitle = null,
                                xpEarned = 150,
                            ),
                            WorkoutHistoryItem(
                                workoutId = "w1",
                                startTime = Instant.parse("2026-02-24T12:00:00Z"),
                                dateFormatted = "Feb 24",
                                timeFormatted = "12:00 PM",
                                relativeDateFormatted = "Today",
                                distanceFormatted = "1.05 mi",
                                durationFormatted = "22:45",
                                avgPaceFormatted = "21:40 /mi",
                                activityType = ActivityType.DOG_WALK,
                                subtitle = "Park Loop",
                                xpEarned = 80,
                            ),
                            WorkoutHistoryItem(
                                workoutId = "r2",
                                startTime = Instant.parse("2026-02-22T18:00:00Z"),
                                dateFormatted = "Feb 22",
                                timeFormatted = "6:00 PM",
                                relativeDateFormatted = "2 days ago",
                                distanceFormatted = "2.45 mi",
                                durationFormatted = "21:15",
                                avgPaceFormatted = "8:40 /mi",
                                activityType = ActivityType.RUN,
                                subtitle = null,
                                xpEarned = 120,
                            ),
                            WorkoutHistoryItem(
                                workoutId = "r3",
                                startTime = Instant.parse("2026-02-20T07:00:00Z"),
                                dateFormatted = "Feb 20",
                                timeFormatted = "7:00 AM",
                                relativeDateFormatted = "4 days ago",
                                distanceFormatted = "4.01 mi",
                                durationFormatted = "33:20",
                                avgPaceFormatted = "8:19 /mi",
                                activityType = ActivityType.RUN,
                                subtitle = null,
                                xpEarned = 200,
                            ),
                        ),
                        "January 2026" to listOf(
                            WorkoutHistoryItem(
                                workoutId = "r4",
                                startTime = Instant.parse("2026-01-30T17:30:00Z"),
                                dateFormatted = "Jan 30",
                                timeFormatted = "5:30 PM",
                                relativeDateFormatted = "Jan 30",
                                distanceFormatted = "2.88 mi",
                                durationFormatted = "24:10",
                                avgPaceFormatted = "8:24 /mi",
                                activityType = ActivityType.RUN,
                                subtitle = null,
                                xpEarned = 140,
                            ),
                            WorkoutHistoryItem(
                                workoutId = "w2",
                                startTime = Instant.parse("2026-01-28T11:00:00Z"),
                                dateFormatted = "Jan 28",
                                timeFormatted = "11:00 AM",
                                relativeDateFormatted = "Jan 28",
                                distanceFormatted = "0.92 mi",
                                durationFormatted = "19:30",
                                avgPaceFormatted = "21:12 /mi",
                                activityType = ActivityType.DOG_WALK,
                                subtitle = "River Trail",
                                xpEarned = 70,
                            ),
                        ),
                    ),
                ),
            )
            every { filter } returns MutableStateFlow(ActivityFilter.ALL)
        }

        captureScreenshot("Screen_History", category = "history") {
            HistoryScreen(
                onWorkoutClick = {},
                onBackClick = {},
                viewModel = viewModel,
            )
        }
    }
}
