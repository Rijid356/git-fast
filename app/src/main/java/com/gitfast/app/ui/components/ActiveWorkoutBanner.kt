package com.gitfast.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.gitfast.app.service.WorkoutTrackingState
import com.gitfast.app.ui.theme.NeonGreen
import com.gitfast.app.util.formatDistance
import com.gitfast.app.util.formatElapsedTime

@Composable
fun ActiveWorkoutBanner(
    workoutState: WorkoutTrackingState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPaused = workoutState.isPaused || workoutState.isAutoPaused || workoutState.isHomeArrivalPaused

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NeonGreen.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Pulsing dot or pause indicator
        if (isPaused) {
            Text(
                text = "\u23F8",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        } else {
            val infiniteTransition = rememberInfiniteTransition(label = "banner_pulse")
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bannerDotAlpha",
            )
            Text(
                text = "\u25CF",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error.copy(alpha = dotAlpha),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Activity label
        val label = when {
            isPaused -> "PAUSED"
            workoutState.activityType == ActivityType.DOG_WALK -> "DOG WALK"
            workoutState.activityType == ActivityType.DOG_RUN -> "DOG RUN"
            else -> "RUNNING"
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = NeonGreen,
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Elapsed time
        Text(
            text = formatElapsedTime(workoutState.elapsedSeconds),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Distance
        Text(
            text = formatDistance(workoutState.distanceMeters),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Return indicator
        Text(
            text = "RETURN >",
            style = MaterialTheme.typography.labelMedium,
            color = NeonGreen,
        )
    }
}
