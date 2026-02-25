package com.gitfast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gitfast.app.navigation.GitFastNavGraph
import com.gitfast.app.navigation.Screen
import com.gitfast.app.service.WorkoutService
import com.gitfast.app.service.WorkoutStateManager
import com.gitfast.app.ui.components.ActiveWorkoutBanner
import com.gitfast.app.ui.theme.GitFastTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var workoutStateManager: WorkoutStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GitFastTheme {
                val navController = rememberNavController()
                val workoutState by workoutStateManager.workoutState.collectAsStateWithLifecycle()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBanner = workoutState.isActive &&
                    currentRoute != null &&
                    !currentRoute.startsWith("workout")

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
