package com.gitfast.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.PhaseType

@Composable
fun WorkoutContent(
    uiState: WorkoutUiState,
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
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isActive) {
            RecordingIndicator(
                isPaused = uiState.isPaused,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Phase label + lap indicator header
            if (uiState.isActive) {
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
            when (uiState.phase) {
                PhaseType.WARMUP -> {
                    StatGrid(
                        elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                        distanceFormatted = uiState.distanceFormatted,
                        averagePaceFormatted = uiState.averagePaceFormatted,
                        gpsPointCount = uiState.gpsPointCount,
                    )
                }
                PhaseType.LAPS -> {
                    LapStatGrid(
                        elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                        distanceFormatted = uiState.distanceFormatted,
                        lapCount = uiState.lapCount,
                        averageLapTimeFormatted = uiState.averageLapTimeFormatted,
                    )
                }
                PhaseType.COOLDOWN -> {
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
                phase = uiState.phase,
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
