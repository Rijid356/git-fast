package com.gitfast.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.gitfast.app.ui.components.GitFastTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.data.healthconnect.HealthConnectManager

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.healthConnectManager.getPermissionContract(),
    ) { granted ->
        viewModel.onHealthConnectPermissionResult(granted)
    }

    Scaffold(
        topBar = {
            GitFastTopAppBar(title = "Settings", onBackClick = onBackClick)
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
                subtitle = "Auto-mark laps within 5m of your start/finish line",
                checked = uiState.autoLapEnabled,
                onCheckedChange = { viewModel.setAutoLapEnabled(it) },
            )

            if (uiState.autoLapEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SetLapStartPointItem(
                    hasLapStartPoint = uiState.hasLapStartPoint,
                    isCapturing = uiState.isCapturingLapStartPoint,
                    onCapture = { viewModel.captureLapStartPoint() },
                    onClear = { viewModel.clearLapStartPoint() },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Location section ---
            SectionHeader(text = "Location")

            SwitchSettingItem(
                title = "Home Arrival",
                subtitle = "Auto-pause when you arrive home (~50ft)",
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

            Spacer(modifier = Modifier.height(24.dp))

            // --- Display section ---
            SectionHeader(text = "Display")

            SwitchSettingItem(
                title = "Keep Screen On",
                subtitle = "Prevent screen from dimming during workouts",
                checked = uiState.keepScreenOn,
                onCheckedChange = { viewModel.setKeepScreenOn(it) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Cloud Backup section ---
            SectionHeader(text = "Cloud Backup")

            CloudBackupSection(
                isSignedIn = uiState.isSignedIn,
                userEmail = uiState.userEmail,
                syncStatus = uiState.syncStatus,
                lastSyncedAt = uiState.lastSyncedAt,
                isSyncing = uiState.isSyncing,
                signInError = uiState.signInError,
                onSignIn = { viewModel.signIn(context) },
                onSignOut = { viewModel.signOut() },
                onSyncNow = { viewModel.syncNow() },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Health Connect section ---
            SectionHeader(text = "Health Connect")

            HealthConnectSection(
                isAvailable = uiState.healthConnectAvailable,
                isConnected = uiState.healthConnectConnected,
                isSyncing = uiState.healthConnectSyncing,
                lastSyncedAt = uiState.healthConnectLastSync,
                latestWeight = uiState.latestWeight,
                latestWeightDate = uiState.latestWeightDate,
                onConnect = {
                    healthConnectPermissionLauncher.launch(
                        HealthConnectManager.PERMISSIONS
                    )
                },
                onSyncNow = { viewModel.syncHealthConnect() },
            )

            Spacer(modifier = Modifier.height(24.dp))

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
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
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
            Spacer(modifier = Modifier.height(4.dp))
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
            Spacer(modifier = Modifier.height(4.dp))
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
private fun SetLapStartPointItem(
    hasLapStartPoint: Boolean,
    isCapturing: Boolean,
    onCapture: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCapturing) {
                if (hasLapStartPoint) onClear() else onCapture()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (hasLapStartPoint) "Lap Start Point" else "Set Lap Start Point",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (hasLapStartPoint) "Tap to clear"
                else "Tap to capture GPS start/finish line",
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
                text = if (hasLapStartPoint) "Set" else "Not set",
                style = MaterialTheme.typography.bodyLarge,
                color = if (hasLapStartPoint) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
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
