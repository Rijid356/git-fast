package com.gitfast.app.screenshots.screens

import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.analytics.routeoverlay.RouteOverlayScreen
import com.gitfast.app.ui.analytics.routeoverlay.RouteOverlayUiState
import com.gitfast.app.ui.analytics.routeoverlay.RouteOverlayViewModel
import com.gitfast.app.ui.analytics.routeoverlay.RouteTrace
import com.gitfast.app.ui.detail.LatLngPoint
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.compose.ui.graphics.Color

@RunWith(RobolectricTestRunner::class)
class RouteOverlayScreenContentTest : FullScreenScreenshotTestBase() {

    private fun createViewModel(
        uiState: RouteOverlayUiState = RouteOverlayUiState(),
    ): RouteOverlayViewModel = mockk(relaxed = true) {
        every { this@mockk.uiState } returns MutableStateFlow(uiState)
    }

    @Test
    fun `Screen RouteOverlay loading`() {
        val viewModel = createViewModel(
            uiState = RouteOverlayUiState(
                routeTags = listOf("Park Loop", "River Trail"),
                selectedTag = "Park Loop",
                isLoading = true,
            ),
        )

        captureScreenshot("Screen_RouteOverlay_Loading", category = "analytics") {
            RouteOverlayScreen(onBackClick = {}, viewModel = viewModel)
        }
    }

    @Test
    fun `Screen RouteOverlay empty no selection`() {
        val viewModel = createViewModel(
            uiState = RouteOverlayUiState(
                routeTags = listOf("Park Loop", "River Trail", "Neighborhood"),
                selectedTag = null,
            ),
        )

        captureScreenshot("Screen_RouteOverlay_NoSelection", category = "analytics") {
            RouteOverlayScreen(onBackClick = {}, viewModel = viewModel)
        }
    }

    @Test
    fun `Screen RouteOverlay no gps data`() {
        val viewModel = createViewModel(
            uiState = RouteOverlayUiState(
                routeTags = listOf("Park Loop"),
                selectedTag = "Park Loop",
                traces = emptyList(),
            ),
        )

        captureScreenshot("Screen_RouteOverlay_NoGps", category = "analytics") {
            RouteOverlayScreen(onBackClick = {}, viewModel = viewModel)
        }
    }

    @Test
    fun `Screen RouteOverlay with traces`() {
        val viewModel = createViewModel(
            uiState = RouteOverlayUiState(
                routeTags = listOf("Park Loop", "River Trail"),
                selectedTag = "Park Loop",
                traces = listOf(
                    RouteTrace(
                        workoutId = "w1",
                        date = "Feb 24",
                        durationFormatted = "22:30",
                        distanceFormatted = "1.05 mi",
                        points = listOf(
                            LatLngPoint(40.7128, -74.0060),
                            LatLngPoint(40.7130, -74.0055),
                        ),
                        color = Color(0xFF39FF14),
                    ),
                    RouteTrace(
                        workoutId = "w2",
                        date = "Feb 22",
                        durationFormatted = "24:15",
                        distanceFormatted = "1.02 mi",
                        points = listOf(
                            LatLngPoint(40.7128, -74.0060),
                            LatLngPoint(40.7131, -74.0054),
                        ),
                        color = Color(0xFF58A6FF),
                    ),
                    RouteTrace(
                        workoutId = "w3",
                        date = "Feb 20",
                        durationFormatted = "25:00",
                        distanceFormatted = "1.01 mi",
                        points = listOf(
                            LatLngPoint(40.7128, -74.0060),
                            LatLngPoint(40.7129, -74.0056),
                        ),
                        color = Color(0xFFF0883E),
                    ),
                ),
            ),
        )

        captureScreenshot("Screen_RouteOverlay_WithTraces", category = "analytics") {
            RouteOverlayScreen(onBackClick = {}, viewModel = viewModel)
        }
    }
}
