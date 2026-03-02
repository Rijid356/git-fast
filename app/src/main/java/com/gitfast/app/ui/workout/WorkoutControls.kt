package com.gitfast.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.ui.theme.AmberAccent

@Composable
fun WorkoutControls(
    isActive: Boolean,
    isPaused: Boolean,
    phase: PhaseType,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit,
    onStartLaps: () -> Unit,
    onMarkLap: () -> Unit,
    onEndLaps: () -> Unit,
    modifier: Modifier = Modifier,
    isAutoPaused: Boolean = false,
    activityType: ActivityType = ActivityType.RUN,
) {
    var showStopConfirmation by remember { mutableStateOf(false) }
    val resumeButtonText = if (isAutoPaused) "AUTO-PAUSED" else "RESUME"

    Column(modifier = modifier.fillMaxWidth()) {
        if (!isActive) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (activityType) {
                        ActivityType.DOG_RUN -> AmberAccent
                        ActivityType.DOG_WALK -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    },
                    contentColor = when (activityType) {
                        ActivityType.DOG_RUN -> MaterialTheme.colorScheme.onTertiary
                        ActivityType.DOG_WALK -> MaterialTheme.colorScheme.onSecondary
                        else -> MaterialTheme.colorScheme.onPrimary
                    },
                ),
            ) {
                Text(
                    text = when (activityType) {
                        ActivityType.DOG_WALK -> "START DOG WALK"
                        ActivityType.DOG_RUN -> "START DOG RUN"
                        else -> "START RUN"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        } else {
            if (activityType.isDogActivity) {
                // Dog activity: simple Pause/Stop controls only
                PauseStopRow(
                    isPaused = isPaused,
                    resumeText = "RESUME",
                    onPause = onPause,
                    onResume = onResume,
                    secondaryText = "STOP",
                    onSecondary = { showStopConfirmation = true },
                )
            } else {
            when (phase) {
                PhaseType.WARMUP -> {
                    PauseStopRow(
                        isPaused = isPaused,
                        resumeText = resumeButtonText,
                        onPause = onPause,
                        onResume = onResume,
                        secondaryText = "STOP",
                        onSecondary = { showStopConfirmation = true },
                    )

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

                    PauseStopRow(
                        isPaused = isPaused,
                        resumeText = resumeButtonText,
                        onPause = onPause,
                        onResume = onResume,
                        secondaryText = "COOL DOWN",
                        onSecondary = onEndLaps,
                    )
                }

                PhaseType.COOLDOWN -> {
                    PauseStopRow(
                        isPaused = isPaused,
                        resumeText = resumeButtonText,
                        onPause = onPause,
                        onResume = onResume,
                        secondaryText = "STOP",
                        onSecondary = { showStopConfirmation = true },
                    )
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
private fun PauseStopRow(
    isPaused: Boolean,
    resumeText: String,
    onPause: () -> Unit,
    onResume: () -> Unit,
    secondaryText: String,
    onSecondary: () -> Unit,
) {
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
                    text = resumeText,
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
            onClick = onSecondary,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun StopConfirmationDialog(
    onSaveAndStop: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "STOP WORKOUT?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save & Stop — primary action, big and green
                Button(
                    onClick = onSaveAndStop,
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
                            text = "SAVE & STOP",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Discard — destructive, red
                Button(
                    onClick = onDiscard,
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
                            text = "DISCARD",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Keep Going — outline, cancel
                OutlinedButton(
                    onClick = onDismiss,
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
                            text = "KEEP GOING",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
