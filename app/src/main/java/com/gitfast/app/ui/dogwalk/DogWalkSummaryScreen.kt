package com.gitfast.app.ui.dogwalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DogWalkSummaryScreen(
    onSaved: (workoutId: String) -> Unit,
    onDiscarded: () -> Unit,
    viewModel: DogWalkSummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved(viewModel.workoutId)
    }
    LaunchedEffect(uiState.isDiscarded) {
        if (uiState.isDiscarded) onDiscarded()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Text(
                text = "WALK COMPLETE",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats summary
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StatRow(label = "TIME", value = uiState.timeFormatted)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "DISTANCE", value = uiState.distanceFormatted)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatRow(label = "AVG PACE", value = uiState.paceFormatted)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dog name (always Juniper)
            SectionLabel("Dog")
            Text(
                text = "Juniper",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Route tag
            RouteTagSelector(
                tags = uiState.routeTags,
                selectedTag = uiState.selectedRouteTag,
                isCreatingNew = uiState.isCreatingNewTag,
                newTagName = uiState.newTagName,
                onSelectTag = { viewModel.selectRouteTag(it) },
                onStartCreatingNew = { viewModel.startCreatingNewTag() },
                onUpdateNewTagName = { viewModel.updateNewTagName(it) },
                onConfirmNewTag = { viewModel.confirmNewTag() },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Weather
            WeatherSelector(
                selectedCondition = uiState.weatherCondition,
                selectedTemp = uiState.weatherTemp,
                onConditionSelected = { viewModel.selectWeatherCondition(it) },
                onTempSelected = { viewModel.selectWeatherTemp(it) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Energy
            EnergySelector(
                selectedLevel = uiState.energyLevel,
                onLevelSelected = { viewModel.selectEnergyLevel(it) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            SectionLabel("Notes")
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Button(
                onClick = { viewModel.saveWalk() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = if (uiState.isSaving) "SAVING..." else "SAVE WALK",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { showDiscardDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "DISCARD",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard walk?") },
            text = { Text("This will delete all data from this walk.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.discardWalk()
                }) {
                    Text("DISCARD", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("CANCEL")
                }
            },
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
    )
}
