package com.gitfast.app.ui.workout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.NeonGreen

@Composable
fun LapIndicator(
    lastLapTimeFormatted: String,
    lastLapDeltaFormatted: String?,
    lastLapDeltaSeconds: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = lastLapTimeFormatted,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (lastLapDeltaFormatted != null && lastLapDeltaSeconds != null) {
            Spacer(modifier = Modifier.width(8.dp))

            val deltaColor = when {
                lastLapDeltaSeconds < 0 -> NeonGreen
                lastLapDeltaSeconds > 0 -> AmberAccent
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Text(
                text = lastLapDeltaFormatted,
                style = MaterialTheme.typography.bodyMedium,
                color = deltaColor,
            )
        }
    }
}
