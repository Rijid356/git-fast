package com.gitfast.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WorkoutContent(
    uiState: WorkoutUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
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
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isPaused) {
                PausedDisplay(
                    elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                )
            } else {
                PaceDisplay(
                    currentPaceFormatted = uiState.currentPaceFormatted,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            StatGrid(
                elapsedTimeFormatted = uiState.elapsedTimeFormatted,
                distanceFormatted = uiState.distanceFormatted,
                averagePaceFormatted = uiState.averagePaceFormatted,
                gpsPointCount = uiState.gpsPointCount,
            )

            Spacer(modifier = Modifier.height(24.dp))

            WorkoutControls(
                isActive = uiState.isActive,
                isPaused = uiState.isPaused,
                onStart = onStart,
                onPause = onPause,
                onResume = onResume,
                onStop = onStop,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
