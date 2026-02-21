package com.gitfast.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.sync.SyncStatus

@Composable
fun CloudBackupSection(
    isSignedIn: Boolean,
    userEmail: String?,
    syncStatus: SyncStatus,
    lastSyncedAt: Long,
    isSyncing: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
) {
    if (!isSignedIn) {
        // Sign-in prompt
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Back up your data to the cloud with Google",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Button(
                onClick = onSignIn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("Sign In")
            }
        }
        return
    }

    // Signed-in state
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = userEmail ?: "Signed in",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }

    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

    // Sync status
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
                text = formatSyncStatus(syncStatus, lastSyncedAt),
                style = MaterialTheme.typography.bodySmall,
                color = when (syncStatus) {
                    is SyncStatus.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
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

    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

    // Sign out
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSignOut() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Sign Out",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

private fun formatSyncStatus(syncStatus: SyncStatus, lastSyncedAt: Long): String {
    return when (syncStatus) {
        is SyncStatus.Syncing -> "Syncing..."
        is SyncStatus.Error -> "Error: ${syncStatus.message}"
        is SyncStatus.Success -> formatTimeSince(syncStatus.timestamp)
        is SyncStatus.Idle -> {
            if (lastSyncedAt > 0) formatTimeSince(lastSyncedAt)
            else "Never synced"
        }
    }
}

private fun formatTimeSince(timestamp: Long): String {
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
