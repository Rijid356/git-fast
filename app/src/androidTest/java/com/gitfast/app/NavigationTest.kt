package com.gitfast.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.gitfast.app.navigation.GitFastNavGraph
import com.gitfast.app.navigation.Screen
import com.gitfast.app.ui.theme.GitFastTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navGraph_startsAtHomeScreen() {
        composeTestRule.setContent {
            GitFastTheme {
                val navController = rememberNavController()
                GitFastNavGraph(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("git-fast").assertIsDisplayed()
    }

    @Test
    fun screenRoutes_areCorrect() {
        assertEquals("home", Screen.Home.route)
        assertEquals("workout", Screen.Workout.route)
        assertEquals("history", Screen.History.route)
        assertEquals("detail/{workoutId}", Screen.Detail.route)
    }

    @Test
    fun detailScreen_createsCorrectRoute() {
        assertEquals("detail/42", Screen.Detail.createRoute(42))
        assertEquals("detail/1", Screen.Detail.createRoute(1))
    }
}
