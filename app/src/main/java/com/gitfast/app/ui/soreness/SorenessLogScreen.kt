package com.gitfast.app.ui.soreness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.gitfast.app.ui.components.GitFastTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.ErrorRed
import com.gitfast.app.ui.theme.NeonGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SorenessLogScreen(
    onBackClick: () -> Unit,
    viewModel: SorenessLogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val hasLoggedToday = state.todayLog != null && !state.isEditing
    val canSave = state.selectedMuscles.isNotEmpty() && state.selectedIntensity != null && !state.isSaving

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GitFastTopAppBar(title = "> Soreness Check-In", onBackClick = onBackClick)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (hasLoggedToday) {
                TodaySummary(state = state, onEdit = { viewModel.startEditing() })
            } else {
                SorenessForm(
                    state = state,
                    onToggleMuscle = viewModel::toggleMuscle,
                    onSelectIntensity = viewModel::selectIntensity,
                    onUpdateNotes = viewModel::updateNotes,
                    onSave = viewModel::saveSoreness,
                    onCancel = if (state.isEditing) viewModel::cancelEditing else null,
                    canSave = canSave,
                )
            }

            // XP earned toast
            state.xpEarned?.let { xp ->
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .padding(16.dp),
                ) {
                    Text(
                        text = "+$xp XP earned!",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonGreen,
                    )
                    state.achievementNames.forEach { name ->
                        Text(
                            text = "Achievement unlocked: $name",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberAccent,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodaySummary(
    state: SorenessLogUiState,
    onEdit: () -> Unit,
) {
    val log = state.todayLog ?: return

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Today's Check-In",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Intensity: ${log.intensity.displayName}",
        style = MaterialTheme.typography.bodyLarge,
        color = intensityColor(log.intensity),
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Sore muscles:",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(4.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        log.muscleGroups.forEach { group ->
            FilterChip(
                selected = true,
                onClick = {},
                enabled = false,
                label = {
                    Text(
                        text = group.displayName,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                    selectedLabelColor = NeonGreen,
                    disabledSelectedContainerColor = NeonGreen.copy(alpha = 0.15f),
                ),
            )
        }
    }

    log.notes?.let { notes ->
        if (notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Notes: $notes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (log.xpAwarded > 0) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "+${log.xpAwarded} XP",
            style = MaterialTheme.typography.labelMedium,
            color = NeonGreen,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = onEdit) {
        Text(
            text = "EDIT",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SorenessForm(
    state: SorenessLogUiState,
    onToggleMuscle: (com.gitfast.app.data.model.MuscleGroup) -> Unit,
    onSelectIntensity: (SorenessIntensity) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: (() -> Unit)?,
    canSave: Boolean,
) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "What's sore?",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(12.dp))

    MuscleGroupSelector(
        selected = state.selectedMuscles,
        onToggle = onToggleMuscle,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "How sore?",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(12.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SorenessIntensity.entries.forEach { intensity ->
            val isSelected = state.selectedIntensity == intensity
            FilterChip(
                selected = isSelected,
                onClick = { onSelectIntensity(intensity) },
                label = {
                    Text(
                        text = intensity.displayName,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = intensityColor(intensity).copy(alpha = 0.2f),
                    selectedLabelColor = intensityColor(intensity),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Notes (optional)",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = state.notes,
        onValueChange = onUpdateNotes,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Any extra details...",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        maxLines = 3,
        shape = RectangleShape,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSave,
        enabled = canSave,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(
            text = if (state.isEditing) "UPDATE SORENESS" else "LOG SORENESS",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }

    if (onCancel != null) {
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "CANCEL",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun intensityColor(intensity: SorenessIntensity): androidx.compose.ui.graphics.Color {
    return when (intensity) {
        SorenessIntensity.MILD -> NeonGreen
        SorenessIntensity.MODERATE -> AmberAccent
        SorenessIntensity.SEVERE -> ErrorRed
    }
}
