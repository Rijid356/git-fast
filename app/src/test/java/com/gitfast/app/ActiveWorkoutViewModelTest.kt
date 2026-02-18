package com.gitfast.app

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.service.WorkoutService
import com.gitfast.app.ui.workout.ActiveWorkoutViewModel
import com.gitfast.app.ui.workout.WorkoutUiState
import com.gitfast.app.util.PermissionManager
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ActiveWorkoutViewModelTest {

    private lateinit var application: Application
    private lateinit var permissionManager: PermissionManager
    private lateinit var settingsStore: SettingsStore
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var viewModel: ActiveWorkoutViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        permissionManager = mockk()
        settingsStore = mockk(relaxed = true)
        workoutRepository = mockk(relaxed = true)

        every { permissionManager.checkPermissions() } returns PermissionManager.PermissionState(
            fineLocation = false,
            backgroundLocation = false,
            notifications = false,
        )

        viewModel = ActiveWorkoutViewModel(application, permissionManager, settingsStore, workoutRepository)
    }

    @After
    fun tearDown() {
        WorkoutService.isRunning = false
    }

    @Test
    fun `initial ui state has default values`() {
        val state = viewModel.uiState.value
        assertEquals(WorkoutUiState(), state)
    }

    @Test
    fun `initial permission state reflects permissionManager`() {
        val state = viewModel.permissionState.value
        assertFalse(state.fineLocation)
        assertFalse(state.backgroundLocation)
        assertFalse(state.notifications)
        assertFalse(state.canTrackWorkout)
    }

    @Test
    fun `refreshPermissions updates permission state`() {
        every { permissionManager.checkPermissions() } returns PermissionManager.PermissionState(
            fineLocation = true,
            backgroundLocation = true,
            notifications = true,
        )

        viewModel.refreshPermissions()

        val state = viewModel.permissionState.value
        assertTrue(state.fineLocation)
        assertTrue(state.backgroundLocation)
        assertTrue(state.notifications)
        assertTrue(state.canTrackWorkout)
    }

    @Test
    fun `setActivityType is reflected in startWorkout intent`() {
        viewModel.setActivityType(ActivityType.DOG_WALK)
        viewModel.startWorkout()

        val shadow = shadowOf(application)
        val intent = shadow.nextStartedService
        assertEquals(ActivityType.DOG_WALK.name, intent.getStringExtra(WorkoutService.EXTRA_ACTIVITY_TYPE))
    }

    @Test
    fun `startWorkout sends ACTION_START intent with activity type`() {
        viewModel.setActivityType(ActivityType.RUN)
        viewModel.startWorkout()

        val shadow = shadowOf(application)
        val intent = shadow.nextStartedService
        assertNotNull(intent)
        assertEquals(WorkoutService.ACTION_START, intent.action)
        assertEquals(ActivityType.RUN.name, intent.getStringExtra(WorkoutService.EXTRA_ACTIVITY_TYPE))
    }

    @Test
    fun `pauseWorkout sends ACTION_PAUSE intent`() {
        viewModel.pauseWorkout()

        val shadow = shadowOf(application)
        val intent = shadow.nextStartedService
        assertNotNull(intent)
        assertEquals(WorkoutService.ACTION_PAUSE, intent.action)
    }

    @Test
    fun `resumeWorkout sends ACTION_RESUME intent`() {
        viewModel.resumeWorkout()

        val shadow = shadowOf(application)
        val intent = shadow.nextStartedService
        assertNotNull(intent)
        assertEquals(WorkoutService.ACTION_RESUME, intent.action)
    }

    @Test
    fun `stopWorkout sends ACTION_STOP intent`() {
        viewModel.stopWorkout()

        val shadow = shadowOf(application)
        val intent = shadow.nextStartedService
        assertNotNull(intent)
        assertEquals(WorkoutService.ACTION_STOP, intent.action)
    }

    @Test
    fun `discardWorkout sends ACTION_DISCARD intent`() {
        viewModel.discardWorkout()

        val shadow = shadowOf(application)
        val intent = shadow.nextStartedService
        assertNotNull(intent)
        assertEquals(WorkoutService.ACTION_DISCARD, intent.action)
    }

    @Test
    fun `startLaps sends ACTION_START_LAPS intent`() {
        viewModel.startLaps()

        val shadow = shadowOf(application)
        val intent = shadow.nextStartedService
        assertNotNull(intent)
        assertEquals(WorkoutService.ACTION_START_LAPS, intent.action)
    }

    @Test
    fun `markLap sends ACTION_MARK_LAP intent`() {
        viewModel.markLap()

        val shadow = shadowOf(application)
        val intent = shadow.nextStartedService
        assertNotNull(intent)
        assertEquals(WorkoutService.ACTION_MARK_LAP, intent.action)
    }

    @Test
    fun `endLaps sends ACTION_END_LAPS intent`() {
        viewModel.endLaps()

        val shadow = shadowOf(application)
        val intent = shadow.nextStartedService
        assertNotNull(intent)
        assertEquals(WorkoutService.ACTION_END_LAPS, intent.action)
    }

    @Test
    fun `lastSummaryStats returns default when no workout stopped`() {
        val stats = viewModel.lastSummaryStats
        assertEquals("00:00", stats.time)
        assertEquals("0.00 mi", stats.distance)
        assertEquals("-- /mi", stats.pace)
        assertEquals("0", stats.points)
        assertEquals(0, stats.xpEarned)
    }

    @Test
    fun `lastWorkoutId is null initially`() {
        assertNull(viewModel.lastWorkoutId)
    }
}
