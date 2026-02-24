package com.gitfast.app.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.data.model.DistanceUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: GoalsSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Goals") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // --- Daily Goals ---
            SectionHeader(text = "Daily Goals")

            SliderGoalItem(
                title = "Active Minutes",
                subtitle = "Minutes of activity per day",
                value = uiState.dailyActiveMinutesGoal,
                valueLabel = "${uiState.dailyActiveMinutesGoal} min",
                range = 5f..120f,
                steps = 22,
                onValueChange = { viewModel.setDailyActiveMinutesGoal(it.roundToInt()) },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            val distanceLabel = if (uiState.distanceUnit == DistanceUnit.KILOMETERS) {
                "%.1f km".format(uiState.dailyDistanceGoalMiles * 1.60934)
            } else {
                "%.1f mi".format(uiState.dailyDistanceGoalMiles)
            }

            SliderGoalItem(
                title = "Distance",
                subtitle = "Distance per day",
                value = (uiState.dailyDistanceGoalMiles * 2).roundToInt(),
                valueLabel = distanceLabel,
                range = 1f..40f,
                steps = 38,
                onValueChange = { viewModel.setDailyDistanceGoal(it / 2.0) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Weekly Goals ---
            SectionHeader(text = "Weekly Goals")

            ActiveDaysItem(
                selectedDays = uiState.weeklyActiveDaysGoal,
                onSelect = { viewModel.setWeeklyActiveDaysGoal(it) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- AHA Guidelines ---
            SectionHeader(text = "Guidelines")

            Text(
                text = "Based on AHA recommendations for adults:\n" +
                    "- 150 min/week moderate activity (~22 min/day)\n" +
                    "- Be active most days of the week\n" +
                    "- Reduce sedentary time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SliderGoalItem(
    title: String,
    subtitle: String,
    value: Int,
    valueLabel: String,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

@Composable
private fun ActiveDaysItem(
    selectedDays: Int,
    onSelect: (Int) -> Unit,
) {
    val options = (1..7).toList()
    val nextIndex = (options.indexOf(selectedDays) + 1) % options.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(options[nextIndex]) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Active Days",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Days with at least one workout per week. Tap to change.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Text(
            text = "$selectedDays days",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
