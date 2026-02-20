package com.gitfast.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
                subtitle = "Auto-mark laps when you return to your start/finish line",
                checked = uiState.autoLapEnabled,
                onCheckedChange = { viewModel.setAutoLapEnabled(it) },
            )

            if (uiState.autoLapEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                AutoLapRadiusItem(
                    selectedMeters = uiState.autoLapAnchorRadiusMeters,
                    onSelect = { viewModel.setAutoLapAnchorRadius(it) },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            DistanceUnitItem(
                selected = uiState.distanceUnit,
                onSelect = { viewModel.setDistanceUnit(it) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Location section ---
            SectionHeader(text = "Location")

            SwitchSettingItem(
                title = "Home Arrival",
                subtitle = "Auto-pause when you arrive home",
                checked = uiState.homeArrivalEnabled,
                onCheckedChange = { viewModel.setHomeArrivalEnabled(it) },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SetHomeLocationItem(
                hasHomeLocation = uiState.hasHomeLocation,
                isCapturing = uiState.isCapturingLocation,
                onCapture = { viewModel.captureCurrentLocation() },
                onClear = { viewModel.clearHomeLocation() },
            )

            if (uiState.hasHomeLocation && uiState.homeArrivalEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                HomeArrivalRadiusItem(
                    selectedMeters = uiState.homeArrivalRadiusMeters,
                    onSelect = { viewModel.setHomeArrivalRadius(it) },
                )
            }

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
private fun AutoLapRadiusItem(
    selectedMeters: Int,
    onSelect: (Int) -> Unit,
) {
    val options = listOf(10 to "10m", 15 to "15m", 20 to "20m", 25 to "25m")
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
                text = "Anchor Radius",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Tap to cycle: 10m, 15m, 20m, 25m",
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
private fun SetHomeLocationItem(
    hasHomeLocation: Boolean,
    isCapturing: Boolean,
    onCapture: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCapturing) {
                if (hasHomeLocation) onClear() else onCapture()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (hasHomeLocation) "Home Location" else "Set Home Location",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = if (hasHomeLocation) "Tap to clear" else "Tap to capture current GPS position",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        if (isCapturing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = if (hasHomeLocation) "Set" else "Not set",
                style = MaterialTheme.typography.bodyLarge,
                color = if (hasHomeLocation) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun HomeArrivalRadiusItem(
    selectedMeters: Int,
    onSelect: (Int) -> Unit,
) {
    val options = listOf(15 to "15m", 30 to "30m", 50 to "50m", 75 to "75m")
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
                text = "Home Radius",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Tap to cycle: 15m, 30m, 50m, 75m",
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
