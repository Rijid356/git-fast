package com.gitfast.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class PermissionState(
        val fineLocation: Boolean,
        val backgroundLocation: Boolean,
        val notifications: Boolean
    ) {
        val canTrackWorkout: Boolean
            get() = fineLocation && backgroundLocation && notifications

        val needsBackgroundLocation: Boolean
            get() = fineLocation && !backgroundLocation
    }

    fun checkPermissions(): PermissionState {
        return PermissionState(
            fineLocation = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED,
            backgroundLocation = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED,
            notifications = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
}
