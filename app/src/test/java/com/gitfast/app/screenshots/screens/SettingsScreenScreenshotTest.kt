package com.gitfast.app.screenshots.screens

import com.gitfast.app.data.healthconnect.HealthConnectManager
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.settings.SettingsScreen
import com.gitfast.app.ui.settings.SettingsUiState
import com.gitfast.app.ui.settings.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen Settings`() {
        val mockHealthConnectManager = mockk<HealthConnectManager>(relaxed = true)

        val viewModel = mockk<SettingsViewModel>(relaxed = true) {
            every { uiState } returns MutableStateFlow(
                SettingsUiState(
                    autoPauseEnabled = true,
                    keepScreenOn = true,
                    autoLapEnabled = false,
                    homeArrivalEnabled = false,
                    hasHomeLocation = false,
                    isSignedIn = true,
                    userEmail = "ryan@example.com",
                    healthConnectAvailable = true,
                    healthConnectConnected = true,
                    latestWeight = "175.0 lbs",
                    latestWeightDate = "Feb 24",
                ),
            )
            every { healthConnectManager } returns mockHealthConnectManager
        }

        captureScreenshot("Screen_Settings", category = "settings") {
            SettingsScreen(
                onBackClick = {},
                viewModel = viewModel,
            )
        }
    }
}
