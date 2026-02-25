package com.gitfast.app.screenshots.screens

import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.analytics.records.PersonalRecordsScreen
import com.gitfast.app.ui.analytics.records.PersonalRecordsUiState
import com.gitfast.app.ui.analytics.records.PersonalRecordsViewModel
import com.gitfast.app.ui.analytics.records.RecordItem
import com.gitfast.app.ui.analytics.records.RecordSection
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PersonalRecordsScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen PersonalRecords`() {
        val viewModel = mockk<PersonalRecordsViewModel>(relaxed = true) {
            every { uiState } returns MutableStateFlow(
                PersonalRecordsUiState(
                    isLoading = false,
                    isEmpty = false,
                    sections = listOf(
                        RecordSection(
                            header = "Distance",
                            records = listOf(
                                RecordItem(
                                    title = "Longest Run",
                                    value = "6.21 mi",
                                    context = "Jan 15, 2026",
                                    workoutId = "r10",
                                ),
                                RecordItem(
                                    title = "Longest Dog Walk",
                                    value = "2.34 mi",
                                    context = "Feb 8, 2026",
                                    workoutId = "w5",
                                ),
                            ),
                        ),
                        RecordSection(
                            header = "Speed",
                            records = listOf(
                                RecordItem(
                                    title = "Fastest Mile",
                                    value = "6:52 /mi",
                                    context = "Feb 20, 2026",
                                    workoutId = "r8",
                                ),
                                RecordItem(
                                    title = "Fastest 5K",
                                    value = "22:15",
                                    context = "Jan 28, 2026",
                                    workoutId = "r6",
                                ),
                            ),
                        ),
                        RecordSection(
                            header = "Duration",
                            records = listOf(
                                RecordItem(
                                    title = "Longest Workout",
                                    value = "52:10",
                                    context = "Jan 15, 2026",
                                    workoutId = "r10",
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        captureScreenshot("Screen_PersonalRecords") {
            PersonalRecordsScreen(
                onBackClick = {},
                onWorkoutClick = {},
                viewModel = viewModel,
            )
        }
    }
}
