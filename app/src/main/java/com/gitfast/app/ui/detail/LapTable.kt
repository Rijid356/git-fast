package com.gitfast.app.ui.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LapTable(
    laps: List<LapDisplayItem>,
    onDeleteLap: ((String) -> Unit)? = null,
) {
    var lapToDelete by remember { mutableStateOf<LapDisplayItem?>(null) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            LapTableHeader()
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            laps.forEach { lap ->
                LapTableRow(
                    lap = lap,
                    onLongPress = if (onDeleteLap != null) {
                        { lapToDelete = lap }
                    } else null,
                )
                if (lap != laps.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }

    lapToDelete?.let { lap ->
        DeleteLapDialog(
            lapNumber = lap.lapNumber,
            onConfirm = {
                onDeleteLap?.invoke(lap.id)
                lapToDelete = null
            },
            onDismiss = { lapToDelete = null },
        )
    }
}

@Composable
private fun LapTableHeader() {
    val headerColor = MaterialTheme.colorScheme.onSurfaceVariant
    val headerStyle = MaterialTheme.typography.labelSmall

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("#", style = headerStyle, color = headerColor, modifier = Modifier.width(32.dp))
        Text("TIME", style = headerStyle, color = headerColor, modifier = Modifier.weight(1f))
        Text("DIST", style = headerStyle, color = headerColor, modifier = Modifier.weight(1f))
        Text("DELTA", style = headerStyle, color = headerColor, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LapTableRow(
    lap: LapDisplayItem,
    onLongPress: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { mod ->
                if (onLongPress != null) {
                    mod.combinedClickable(
                        onClick = {},
                        onLongClick = onLongPress,
                    )
                } else mod
            }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lap number
        Text(
            text = "${lap.lapNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )

        // Time with fastest/slowest indicator
        Row(modifier = Modifier.weight(1f)) {
            Text(
                text = lap.timeFormatted,
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    lap.isFastest -> MaterialTheme.colorScheme.secondary
                    lap.isSlowest -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (lap.isFastest) Text(" ★", color = MaterialTheme.colorScheme.secondary)
            if (lap.isSlowest) Text(" ▾", color = MaterialTheme.colorScheme.tertiary)
        }

        // Distance
        Text(
            text = lap.distanceFormatted,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Delta
        Text(
            text = lap.deltaFormatted ?: "--",
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                lap.deltaSeconds == null -> MaterialTheme.colorScheme.onSurfaceVariant
                lap.deltaSeconds < 0 -> MaterialTheme.colorScheme.secondary
                lap.deltaSeconds > 0 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DeleteLapDialog(
    lapNumber: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete lap $lapNumber?")
        },
        text = {
            Text(text = "This action cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "DELETE",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCEL")
            }
        },
    )
}
