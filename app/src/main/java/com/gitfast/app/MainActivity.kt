package com.gitfast.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.gitfast.app.navigation.GitFastNavGraph
import com.gitfast.app.navigation.Screen
import com.gitfast.app.service.WorkoutService
import com.gitfast.app.service.WorkoutStateManager
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
                GitFastNavGraph(navController = navController)

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
