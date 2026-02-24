package com.gitfast.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HealthConnectSection(
    isAvailable: Boolean,
    isConnected: Boolean,
    isSyncing: Boolean,
    lastSyncedAt: Long,
    latestWeight: String?,
    latestWeightDate: String?,
    onConnect: () -> Unit,
    onSyncNow: () -> Unit,
) {
    // Status row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Status",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = when {
                    !isAvailable -> "Health Connect is not installed"
                    isConnected -> "Reading weight & body composition"
                    else -> "Tap Connect to grant permissions"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Text(
            text = when {
                !isAvailable -> "Not Available"
                isConnected -> "Connected"
                else -> "Not Connected"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                !isAvailable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                isConnected -> MaterialTheme.colorScheme.primary
                else -> Color(0xFFF85149) // ErrorRed
            },
        )
    }

    if (!isAvailable) return

    // Connect button (only if not connected)
    if (!isConnected) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Connect",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Grant permission to read health data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("Connect")
            }
        }
        return
    }

    // Sync Now row (only if connected)
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSyncing) { onSyncNow() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Sync Now",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = formatHcSyncTime(lastSyncedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = if (lastSyncedAt > 0) "Synced" else "Never",
                style = MaterialTheme.typography.bodyLarge,
                color = if (lastSyncedAt > 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
    }

    // Latest reading (only if we have data)
    if (latestWeight != null) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Latest Reading",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = latestWeightDate ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Text(
                text = latestWeight,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun formatHcSyncTime(timestamp: Long): String {
    if (timestamp <= 0) return "Never synced"
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Last synced: just now"
        minutes < 60 -> "Last synced: ${minutes}m ago"
        hours < 24 -> "Last synced: ${hours}h ago"
        else -> "Last synced: ${days}d ago"
    }
}
