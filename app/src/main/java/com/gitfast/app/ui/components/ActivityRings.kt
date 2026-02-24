package com.gitfast.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.DailyActivityMetrics
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.CyanAccent
import com.gitfast.app.ui.theme.NeonGreen

@Composable
fun ActivityRings(
    metrics: DailyActivityMetrics,
    onGoalsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedMinutes by animateFloatAsState(
        targetValue = metrics.activeMinutesProgress,
        animationSpec = tween(durationMillis = 800),
        label = "minutesProgress",
    )
    val animatedDistance by animateFloatAsState(
        targetValue = metrics.distanceProgress,
        animationSpec = tween(durationMillis = 800, delayMillis = 100),
        label = "distanceProgress",
    )
    val animatedDays by animateFloatAsState(
        targetValue = metrics.activeDaysProgress,
        animationSpec = tween(durationMillis = 800, delayMillis = 200),
        label = "daysProgress",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onGoalsClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp),
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val strokeWidth = 12.dp.toPx()
                val gap = 8.dp.toPx()

                // Outer ring — Active Minutes (NeonGreen)
                drawRing(
                    progress = animatedMinutes,
                    color = NeonGreen,
                    strokeWidth = strokeWidth,
                    inset = 0f,
                )

                // Middle ring — Distance (Cyan)
                drawRing(
                    progress = animatedDistance,
                    color = CyanAccent,
                    strokeWidth = strokeWidth,
                    inset = strokeWidth + gap,
                )

                // Inner ring — Active Days (Amber)
                drawRing(
                    progress = animatedDays,
                    color = AmberAccent,
                    strokeWidth = strokeWidth,
                    inset = (strokeWidth + gap) * 2,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RingLegendItem(
                color = NeonGreen,
                label = "MIN",
                value = "${metrics.activeMinutes}/${metrics.activeMinutesGoal}",
            )
            RingLegendItem(
                color = CyanAccent,
                label = "MI",
                value = "%.1f/%.1f".format(metrics.distanceMiles, metrics.distanceGoal),
            )
            RingLegendItem(
                color = AmberAccent,
                label = "DAYS",
                value = "${metrics.activeDaysThisWeek}/${metrics.activeDaysGoal}",
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "GOALS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
        )
    }
}

private fun DrawScope.drawRing(
    progress: Float,
    color: Color,
    strokeWidth: Float,
    inset: Float,
) {
    val topLeft = Offset(inset + strokeWidth / 2, inset + strokeWidth / 2)
    val arcSize = Size(
        size.width - inset * 2 - strokeWidth,
        size.height - inset * 2 - strokeWidth,
    )
    if (arcSize.width <= 0f || arcSize.height <= 0f) return

    val trackAlpha = 0.12f

    // Track (full circle, dim)
    drawArc(
        color = color.copy(alpha = trackAlpha),
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )

    // Progress arc
    val baseSweep = (progress.coerceAtMost(1f)) * 360f
    if (baseSweep > 0f) {
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = baseSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }

    // Overflow lap (brighter, on top)
    if (progress > 1f) {
        val overflowSweep = ((progress - 1f).coerceAtMost(1f)) * 360f
        drawArc(
            color = Color.White.copy(alpha = 0.5f),
            startAngle = -90f,
            sweepAngle = overflowSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun RingLegendItem(
    color: Color,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}
