package com.gitfast.app.ui.workout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.NeonGreen

@Composable
fun LapPhaseContent(
    currentLapTimeFormatted: String,
    ghostLapTimeFormatted: String? = null,
    ghostDeltaSeconds: Int? = null,
    ghostDeltaFormatted: String? = null,
    autoLapAnchorSet: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (autoLapAnchorSet) {
            Text(
                text = "> START/FINISH LINE SET",
                style = MaterialTheme.typography.labelSmall,
                color = NeonGreen,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = "Current Lap",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = currentLapTimeFormatted,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
            color = androidx.compose.ui.graphics.Color.White,
            textAlign = TextAlign.Center,
        )

        if (ghostLapTimeFormatted != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ghost $ghostLapTimeFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (ghostDeltaFormatted != null && ghostDeltaSeconds != null) {
                    Spacer(modifier = Modifier.width(8.dp))

                    val deltaColor = when {
                        ghostDeltaSeconds < 0 -> NeonGreen
                        ghostDeltaSeconds > 0 -> AmberAccent
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Text(
                        text = ghostDeltaFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = deltaColor,
                    )
                }
            }
        }
    }
}
