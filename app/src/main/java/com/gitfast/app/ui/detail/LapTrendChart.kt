package com.gitfast.app.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun LapTrendChart(
    points: List<LapChartPoint>,
    averageSeconds: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val avgLineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fastColor = MaterialTheme.colorScheme.secondary
    val slowColor = MaterialTheme.colorScheme.tertiary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val durations = points.map { it.durationSeconds }
            val minDuration = durations.min() - 5
            val maxDuration = durations.max() + 5
            val range = (maxDuration - minDuration).coerceAtLeast(1)

            val stepX = size.width / (points.size - 1).coerceAtLeast(1)

            fun xForLap(index: Int): Float = index * stepX
            fun yForDuration(seconds: Int): Float {
                val normalized = 1f - (seconds - minDuration).toFloat() / range
                return normalized * size.height
            }

            // Draw average line (dashed)
            val avgY = yForDuration(averageSeconds)
            drawLine(
                color = avgLineColor,
                start = Offset(0f, avgY),
                end = Offset(size.width, avgY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(8.dp.toPx(), 4.dp.toPx())
                )
            )

            // Draw connecting lines
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = primaryColor,
                    start = Offset(xForLap(i), yForDuration(durations[i])),
                    end = Offset(xForLap(i + 1), yForDuration(durations[i + 1])),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw points with color coding
            points.forEachIndexed { index, point ->
                val x = xForLap(index)
                val y = yForDuration(point.durationSeconds)
                val isBest = point.durationSeconds == durations.min()
                val isWorst = point.durationSeconds == durations.max()

                val dotColor = when {
                    isBest -> fastColor
                    isWorst -> slowColor
                    else -> primaryColor
                }

                drawCircle(color = dotColor, radius = 6.dp.toPx(), center = Offset(x, y))
                drawCircle(color = dotColor, radius = 4.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}
