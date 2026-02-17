package com.gitfast.app.ui.workout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.ui.components.KeepScreenOn

@Composable
fun ActiveWorkoutScreen(
    onWorkoutComplete: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()

    var showBackConfirmation by remember { mutableStateOf(false) }

    // Bind/unbind service
    DisposableEffect(Unit) {
        viewModel.bindService()
        onDispose {
            viewModel.unbindService()
        }
    }

    // Detect workout completion
    LaunchedEffect(uiState.isWorkoutComplete) {
        if (uiState.isWorkoutComplete) {
            onWorkoutComplete()
        }
    }

    // Back handler during active workout
    BackHandler(enabled = uiState.isActive) {
        showBackConfirmation = true
    }

    // Keep screen on during workout
    if (uiState.isActive) {
        KeepScreenOn()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (!permissionState.canTrackWorkout) {
            PermissionRequestContent(
                permissionState = permissionState,
                onPermissionsChanged = { viewModel.refreshPermissions() },
            )
        } else {
            WorkoutContent(
                uiState = uiState,
                onStart = { viewModel.startWorkout() },
                onPause = { viewModel.pauseWorkout() },
                onResume = { viewModel.resumeWorkout() },
                onStop = { viewModel.stopWorkout() },
            )
        }
    }

    if (showBackConfirmation) {
        AlertDialog(
            onDismissRequest = { showBackConfirmation = false },
            title = { Text("Stop Workout?") },
            text = { Text("Going back will stop your current workout. Your progress will be saved.") },
            confirmButton = {
                TextButton(onClick = {
                    showBackConfirmation = false
                    viewModel.stopWorkout()
                }) {
                    Text(
                        text = "Stop",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirmation = false }) {
                    Text("Continue")
                }
            },
        )
    }
}
