package com.gitfast.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gitfast.app.ui.home.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Workout : Screen("workout")
    data object History : Screen("history")
    data object Detail : Screen("detail/{workoutId}") {
        fun createRoute(workoutId: Long): String = "detail/$workoutId"
    }
}

@Composable
fun GitFastNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartWorkout = {
                    navController.navigate(Screen.Workout.route)
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                },
            )
        }
        composable(Screen.Workout.route) {
            // TODO: WorkoutScreen - Checkpoint 1
        }
        composable(Screen.History.route) {
            // TODO: HistoryScreen - Checkpoint 2
        }
        composable(Screen.Detail.route) {
            // TODO: DetailScreen - Checkpoint 2
        }
    }
}
