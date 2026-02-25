package com.gitfast.app.screenshots

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.gitfast.app.ui.theme.GitFastTheme
import org.junit.Rule
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
abstract class ScreenshotTestBase {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val roborazziOptions = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(
            changeThreshold = 0.01f, // 1% tolerance for cross-platform rendering differences
        ),
    )

    protected fun captureScreenshot(name: String, composable: @Composable () -> Unit) {
        composeTestRule.setContent {
            GitFastTheme {
                composable()
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/snapshots/$name.png",
            roborazziOptions = roborazziOptions,
        )
    }
}
