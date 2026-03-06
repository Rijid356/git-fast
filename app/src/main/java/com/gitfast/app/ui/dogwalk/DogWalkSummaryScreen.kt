package com.gitfast.app.ui.dogwalk

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.ui.theme.AmberAccent

@Composable
fun DogWalkSummaryScreen(
    onSaved: (workoutId: String) -> Unit,
    onDiscarded: () -> Unit,
    viewModel: DogWalkSummaryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { viewModel.addPhoto(it) }
    }

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
                .padding(top = 56.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            val isDogRun = uiState.activityType == ActivityType.DOG_RUN
            Text(
                text = if (isDogRun) "DOG RUN COMPLETE" else "DOG WALK COMPLETE",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isDogRun) AmberAccent else MaterialTheme.colorScheme.secondary,
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

            // Sprint summary (if any sprints detected)
            if (uiState.sprintCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SPRINT SUMMARY",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        StatRow(label = "SPRINTS", value = uiState.sprintCount.toString())
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.totalSprintTimeFormatted?.let {
                            StatRow(label = "TOTAL TIME", value = it)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        uiState.longestSprintTimeFormatted?.let {
                            StatRow(label = "LONGEST", value = it)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        uiState.avgSprintTimeFormatted?.let {
                            StatRow(label = "AVERAGE", value = it)
                        }
                    }
                }

                // Training tip
                Spacer(modifier = Modifier.height(8.dp))
                val longestSeconds = uiState.longestSprintTimeFormatted
                val tip = when {
                    longestSeconds == "00:00" -> null
                    uiState.sprintCount == 0 -> "Try running intervals with Juniper — start with 30 seconds!"
                    // Parse rough seconds from longest sprint for tip logic
                    else -> {
                        // Simple heuristic based on sprint count and longest time
                        val parts = (uiState.longestSprintTimeFormatted ?: "0:00").split(":")
                        val approxSec = (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 +
                            (parts.getOrNull(1)?.toIntOrNull() ?: 0)
                        when {
                            approxSec < 30 -> "Great start! Gradually build up to 1-minute runs."
                            approxSec < 60 -> "Nice progress! Use 'With me' for position training."
                            approxSec < 120 -> "Impressive endurance! Keep at 3 dog runs per week."
                            else -> "Juniper's becoming a runner!"
                        }
                    }
                }
                tip?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Event section (narrative + map + timeline)
            if (uiState.events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                // Narrative card
                uiState.narrative?.let { narrative ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Text(
                            text = narrative,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                // Event route map
                if (uiState.gpsPoints.size >= 2) {
                    Spacer(modifier = Modifier.height(12.dp))
                    EventRouteMap(
                        gpsPoints = uiState.gpsPoints,
                        events = uiState.events,
                    )
                }

                // Event timeline
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EVENT LOG",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        EventTimeline(
                            events = uiState.events,
                            walkStartTimeMillis = uiState.walkStartTimeMillis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Route tag
            RouteTagSelector(
                tags = uiState.routeTags,
                selectedTag = uiState.selectedRouteTag,
                isAutoDetected = uiState.isRouteAutoDetected,
                onSelectTag = { viewModel.selectRouteTag(it) },
                onConfirmNewTag = { viewModel.confirmNewTag(it) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Weather
            WeatherCard(
                weatherData = uiState.weatherData,
                isEditing = uiState.isWeatherEditing,
                selectedCondition = uiState.weatherCondition,
                selectedTemp = uiState.weatherTemp,
                onToggleEdit = { viewModel.toggleWeatherEdit() },
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

            // Photos
            PhotoGrid(
                photos = uiState.photos,
                onAddPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemovePhoto = { viewModel.removePhoto(it) },
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
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text(
                    text = when {
                        uiState.isSaving -> "SAVING..."
                        uiState.activityType == ActivityType.DOG_RUN -> "SAVE DOG RUN"
                        else -> "SAVE DOG WALK"
                    },
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
