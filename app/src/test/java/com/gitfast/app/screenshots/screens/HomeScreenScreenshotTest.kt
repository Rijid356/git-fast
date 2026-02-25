package com.gitfast.app.screenshots.screens

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.BodyCompReading
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.DailyActivityMetrics
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.history.WorkoutHistoryItem
import com.gitfast.app.ui.home.HomeScreen
import com.gitfast.app.ui.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class HomeScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen Home`() {
        val viewModel = mockk<HomeViewModel>(relaxed = true) {
            every { showRecoveryDialog } returns MutableStateFlow(false)
            every { characterProfile } returns MutableStateFlow(
                CharacterProfile(
                    level = 12,
                    totalXp = 4850,
                    xpForCurrentLevel = 4500,
                    xpForNextLevel = 5200,
                    xpProgressInLevel = 350,
                    xpProgress = 0.5f,
                    speedStat = 34,
                    enduranceStat = 28,
                    consistencyStat = 45,
                    vitalityStat = 22,
                    currentStreak = 3,
                    streakMultiplier = 1.2,
                ),
            )
            every { dailyMetrics } returns MutableStateFlow(
                DailyActivityMetrics(
                    activeMinutes = 18,
                    activeMinutesGoal = 22,
                    distanceMiles = 1.2,
                    distanceGoal = 1.5,
                    activeDaysThisWeek = 3,
                    activeDaysGoal = 5,
                ),
            )
            every { latestWeight } returns MutableStateFlow(
                BodyCompReading(
                    id = "w1",
                    timestamp = Instant.parse("2026-02-24T08:00:00Z"),
                    weightKg = 79.4,
                    weightLbs = 175.0,
                    bodyFatPercent = 18.5,
                    leanBodyMassKg = null,
                    leanBodyMassLbs = null,
                    boneMassKg = null,
                    boneMassLbs = null,
                    bmrKcalPerDay = null,
                    heightMeters = null,
                    bmi = null,
                    source = "Health Connect",
                ),
            )
            every { recentRuns } returns MutableStateFlow(
                listOf(
                    WorkoutHistoryItem(
                        workoutId = "run1",
                        startTime = Instant.parse("2026-02-24T07:30:00Z"),
                        dateFormatted = "Feb 24, 2026",
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
                        workoutId = "run2",
                        startTime = Instant.parse("2026-02-22T18:00:00Z"),
                        dateFormatted = "Feb 22, 2026",
                        timeFormatted = "6:00 PM",
                        relativeDateFormatted = "2 days ago",
                        distanceFormatted = "2.45 mi",
                        durationFormatted = "21:15",
                        avgPaceFormatted = "8:40 /mi",
                        activityType = ActivityType.RUN,
                        subtitle = null,
                        xpEarned = 120,
                    ),
                ),
            )
            every { recentDogWalks } returns MutableStateFlow(
                listOf(
                    WorkoutHistoryItem(
                        workoutId = "walk1",
                        startTime = Instant.parse("2026-02-24T12:00:00Z"),
                        dateFormatted = "Feb 24, 2026",
                        timeFormatted = "12:00 PM",
                        relativeDateFormatted = "Today",
                        distanceFormatted = "1.05 mi",
                        durationFormatted = "22:45",
                        avgPaceFormatted = "21:40 /mi",
                        activityType = ActivityType.DOG_WALK,
                        subtitle = "Park Loop",
                        xpEarned = 80,
                    ),
                ),
            )
        }

        captureScreenshot("Screen_Home") {
            HomeScreen(
                onStartWorkout = {},
                onViewHistory = {},
                onWorkoutClick = {},
                onSettingsClick = {},
                onCharacterClick = {},
                onAnalyticsClick = {},
                onGoalsClick = {},
                onBodyCompClick = {},
                viewModel = viewModel,
            )
        }
    }
}
