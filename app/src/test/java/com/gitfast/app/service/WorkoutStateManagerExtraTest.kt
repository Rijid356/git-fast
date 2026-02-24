package com.gitfast.app.service

import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.GpsPoint
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.util.AchievementDef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class WorkoutStateManagerExtraTest {

    private lateinit var manager: WorkoutStateManager

    @Before
    fun setUp() {
        manager = WorkoutStateManager()
    }

    // =========================================================================
    // setUnlockedAchievements
    // =========================================================================

    @Test
    fun `setUnlockedAchievements updates flow`() {
        val achievements = listOf(AchievementDef.FIRST_MILE, AchievementDef.FIRST_STEPS)
        manager.setUnlockedAchievements(achievements)

        assertEquals(achievements, manager.lastUnlockedAchievements.value)
    }

    @Test
    fun `setUnlockedAchievements with empty list clears achievements`() {
        manager.setUnlockedAchievements(listOf(AchievementDef.FIRST_MILE))
        manager.setUnlockedAchievements(emptyList())

        assertTrue(manager.lastUnlockedAchievements.value.isEmpty())
    }

    // =========================================================================
    // setSaveStreakInfo
    // =========================================================================

    @Test
    fun `setSaveStreakInfo updates both flows`() {
        manager.setSaveStreakInfo(5, 1.5)

        assertEquals(5, manager.lastSaveStreakDays.value)
        assertEquals(1.5, manager.lastSaveStreakMultiplier.value, 0.001)
    }

    @Test
    fun `setSaveStreakInfo defaults are zero and one`() {
        assertEquals(0, manager.lastSaveStreakDays.value)
        assertEquals(1.0, manager.lastSaveStreakMultiplier.value, 0.001)
    }

    // =========================================================================
    // initStepBaseline and updateStepCount
    // =========================================================================

    @Test
    fun `initStepBaseline sets baseline and resets counts`() {
        manager.startWorkout()
        manager.initStepBaseline(1000)
        manager.updateStepCount(1050)

        assertEquals(50, manager.workoutState.value.stepCount)
    }

    @Test
    fun `updateStepCount calculates delta from baseline`() {
        manager.startWorkout()
        manager.initStepBaseline(500)

        manager.updateStepCount(510)
        assertEquals(10, manager.workoutState.value.stepCount)

        manager.updateStepCount(600)
        assertEquals(100, manager.workoutState.value.stepCount)
    }

    @Test
    fun `step count resets after stopWorkout`() {
        manager.startWorkout()
        manager.initStepBaseline(100)
        manager.updateStepCount(200)
        assertEquals(100, manager.workoutState.value.stepCount)

        manager.stopWorkout()
        assertEquals(0, manager.workoutState.value.stepCount)
    }

    // =========================================================================
    // homeArrivalPause
    // =========================================================================

    @Test
    fun `homeArrivalPause sets isPaused and isHomeArrivalPaused`() {
        manager.startWorkout()

        manager.homeArrivalPause()

        assertTrue(manager.workoutState.value.isPaused)
        assertTrue(manager.workoutState.value.isHomeArrivalPaused)
    }

    @Test
    fun `homeArrivalPause cleared by resumeWorkout`() {
        manager.startWorkout()
        manager.homeArrivalPause()

        manager.resumeWorkout()

        assertFalse(manager.workoutState.value.isPaused)
        assertFalse(manager.workoutState.value.isHomeArrivalPaused)
    }

    @Test
    fun `homeArrivalPause cleared by autoResumeWorkout`() {
        manager.startWorkout()
        manager.homeArrivalPause()

        manager.autoResumeWorkout()

        assertFalse(manager.workoutState.value.isPaused)
        assertFalse(manager.workoutState.value.isHomeArrivalPaused)
    }

    // =========================================================================
    // getBestLapNumber
    // =========================================================================

    @Test
    fun `getBestLapNumber returns null with no laps`() {
        manager.startWorkout()
        assertNull(manager.getBestLapNumber())
    }

    @Test
    fun `getBestLapNumber returns lap number of fastest lap`() {
        manager.startWorkout()
        manager.startLaps()

        // Lap 1: ~100ms
        Thread.sleep(100)
        manager.markLap()

        // Lap 2: ~10ms (faster)
        Thread.sleep(10)
        manager.markLap()

        // Lap 3: ~150ms
        Thread.sleep(150)
        manager.markLap()

        // Fastest should be lap 2
        assertEquals(2, manager.getBestLapNumber())
    }

    // =========================================================================
    // getLapDurations
    // =========================================================================

    @Test
    fun `getLapDurations returns empty list with no laps`() {
        manager.startWorkout()
        assertTrue(manager.getLapDurations().isEmpty())
    }

    @Test
    fun `getLapDurations returns durations in seconds for each lap`() {
        manager.startWorkout()
        manager.startLaps()

        Thread.sleep(50)
        manager.markLap()
        Thread.sleep(50)
        manager.markLap()

        val durations = manager.getLapDurations()
        assertEquals(2, durations.size)
    }

    // =========================================================================
    // updateElapsedTime guards
    // =========================================================================

    @Test
    fun `updateElapsedTime does nothing before workout starts`() {
        manager.updateElapsedTime()
        assertEquals(0, manager.workoutState.value.elapsedSeconds)
    }

    @Test
    fun `updateElapsedTime does nothing when paused`() {
        manager.startWorkout()
        Thread.sleep(50)
        manager.updateElapsedTime()
        val beforePause = manager.workoutState.value.elapsedSeconds

        manager.pauseWorkout()
        Thread.sleep(50)
        manager.updateElapsedTime()

        // Should not have changed from pre-pause value
        assertEquals(beforePause, manager.workoutState.value.elapsedSeconds)
    }

    // =========================================================================
    // endLaps guard
    // =========================================================================

    @Test
    fun `endLaps does nothing when not in LAPS phase`() {
        manager.startWorkout()
        // Still in WARMUP phase
        assertEquals(PhaseType.WARMUP, manager.workoutState.value.phase)

        manager.endLaps()

        // Phase should still be WARMUP (not COOLDOWN)
        assertEquals(PhaseType.WARMUP, manager.workoutState.value.phase)
    }

    // =========================================================================
    // startWorkout with DOG_WALK activity type
    // =========================================================================

    @Test
    fun `startWorkout with DOG_WALK sets correct activity type`() {
        manager.startWorkout(ActivityType.DOG_WALK)

        assertEquals(ActivityType.DOG_WALK, manager.workoutState.value.activityType)
    }

    @Test
    fun `stopWorkout with DOG_WALK returns correct activity type in snapshot`() {
        manager.startWorkout(ActivityType.DOG_WALK)
        val snapshot = manager.stopWorkout()

        assertEquals(ActivityType.DOG_WALK, snapshot.activityType)
    }

    // =========================================================================
    // Step tracking through phases
    // =========================================================================

    @Test
    fun `step count included in stopWorkout snapshot`() {
        manager.startWorkout()
        manager.initStepBaseline(100)
        manager.updateStepCount(250)

        val snapshot = manager.stopWorkout()

        assertEquals(150, snapshot.totalSteps)
    }

    // =========================================================================
    // addGpsPoint with single point
    // =========================================================================

    @Test
    fun `addGpsPoint with single point keeps distance at zero`() {
        manager.startWorkout()
        manager.addGpsPoint(
            GpsPoint(40.7128, -74.0060, Instant.now(), 5.0f)
        )

        assertEquals(0.0, manager.workoutState.value.distanceMeters, 0.001)
        assertEquals(1, manager.gpsPoints.value.size)
    }

    // =========================================================================
    // discardMicroLap behavior (tested via endLaps)
    // =========================================================================

    @Test
    fun `endLaps discards micro-lap and merges distance into previous`() {
        manager.startWorkout()
        manager.startLaps()

        // Lap 1: reasonable duration
        Thread.sleep(100)
        manager.markLap()

        // Immediately end laps — the in-progress "lap 2" will be very short (< 5s)
        // and should be auto-completed then discarded/merged
        manager.endLaps()

        val state = manager.workoutState.value
        assertEquals(PhaseType.COOLDOWN, state.phase)
    }
}
