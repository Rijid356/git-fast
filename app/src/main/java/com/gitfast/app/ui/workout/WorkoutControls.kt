package com.gitfast.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType

@Composable
fun WorkoutControls(
    isActive: Boolean,
    isPaused: Boolean,
    isAutoPaused: Boolean = false,
    phase: PhaseType,
    activityType: ActivityType = ActivityType.RUN,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit,
    onStartLaps: () -> Unit,
    onMarkLap: () -> Unit,
    onEndLaps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStopConfirmation by remember { mutableStateOf(false) }
    val resumeButtonText = if (isAutoPaused) "AUTO-PAUSED" else "RESUME"

    Column(modifier = modifier.fillMaxWidth()) {
        if (!isActive) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = if (activityType == ActivityType.DOG_WALK) "START WALK" else "START RUN",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        } else {
            if (activityType == ActivityType.DOG_WALK) {
                // Dog walk: simple Pause/Stop controls only
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isPaused) {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary,
                            ),
                        ) {
                            Text("RESUME", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    } else {
                        Button(
                            onClick = onPause,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary,
                            ),
                        ) {
                            Text("PAUSE", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    Button(
                        onClick = { showStopConfirmation = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    ) {
                        Text("STOP", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            } else {
            when (phase) {
                PhaseType.WARMUP -> {
                    // Pause/Stop row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (isPaused) {
                            Button(
                                onClick = onResume,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                ),
                            ) {
                                Text(
                                    text = resumeButtonText,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        } else {
                            Button(
                                onClick = onPause,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary,
                                ),
                            ) {
                                Text(
                                    text = "PAUSE",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        }

                        Button(
                            onClick = { showStopConfirmation = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Text(
                                text = "STOP",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // START LAPS button below
                    Button(
                        onClick = onStartLaps,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = "START LAPS",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }

                PhaseType.LAPS -> {
                    // LAP button (large, prominent) above the row
                    Button(
                        onClick = onMarkLap,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = "LAP",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pause/End Laps row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (isPaused) {
                            Button(
                                onClick = onResume,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                ),
                            ) {
                                Text(
                                    text = resumeButtonText,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        } else {
                            Button(
                                onClick = onPause,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary,
                                ),
                            ) {
                                Text(
                                    text = "PAUSE",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        }

                        Button(
                            onClick = onEndLaps,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Text(
                                text = "COOL DOWN",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                }

                PhaseType.COOLDOWN -> {
                    // Standard pause/stop (same as original)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (isPaused) {
                            Button(
                                onClick = onResume,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                ),
                            ) {
                                Text(
                                    text = resumeButtonText,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        } else {
                            Button(
                                onClick = onPause,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary,
                                ),
                            ) {
                                Text(
                                    text = "PAUSE",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        }

                        Button(
                            onClick = { showStopConfirmation = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Text(
                                text = "STOP",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                }
            }
            }
        }
    }

    if (showStopConfirmation) {
        StopConfirmationDialog(
            onSaveAndStop = {
                showStopConfirmation = false
                onStop()
            },
            onDiscard = {
                showStopConfirmation = false
                onDiscard()
            },
            onDismiss = { showStopConfirmation = false },
        )
    }
}

@Composable
private fun StopConfirmationDialog(
    onSaveAndStop: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Stop Workout?")
        },
        text = {
            Text(text = "Save your workout or discard it?")
        },
        confirmButton = {
            TextButton(
                onClick = onSaveAndStop,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("SAVE & STOP")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDiscard,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("DISCARD")
                }
                TextButton(onClick = onDismiss) {
                    Text("KEEP GOING")
                }
            }
        },
    )
}
