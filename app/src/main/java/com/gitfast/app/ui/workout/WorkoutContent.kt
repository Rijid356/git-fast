package com.gitfast.app.ui.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.util.formatElapsedTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutContent(
    uiState: WorkoutUiState,
    ghostSources: List<GhostSource> = emptyList(),
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit,
    onStartLaps: () -> Unit,
    onMarkLap: () -> Unit,
    onEndLaps: () -> Unit,
    onSelectGhost: (String?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isActive) {
            RecordingIndicator(
                isPaused = uiState.isPaused,
                activityType = uiState.activityType,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Phase label + lap indicator header
            if (uiState.isActive) {
                if (uiState.activityType == ActivityType.DOG_WALK) {
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = uiState.phaseLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )

                        if (uiState.phase == PhaseType.LAPS && uiState.lastLapTimeFormatted != null) {
                            LapIndicator(
                                lastLapTimeFormatted = uiState.lastLapTimeFormatted,
                                lastLapDeltaFormatted = uiState.lastLapDeltaFormatted,
                                lastLapDeltaSeconds = uiState.lastLapDeltaSeconds,
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Hero display - phase-specific
            when {
                uiState.isPaused -> {
                    PausedDisplay(
                        elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                    )
                }
                uiState.phase == PhaseType.LAPS && uiState.isActive -> {
                    LapPhaseContent(
                        currentLapTimeFormatted = uiState.currentLapTimeFormatted,
                        ghostLapTimeFormatted = uiState.ghostLapTimeFormatted,
                        ghostDeltaSeconds = uiState.ghostDeltaSeconds,
                        ghostDeltaFormatted = uiState.ghostDeltaFormatted,
                        autoLapAnchorSet = uiState.autoLapAnchorSet,
                    )
                }
                else -> {
                    PaceDisplay(
                        currentPaceFormatted = uiState.currentPaceFormatted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stat grid - phase-specific
            when {
                uiState.activityType == ActivityType.DOG_WALK -> {
                    StatGrid(
                        elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                        distanceFormatted = uiState.distanceFormatted,
                        averagePaceFormatted = uiState.averagePaceFormatted,
                        gpsPointCount = uiState.gpsPointCount,
                    )
                }
                uiState.phase == PhaseType.WARMUP -> {
                    Column {
                        StatGrid(
                            elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                            distanceFormatted = uiState.distanceFormatted,
                            averagePaceFormatted = uiState.averagePaceFormatted,
                            gpsPointCount = uiState.gpsPointCount,
                        )
                        if (ghostSources.isNotEmpty() && uiState.isActive && uiState.activityType == ActivityType.RUN) {
                            Spacer(modifier = Modifier.height(12.dp))
                            GhostSelector(
                                ghostSources = ghostSources,
                                selectedGhostTime = uiState.ghostLapTimeFormatted,
                                onSelectGhost = onSelectGhost,
                            )
                        }
                    }
                }
                uiState.phase == PhaseType.LAPS -> {
                    LapStatGrid(
                        elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                        distanceFormatted = uiState.distanceFormatted,
                        lapCount = uiState.lapCount,
                        averageLapTimeFormatted = uiState.averageLapTimeFormatted,
                        ghostLapTimeFormatted = uiState.ghostLapTimeFormatted,
                    )
                }
                uiState.phase == PhaseType.COOLDOWN -> {
                    CooldownStatGrid(
                        elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                        distanceFormatted = uiState.distanceFormatted,
                        lapCount = uiState.lapCount,
                        bestLapTimeFormatted = uiState.bestLapTimeFormatted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            WorkoutControls(
                isActive = uiState.isActive,
                isPaused = uiState.isPaused,
                isAutoPaused = uiState.isAutoPaused,
                phase = uiState.phase,
                activityType = uiState.activityType,
                onStart = onStart,
                onPause = onPause,
                onResume = onResume,
                onStop = onStop,
                onDiscard = onDiscard,
                onStartLaps = onStartLaps,
                onMarkLap = onMarkLap,
                onEndLaps = onEndLaps,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GhostSelector(
    ghostSources: List<GhostSource>,
    selectedGhostTime: String?,
    onSelectGhost: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Ghost",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = selectedGhostTime ?: "Auto (best lap)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Auto (best lap)") },
                onClick = {
                    onSelectGhost(null)
                    expanded = false
                },
            )
            ghostSources.forEach { source ->
                val dateStr = source.date
                    .atZone(ZoneId.systemDefault())
                    .format(dateFormatter)
                val lapTime = formatElapsedTime(source.bestLapSeconds)
                DropdownMenuItem(
                    text = { Text("$dateStr - $lapTime (${source.lapCount} laps)") },
                    onClick = {
                        onSelectGhost(source.workoutId)
                        expanded = false
                    },
                )
            }
        }
    }
}
