package com.gitfast.app.screenshots

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.ui.workout.WorkoutControls

@RunWith(RobolectricTestRunner::class)
class WorkoutControlsScreenshotTest : ScreenshotTestBase() {

    private val noOp: () -> Unit = {}

    @Test
    fun `WorkoutControls start run`() {
        captureScreenshot("WorkoutControls_start_run", category = "workout") {
            WorkoutControls(
                isActive = false,
                isPaused = false,
                phase = PhaseType.WARMUP,
                activityType = ActivityType.RUN,
                onStart = noOp,
                onPause = noOp,
                onResume = noOp,
                onStop = noOp,
                onDiscard = noOp,
                onStartLaps = noOp,
                onMarkLap = noOp,
                onEndLaps = noOp,
            )
        }
    }

    @Test
    fun `WorkoutControls warmup active`() {
        captureScreenshot("WorkoutControls_warmup_active", category = "workout") {
            WorkoutControls(
                isActive = true,
                isPaused = false,
                phase = PhaseType.WARMUP,
                activityType = ActivityType.RUN,
                onStart = noOp,
                onPause = noOp,
                onResume = noOp,
                onStop = noOp,
                onDiscard = noOp,
                onStartLaps = noOp,
                onMarkLap = noOp,
                onEndLaps = noOp,
            )
        }
    }

    @Test
    fun `WorkoutControls dog walk`() {
        captureScreenshot("WorkoutControls_dog_walk", category = "workout") {
            WorkoutControls(
                isActive = true,
                isPaused = false,
                phase = PhaseType.WARMUP,
                activityType = ActivityType.DOG_WALK,
                onStart = noOp,
                onPause = noOp,
                onResume = noOp,
                onStop = noOp,
                onDiscard = noOp,
                onStartLaps = noOp,
                onMarkLap = noOp,
                onEndLaps = noOp,
            )
        }
    }
}
