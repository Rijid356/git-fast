package com.gitfast.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LapTable(laps: List<LapDisplayItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            LapTableHeader()
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            laps.forEach { lap ->
                LapTableRow(lap = lap)
                if (lap != laps.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
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

@Composable
private fun LapTableRow(lap: LapDisplayItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
