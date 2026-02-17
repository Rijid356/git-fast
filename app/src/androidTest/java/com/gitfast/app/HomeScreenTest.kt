package com.gitfast.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.gitfast.app.ui.home.HomeScreen
import com.gitfast.app.ui.theme.GitFastTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysTitle() {
        composeTestRule.setContent {
            GitFastTheme {
                HomeScreen(
                    onStartWorkout = {},
                    onViewHistory = {},
                )
            }
        }

        composeTestRule.onNodeWithText("git-fast").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysTagline() {
        composeTestRule.setContent {
            GitFastTheme {
                HomeScreen(
                    onStartWorkout = {},
                    onViewHistory = {},
                )
            }
        }

        composeTestRule.onNodeWithText("track runs like commits", substring = true).assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysStartWorkoutButton() {
        composeTestRule.setContent {
            GitFastTheme {
                HomeScreen(
                    onStartWorkout = {},
                    onViewHistory = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Start Workout").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysViewHistoryButton() {
        composeTestRule.setContent {
            GitFastTheme {
                HomeScreen(
                    onStartWorkout = {},
                    onViewHistory = {},
                )
            }
        }

        composeTestRule.onNodeWithText("View History").assertIsDisplayed()
    }
}
