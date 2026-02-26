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
            changeThreshold = 0.05f, // 5% tolerance for cross-platform font rasterization differences
        ),
    )

    protected fun captureScreenshot(
        name: String,
        category: String = "",
        composable: @Composable () -> Unit,
    ) {
        composeTestRule.setContent {
            GitFastTheme {
                composable()
            }
        }
        val path = if (category.isNotEmpty()) "src/test/snapshots/components/$category/$name.png"
        else "src/test/snapshots/$name.png"
        composeTestRule.onRoot().captureRoboImage(
            path,
            roborazziOptions = roborazziOptions,
        )
    }
}
