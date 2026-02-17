package com.gitfast.app.ui.workout

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.ActivityType

@Composable
fun RecordingIndicator(
    isPaused: Boolean,
    activityType: ActivityType = ActivityType.RUN,
    modifier: Modifier = Modifier,
) {
    if (isPaused) {
        Text(
            text = "\u23F8 PAUSE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = modifier.padding(8.dp),
        )
    } else if (activityType == ActivityType.DOG_WALK) {
        Text(
            text = "WALKING",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = modifier.padding(8.dp),
        )
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
        val dotAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "dotAlpha",
        )

        Row(
            modifier = modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "\u25CF",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error.copy(alpha = dotAlpha),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "REC",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
