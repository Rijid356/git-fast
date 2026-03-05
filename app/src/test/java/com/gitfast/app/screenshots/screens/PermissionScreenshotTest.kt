package com.gitfast.app.screenshots.screens

import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.workout.PermissionRequestContent
import com.gitfast.app.util.PermissionManager
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PermissionScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen Permissions all denied`() {
        captureScreenshot("Screen_Permissions_AllDenied", category = "workout") {
            PermissionRequestContent(
                permissionState = PermissionManager.PermissionState(
                    fineLocation = false,
                    backgroundLocation = false,
                    notifications = false,
                    activityRecognition = false,
                ),
                onPermissionsChanged = {},
            )
        }
    }

    @Test
    fun `Screen Permissions location granted`() {
        captureScreenshot("Screen_Permissions_LocationGranted", category = "workout") {
            PermissionRequestContent(
                permissionState = PermissionManager.PermissionState(
                    fineLocation = true,
                    backgroundLocation = false,
                    notifications = false,
                    activityRecognition = false,
                ),
                onPermissionsChanged = {},
            )
        }
    }

    @Test
    fun `Screen Permissions all granted`() {
        captureScreenshot("Screen_Permissions_AllGranted", category = "workout") {
            PermissionRequestContent(
                permissionState = PermissionManager.PermissionState(
                    fineLocation = true,
                    backgroundLocation = true,
                    notifications = true,
                    activityRecognition = true,
                ),
                onPermissionsChanged = {},
            )
        }
    }
}
