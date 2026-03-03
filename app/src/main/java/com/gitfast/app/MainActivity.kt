package com.gitfast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.navigation.GitFastNavGraph
import com.gitfast.app.navigation.Screen
import com.gitfast.app.service.WorkoutService
import com.gitfast.app.service.WorkoutStateManager
import com.gitfast.app.ui.components.ActiveWorkoutBanner
import com.gitfast.app.ui.components.ScreenshotOverlay
import com.gitfast.app.ui.theme.GitFastTheme
import com.gitfast.app.util.ScreenCaptureManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var workoutStateManager: WorkoutStateManager
    @Inject lateinit var screenCaptureManager: ScreenCaptureManager
    @Inject lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GitFastTheme {
                val navController = rememberNavController()
                val workoutState by workoutStateManager.workoutState.collectAsStateWithLifecycle()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val coroutineScope = rememberCoroutineScope()

                var isCapturing by remember { mutableStateOf(false) }
                var overlayEnabled by remember { mutableStateOf(settingsStore.screenshotOverlayEnabled) }

                val showBanner = workoutState.isActive &&
                    currentRoute != null &&
                    !currentRoute.startsWith("workout")

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (showBanner) {
                            ActiveWorkoutBanner(
                                workoutState = workoutState,
                                onClick = {
                                    navController.navigate(
                                        Screen.Workout.createRoute(workoutState.activityType)
                                    ) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                        GitFastNavGraph(
                            navController = navController,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (overlayEnabled && !isCapturing) {
                        ScreenshotOverlay(
                            onCaptureRequest = {
                                coroutineScope.launch {
                                    isCapturing = true
                                    delay(100)
                                    screenCaptureManager.captureAndSave(
                                        this@MainActivity,
                                        currentRoute,
                                    )
                                    isCapturing = false
                                }
                            },
                        )
                    }
                }

                // Re-read setting when returning to composition (e.g., back from Settings)
                LaunchedEffect(currentRoute) {
                    overlayEnabled = settingsStore.screenshotOverlayEnabled
                }

                LaunchedEffect(Unit) {
                    if (WorkoutService.isRunning) {
                        val activityType = workoutStateManager.workoutState.value.activityType
                        navController.navigate(Screen.Workout.createRoute(activityType)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}
