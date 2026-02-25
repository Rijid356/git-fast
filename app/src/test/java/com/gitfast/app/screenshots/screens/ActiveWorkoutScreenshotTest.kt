package com.gitfast.app.screenshots.screens

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.workout.WorkoutContent
import com.gitfast.app.ui.workout.WorkoutUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActiveWorkoutScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen Workout Running`() {
        captureScreenshot("Screen_Workout_Running") {
            WorkoutContent(
                uiState = WorkoutUiState(
                    isActive = true,
                    isPaused = false,
                    activityType = ActivityType.RUN,
                    phase = PhaseType.WARMUP,
                    phaseLabel = "RUNNING",
                    elapsedTimeFormatted = "12:34",
                    distanceFormatted = "1.52 mi",
                    currentPaceFormatted = "7:45 /mi",
                    averagePaceFormatted = "8:14 /mi",
                    currentSpeedFormatted = "7.7 MPH",
                    stepCount = 2847,
                ),
                onStart = {},
                onPause = {},
                onResume = {},
                onStop = {},
                onDiscard = {},
                onStartLaps = {},
                onMarkLap = {},
                onEndLaps = {},
            )
        }
    }

    @Test
    fun `Screen Workout Paused`() {
        captureScreenshot("Screen_Workout_Paused") {
            WorkoutContent(
                uiState = WorkoutUiState(
                    isActive = true,
                    isPaused = true,
                    activityType = ActivityType.RUN,
                    phase = PhaseType.WARMUP,
                    phaseLabel = "PAUSED",
                    elapsedTimeFormatted = "18:22",
                    distanceFormatted = "2.31 mi",
                    currentPaceFormatted = null,
                    averagePaceFormatted = "7:57 /mi",
                    stepCount = 4210,
                ),
                onStart = {},
                onPause = {},
                onResume = {},
                onStop = {},
                onDiscard = {},
                onStartLaps = {},
                onMarkLap = {},
                onEndLaps = {},
            )
        }
    }

    @Test
    fun `Screen Workout DogWalk`() {
        captureScreenshot("Screen_Workout_DogWalk") {
            WorkoutContent(
                uiState = WorkoutUiState(
                    isActive = true,
                    isPaused = false,
                    activityType = ActivityType.DOG_WALK,
                    phase = PhaseType.WARMUP,
                    phaseLabel = "WALKING",
                    elapsedTimeFormatted = "15:42",
                    distanceFormatted = "0.78 mi",
                    currentPaceFormatted = "20:10 /mi",
                    averagePaceFormatted = "20:10 /mi",
                    currentSpeedFormatted = "3.0 MPH",
                    stepCount = 1890,
                ),
                onStart = {},
                onPause = {},
                onResume = {},
                onStop = {},
                onDiscard = {},
                onStartLaps = {},
                onMarkLap = {},
                onEndLaps = {},
            )
        }
    }

    @Test
    fun `Screen Workout Laps`() {
        captureScreenshot("Screen_Workout_Laps") {
            WorkoutContent(
                uiState = WorkoutUiState(
                    isActive = true,
                    isPaused = false,
                    activityType = ActivityType.RUN,
                    phase = PhaseType.LAPS,
                    phaseLabel = "LAP 4",
                    elapsedTimeFormatted = "22:15",
                    distanceFormatted = "2.84 mi",
                    currentPaceFormatted = "7:32 /mi",
                    averagePaceFormatted = "7:50 /mi",
                    currentSpeedFormatted = "8.0 MPH",
                    stepCount = 5120,
                    lapCount = 4,
                    currentLapNumber = 4,
                    currentLapTimeFormatted = "1:48",
                    lastLapTimeFormatted = "2:05",
                    lastLapDeltaSeconds = -8,
                    lastLapDeltaFormatted = "▼ -8s",
                    bestLapTimeFormatted = "1:58",
                    averageLapTimeFormatted = "2:03",
                    autoLapAnchorSet = true,
                ),
                onStart = {},
                onPause = {},
                onResume = {},
                onStop = {},
                onDiscard = {},
                onStartLaps = {},
                onMarkLap = {},
                onEndLaps = {},
            )
        }
    }
}
