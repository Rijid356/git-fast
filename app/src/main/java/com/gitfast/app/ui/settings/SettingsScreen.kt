package com.gitfast.app.ui.settings

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
import androidx.compose.material3.Switch
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            // --- Workout section ---
            SectionHeader(text = "Workout")

            SwitchSettingItem(
                title = "Auto-Pause",
                subtitle = "Automatically pause runs when you stop moving",
                checked = uiState.autoPauseEnabled,
                onCheckedChange = { viewModel.setAutoPauseEnabled(it) },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SwitchSettingItem(
                title = "Auto Lap",
                subtitle = "Automatically mark laps at a set distance",
                checked = uiState.autoLapEnabled,
                onCheckedChange = { viewModel.setAutoLapEnabled(it) },
            )

            if (uiState.autoLapEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                AutoLapDistanceItem(
                    selectedMeters = uiState.autoLapDistanceMeters,
                    onSelect = { viewModel.setAutoLapDistanceMeters(it) },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            DistanceUnitItem(
                selected = uiState.distanceUnit,
                onSelect = { viewModel.setDistanceUnit(it) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Display section ---
            SectionHeader(text = "Display")

            SwitchSettingItem(
                title = "Keep Screen On",
                subtitle = "Prevent screen from dimming during workouts",
                checked = uiState.keepScreenOn,
                onCheckedChange = { viewModel.setKeepScreenOn(it) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- About section ---
            SectionHeader(text = "About")

            AboutItem(title = "App", value = "git-fast")
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
private fun SwitchSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun DistanceUnitItem(
    selected: DistanceUnit,
    onSelect: (DistanceUnit) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val next = if (selected == DistanceUnit.MILES) DistanceUnit.KILOMETERS else DistanceUnit.MILES
                onSelect(next)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Distance Unit",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Tap to toggle between miles and kilometers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Text(
            text = when (selected) {
                DistanceUnit.MILES -> "Miles"
                DistanceUnit.KILOMETERS -> "Kilometers"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun AutoLapDistanceItem(
    selectedMeters: Int,
    onSelect: (Int) -> Unit,
) {
    val options = listOf(200 to "200m", 400 to "400m", 800 to "800m", 1000 to "1 km", 1609 to "1 mile")
    val currentLabel = options.find { it.first == selectedMeters }?.second ?: "${selectedMeters}m"
    val nextIndex = (options.indexOfFirst { it.first == selectedMeters } + 1) % options.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(options[nextIndex].first) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Auto Lap Distance",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Tap to cycle: 200m, 400m, 800m, 1km, 1mi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Text(
            text = currentLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun AboutItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}
