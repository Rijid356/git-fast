package com.gitfast.app.ui.workout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.ui.components.KeepScreenOn

@Composable
fun ActiveWorkoutScreen(
    activityType: ActivityType = ActivityType.RUN,
    onWorkoutComplete: (stats: WorkoutSummaryStats, workoutId: String?) -> Unit,
    onWorkoutDiscarded: () -> Unit,
    onNavigateHome: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()
    val ghostSources by viewModel.ghostSources.collectAsStateWithLifecycle()

    var showBackConfirmation by remember { mutableStateOf(false) }

    // Set activity type on viewModel
    LaunchedEffect(activityType) {
        viewModel.setActivityType(activityType)
    }

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
            onWorkoutComplete(viewModel.lastSummaryStats, viewModel.lastWorkoutId)
        }
    }

    // Detect workout discard
    LaunchedEffect(uiState.isDiscarded) {
        if (uiState.isDiscarded) {
            onWorkoutDiscarded()
        }
    }

    // Back handler during active workout
    BackHandler(enabled = uiState.isActive) {
        showBackConfirmation = true
    }

    // Keep screen on during workout (respects settings)
    if (uiState.isActive && uiState.keepScreenOn) {
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
                ghostSources = ghostSources,
                onStart = { viewModel.startWorkout() },
                onPause = { viewModel.pauseWorkout() },
                onResume = { viewModel.resumeWorkout() },
                onStop = { viewModel.stopWorkout() },
                onDiscard = { viewModel.discardWorkout() },
                onStartLaps = { viewModel.startLaps() },
                onMarkLap = { viewModel.markLap() },
                onEndLaps = { viewModel.endLaps() },
                onSelectGhost = { viewModel.selectGhost(it) },
                dogWalkEventCounts = uiState.dogWalkEventCounts,
                onLogEvent = { viewModel.logEvent(it) },
                onUndoEvent = { viewModel.undoEvent(it) },
            )
        }
    }

    if (showBackConfirmation) {
        Dialog(onDismissRequest = { showBackConfirmation = false }) {
            Surface(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "WORKOUT IN PROGRESS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your workout will keep\nrunning in the background.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Go Home — primary action, green
                    Button(
                        onClick = {
                            showBackConfirmation = false
                            onNavigateHome()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "GO HOME",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stop Workout — destructive, red
                    Button(
                        onClick = {
                            showBackConfirmation = false
                            viewModel.stopWorkout()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "STOP WORKOUT",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cancel — outline
                    OutlinedButton(
                        onClick = { showBackConfirmation = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "CANCEL",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}
