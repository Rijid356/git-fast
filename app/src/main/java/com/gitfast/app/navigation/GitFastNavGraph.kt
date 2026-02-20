package com.gitfast.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.ui.analytics.AnalyticsHubScreen
import com.gitfast.app.ui.analytics.routeoverlay.RouteOverlayScreen
import com.gitfast.app.ui.character.CharacterSheetScreen
import com.gitfast.app.ui.detail.DetailScreen
import com.gitfast.app.ui.dogwalk.DogWalkSummaryScreen
import com.gitfast.app.ui.history.HistoryScreen
import com.gitfast.app.ui.home.HomeScreen
import com.gitfast.app.ui.settings.SettingsScreen
import com.gitfast.app.ui.workout.ActiveWorkoutScreen
import com.gitfast.app.ui.workout.WorkoutSummaryScreen
import com.gitfast.app.ui.workout.WorkoutSummaryStats
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Workout : Screen("workout?activityType={activityType}") {
        fun createRoute(activityType: ActivityType): String = "workout?activityType=${activityType.name}"
    }
    data object History : Screen("history")
    data object Detail : Screen("detail/{workoutId}") {
        fun createRoute(workoutId: String): String = "detail/$workoutId"
    }
    data object Settings : Screen("settings")
    data object Analytics : Screen("analytics")
    data object RouteOverlay : Screen("route_overlay")
    data object CharacterSheet : Screen("character_sheet")
    data object DogWalkSummary : Screen("dog_walk_summary/{workoutId}") {
        fun createRoute(workoutId: String): String = "dog_walk_summary/$workoutId"
    }
    data object WorkoutSummary : Screen("workout_summary/{time}/{distance}/{pace}/{steps}?lapCount={lapCount}&bestLapTime={bestLapTime}&bestLapNumber={bestLapNumber}&trendLabel={trendLabel}&workoutId={workoutId}&xpEarned={xpEarned}&achievements={achievements}&streakDays={streakDays}") {
        fun createRoute(
            time: String,
            distance: String,
            pace: String,
            steps: String,
            lapCount: Int = 0,
            bestLapTime: String? = null,
            bestLapNumber: Int? = null,
            trendLabel: String? = null,
            workoutId: String? = null,
            xpEarned: Int = 0,
            achievements: List<String> = emptyList(),
            streakDays: Int = 0,
        ): String {
            val enc = { s: String -> URLEncoder.encode(s, "UTF-8") }
            val base = "workout_summary/${enc(time)}/${enc(distance)}/${enc(pace)}/${enc(steps)}"
            val params = buildString {
                append("?lapCount=$lapCount")
                bestLapTime?.let { append("&bestLapTime=${enc(it)}") }
                bestLapNumber?.let { append("&bestLapNumber=$it") }
                trendLabel?.let { append("&trendLabel=${enc(it)}") }
                workoutId?.let { append("&workoutId=${enc(it)}") }
                append("&xpEarned=$xpEarned")
                if (achievements.isNotEmpty()) {
                    append("&achievements=${enc(achievements.joinToString("|"))}")
                }
                append("&streakDays=$streakDays")
            }
            return base + params
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
                onStartWorkout = { activityType ->
                    navController.navigate(Screen.Workout.createRoute(activityType))
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                },
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.Detail.createRoute(workoutId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onCharacterClick = {
                    navController.navigate(Screen.CharacterSheet.route)
                },
                onAnalyticsClick = {
                    navController.navigate(Screen.Analytics.route)
                },
            )
        }
        composable(Screen.Analytics.route) {
            AnalyticsHubScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onRouteMapClick = {
                    navController.navigate(Screen.RouteOverlay.route)
                },
            )
        }
        composable(Screen.RouteOverlay.route) {
            RouteOverlayScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
        composable(Screen.CharacterSheet.route) {
            CharacterSheetScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            route = Screen.Workout.route,
            arguments = listOf(
                navArgument("activityType") { type = NavType.StringType; defaultValue = "RUN" },
            ),
        ) { backStackEntry ->
            val activityTypeName = backStackEntry.arguments?.getString("activityType") ?: "RUN"
            val activityType = try { ActivityType.valueOf(activityTypeName) } catch (e: Exception) { ActivityType.RUN }

            ActiveWorkoutScreen(
                activityType = activityType,
                onWorkoutComplete = { stats, workoutId ->
                    if (activityType == ActivityType.DOG_WALK && workoutId != null) {
                        navController.navigate(Screen.DogWalkSummary.createRoute(workoutId)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    } else {
                        navController.navigate(
                            Screen.WorkoutSummary.createRoute(
                                time = stats.time,
                                distance = stats.distance,
                                pace = stats.pace,
                                steps = stats.steps,
                                lapCount = stats.lapCount,
                                bestLapTime = stats.bestLapTime,
                                bestLapNumber = stats.bestLapNumber,
                                trendLabel = stats.trendLabel,
                                workoutId = workoutId,
                                xpEarned = stats.xpEarned,
                                achievements = stats.achievementNames,
                                streakDays = stats.streakDays,
                            )
                        ) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
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
                navArgument("steps") { type = NavType.StringType },
                navArgument("lapCount") { type = NavType.StringType; defaultValue = "0" },
                navArgument("bestLapTime") { type = NavType.StringType; defaultValue = "" },
                navArgument("bestLapNumber") { type = NavType.StringType; defaultValue = "" },
                navArgument("trendLabel") { type = NavType.StringType; defaultValue = "" },
                navArgument("workoutId") { type = NavType.StringType; defaultValue = "" },
                navArgument("xpEarned") { type = NavType.StringType; defaultValue = "0" },
                navArgument("achievements") { type = NavType.StringType; defaultValue = "" },
                navArgument("streakDays") { type = NavType.StringType; defaultValue = "0" },
            ),
        ) { backStackEntry ->
            val dec = { key: String ->
                URLDecoder.decode(
                    backStackEntry.arguments?.getString(key) ?: "",
                    "UTF-8",
                )
            }
            val lapCount = dec("lapCount").toIntOrNull() ?: 0
            val bestLapTime = dec("bestLapTime").ifEmpty { null }
            val bestLapNumber = dec("bestLapNumber").toIntOrNull()
            val trendLabel = dec("trendLabel").ifEmpty { null }
            val workoutId = dec("workoutId").ifEmpty { null }
            val xpEarned = dec("xpEarned").toIntOrNull() ?: 0
            val achievements = dec("achievements").ifEmpty { null }
                ?.split("|")
                ?.filter { it.isNotEmpty() }
                ?: emptyList()
            val streakDays = dec("streakDays").toIntOrNull() ?: 0

            WorkoutSummaryScreen(
                time = dec("time"),
                distance = dec("distance"),
                pace = dec("pace"),
                steps = dec("steps"),
                lapCount = lapCount,
                bestLapTime = bestLapTime,
                bestLapNumber = bestLapNumber,
                trendLabel = trendLabel,
                workoutId = workoutId,
                xpEarned = xpEarned,
                achievements = achievements,
                streakDays = streakDays,
                onViewDetails = {
                    workoutId?.let { id ->
                        navController.navigate(Screen.Detail.createRoute(id)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                },
                onDone = {
                    navController.popBackStack(
                        route = Screen.Home.route,
                        inclusive = false,
                    )
                },
            )
        }
        composable(
            route = Screen.DogWalkSummary.route,
            arguments = listOf(
                navArgument("workoutId") { type = NavType.StringType },
            ),
        ) {
            DogWalkSummaryScreen(
                onSaved = { workoutId ->
                    navController.navigate(Screen.Detail.createRoute(workoutId)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onDiscarded = {
                    navController.popBackStack(route = Screen.Home.route, inclusive = false)
                },
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.Detail.createRoute(workoutId))
                },
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("workoutId") { type = NavType.StringType },
            ),
        ) {
            DetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
    }
}
