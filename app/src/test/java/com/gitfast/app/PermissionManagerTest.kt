package com.gitfast.app

import com.gitfast.app.util.PermissionManager.PermissionState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionManagerTest {

    @Test
    fun `canTrackWorkout returns true when all permissions granted`() {
        val state = PermissionState(
            fineLocation = true,
            backgroundLocation = true,
            notifications = true,
            activityRecognition = true
        )
        assertTrue(state.canTrackWorkout)
    }

    @Test
    fun `canTrackWorkout returns false when fine location denied`() {
        val state = PermissionState(
            fineLocation = false,
            backgroundLocation = true,
            notifications = true,
            activityRecognition = true
        )
        assertFalse(state.canTrackWorkout)
    }

    @Test
    fun `needsBackgroundLocation returns true when fine granted but background denied`() {
        val state = PermissionState(
            fineLocation = true,
            backgroundLocation = false,
            notifications = true,
            activityRecognition = true
        )
        assertTrue(state.needsBackgroundLocation)
    }
}
