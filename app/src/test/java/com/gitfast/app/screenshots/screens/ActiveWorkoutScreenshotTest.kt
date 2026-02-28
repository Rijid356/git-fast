package com.gitfast.app.screenshots.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.DogWalkEventType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.workout.EventWheelContent
import com.gitfast.app.ui.workout.WorkoutContent
import com.gitfast.app.ui.workout.WorkoutUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActiveWorkoutScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen Workout Running`() {
        captureScreenshot("Screen_Workout_Running", category = "workout") {
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
        captureScreenshot("Screen_Workout_Paused", category = "workout") {
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
        captureScreenshot("Screen_Workout_DogWalk", category = "workout") {
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
                dogWalkEventCounts = mapOf(
                    DogWalkEventType.DEEP_SNIFF to 3,
                    DogWalkEventType.PEE to 2,
                    DogWalkEventType.POOP to 1,
                    DogWalkEventType.SNACK_FOUND to 1,
                    DogWalkEventType.WATER_BREAK to 1,
                    DogWalkEventType.BARK_REACT to 2,
                ),
                onLogEvent = {},
                onUndoEvent = {},
            )
        }
    }

    @Test
    fun `Screen Workout DogRun`() {
        captureScreenshot("Screen_Workout_DogRun", category = "workout") {
            WorkoutContent(
                uiState = WorkoutUiState(
                    isActive = true,
                    isPaused = false,
                    activityType = ActivityType.DOG_RUN,
                    phase = PhaseType.WARMUP,
                    phaseLabel = "RUNNING",
                    elapsedTimeFormatted = "08:15",
                    distanceFormatted = "0.62 mi",
                    currentPaceFormatted = "13:18 /mi",
                    averagePaceFormatted = "13:18 /mi",
                    currentSpeedFormatted = "4.5 MPH",
                    stepCount = 1240,
                    sprintCount = 2,
                    totalSprintTimeFormatted = "01:35",
                ),
                onStart = {},
                onPause = {},
                onResume = {},
                onStop = {},
                onDiscard = {},
                onStartLaps = {},
                onMarkLap = {},
                onEndLaps = {},
                dogWalkEventCounts = mapOf(
                    DogWalkEventType.SQUIRREL_CHASE to 1,

                    DogWalkEventType.BARK_REACT to 1,
                ),
                onLogEvent = {},
                onUndoEvent = {},
            )
        }
    }

    @Test
    fun `Screen Workout DogRun Sprinting`() {
        captureScreenshot("Screen_Workout_DogRun_Sprinting", category = "workout") {
            WorkoutContent(
                uiState = WorkoutUiState(
                    isActive = true,
                    isPaused = false,
                    activityType = ActivityType.DOG_RUN,
                    phase = PhaseType.WARMUP,
                    phaseLabel = "RUNNING",
                    elapsedTimeFormatted = "10:42",
                    distanceFormatted = "0.85 mi",
                    currentPaceFormatted = "9:45 /mi",
                    averagePaceFormatted = "12:36 /mi",
                    currentSpeedFormatted = "6.2 MPH",
                    stepCount = 1620,
                    isSprintActive = true,
                    sprintCount = 3,
                    currentSprintTimeFormatted = "00:18",
                    totalSprintTimeFormatted = "02:10",
                    longestSprintTimeFormatted = "00:52",
                ),
                onStart = {},
                onPause = {},
                onResume = {},
                onStop = {},
                onDiscard = {},
                onStartLaps = {},
                onMarkLap = {},
                onEndLaps = {},
                dogWalkEventCounts = mapOf(
                    DogWalkEventType.SQUIRREL_CHASE to 2,

                    DogWalkEventType.SNACK_FOUND to 1,
                    DogWalkEventType.WATER_BREAK to 1,
                ),
                onLogEvent = {},
                onUndoEvent = {},
            )
        }
    }

    @Test
    fun `Screen Workout DogWalk EventWheel Expanded`() {
        val eventCounts = mapOf(
            DogWalkEventType.DEEP_SNIFF to 3,
            DogWalkEventType.PEE to 2,
            DogWalkEventType.POOP to 1,
            DogWalkEventType.SNACK_FOUND to 1,
            DogWalkEventType.SQUIRREL_CHASE to 1,
            DogWalkEventType.WATER_BREAK to 1,
            DogWalkEventType.BARK_REACT to 2,
        )
        captureScreenshot("Screen_Workout_DogWalk_EventWheel_Expanded", category = "workout") {
            Box(modifier = Modifier.fillMaxSize()) {
                // Workout screen visible underneath (no event counts so no
                // duplicate FAB — in the real app the FAB fades to alpha=0)
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
                    onLogEvent = {},
                    onUndoEvent = {},
                )
                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                )
                // Wheel at the FAB's position (~62% down the screen)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = BiasAlignment(
                        horizontalBias = 0f,
                        verticalBias = 0.3f,
                    ),
                ) {
                    EventWheelContent(
                        eventCounts = eventCounts,
                        expandProgress = 1f,
                        fabRotation = 45f,
                        onLogEvent = {},
                        onUndoEvent = {},
                    )
                }
            }
        }
    }

    @Test
    fun `Screen Workout Laps`() {
        captureScreenshot("Screen_Workout_Laps", category = "workout") {
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
