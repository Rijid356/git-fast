package com.gitfast.app.screenshots

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.service.WorkoutTrackingState
import com.gitfast.app.ui.components.ActiveWorkoutBanner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActiveWorkoutBannerScreenshotTest : ScreenshotTestBase() {

    @Test
    fun `ActiveWorkoutBanner running`() {
        captureScreenshot("ActiveWorkoutBanner_running", category = "components") {
            ActiveWorkoutBanner(
                workoutState = WorkoutTrackingState(
                    isActive = true,
                    elapsedSeconds = 754,
                    distanceMeters = 2450.0,
                    activityType = ActivityType.RUN,
                ),
                onClick = {},
            )
        }
    }

    @Test
    fun `ActiveWorkoutBanner paused`() {
        captureScreenshot("ActiveWorkoutBanner_paused", category = "components") {
            ActiveWorkoutBanner(
                workoutState = WorkoutTrackingState(
                    isActive = true,
                    isPaused = true,
                    elapsedSeconds = 420,
                    distanceMeters = 1200.0,
                    activityType = ActivityType.RUN,
                ),
                onClick = {},
            )
        }
    }

    @Test
    fun `ActiveWorkoutBanner dog walk`() {
        captureScreenshot("ActiveWorkoutBanner_dogwalk", category = "components") {
            ActiveWorkoutBanner(
                workoutState = WorkoutTrackingState(
                    isActive = true,
                    elapsedSeconds = 1320,
                    distanceMeters = 1800.0,
                    activityType = ActivityType.DOG_WALK,
                ),
                onClick = {},
            )
        }
    }

    @Test
    fun `ActiveWorkoutBanner auto paused`() {
        captureScreenshot("ActiveWorkoutBanner_autopaused", category = "components") {
            ActiveWorkoutBanner(
                workoutState = WorkoutTrackingState(
                    isActive = true,
                    isAutoPaused = true,
                    elapsedSeconds = 600,
                    distanceMeters = 1600.0,
                    activityType = ActivityType.RUN,
                ),
                onClick = {},
            )
        }
    }

    @Test
    fun `ActiveWorkoutBanner home arrival paused`() {
        captureScreenshot("ActiveWorkoutBanner_homearrival", category = "components") {
            ActiveWorkoutBanner(
                workoutState = WorkoutTrackingState(
                    isActive = true,
                    isHomeArrivalPaused = true,
                    elapsedSeconds = 900,
                    distanceMeters = 2100.0,
                    activityType = ActivityType.DOG_WALK,
                ),
                onClick = {},
            )
        }
    }
}
