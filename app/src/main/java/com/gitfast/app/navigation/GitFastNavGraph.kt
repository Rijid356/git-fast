package com.gitfast.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gitfast.app.ui.home.HomeScreen
import com.gitfast.app.ui.workout.ActiveWorkoutScreen
import com.gitfast.app.ui.workout.WorkoutSummaryScreen
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Workout : Screen("workout")
    data object History : Screen("history")
    data object Detail : Screen("detail/{workoutId}") {
        fun createRoute(workoutId: Long): String = "detail/$workoutId"
    }
    data object WorkoutSummary : Screen("workout_summary/{time}/{distance}/{pace}/{points}") {
        fun createRoute(time: String, distance: String, pace: String, points: String): String {
            val enc = { s: String -> URLEncoder.encode(s, "UTF-8") }
            return "workout_summary/${enc(time)}/${enc(distance)}/${enc(pace)}/${enc(points)}"
        }
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
            ActiveWorkoutScreen(
                onWorkoutComplete = { time, distance, pace, points ->
                    navController.navigate(
                        Screen.WorkoutSummary.createRoute(time, distance, pace, points)
                    ) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onWorkoutDiscarded = {
                    navController.popBackStack(
                        route = Screen.Home.route,
                        inclusive = false,
                    )
                },
            )
        }
        composable(
            route = Screen.WorkoutSummary.route,
            arguments = listOf(
                navArgument("time") { type = NavType.StringType },
                navArgument("distance") { type = NavType.StringType },
                navArgument("pace") { type = NavType.StringType },
                navArgument("points") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val dec = { key: String ->
                URLDecoder.decode(
                    backStackEntry.arguments?.getString(key) ?: "",
                    "UTF-8",
                )
            }
            WorkoutSummaryScreen(
                time = dec("time"),
                distance = dec("distance"),
                pace = dec("pace"),
                points = dec("points"),
                onViewDetails = {
                    // TODO: Navigate to detail screen when available
                },
                onDone = {
                    navController.popBackStack(
                        route = Screen.Home.route,
                        inclusive = false,
                    )
                },
            )
        }
        composable(Screen.History.route) {
            // TODO: HistoryScreen - Checkpoint 2
        }
        composable(Screen.Detail.route) {
            // TODO: DetailScreen - Checkpoint 2
        }
    }
}
