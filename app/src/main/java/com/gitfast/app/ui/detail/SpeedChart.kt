package com.gitfast.app.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SpeedChartPoint(
    val elapsedMinutes: Float,
    val speedMph: Float,
)

@Composable
fun SpeedChart(
    points: List<SpeedChartPoint>,
    averageSpeedMph: Float,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val avgLineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val maxColor = MaterialTheme.colorScheme.secondary
    val textStyle = TextStyle(
        fontSize = 8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val textMeasurer = rememberTextMeasurer()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        ) {
            if (points.size < 2) return@Canvas

            val speeds = points.map { it.speedMph }
            val maxSpeed = speeds.max()
            val minSpeed = (speeds.min() - 0.5f).coerceAtLeast(0f)
            val range = (maxSpeed - minSpeed).coerceAtLeast(0.5f)

            val maxTime = points.last().elapsedMinutes
            val minTime = points.first().elapsedMinutes
            val timeRange = (maxTime - minTime).coerceAtLeast(0.1f)

            fun xForTime(minutes: Float): Float {
                return ((minutes - minTime) / timeRange) * size.width
            }

            fun yForSpeed(mph: Float): Float {
                val normalized = 1f - (mph - minSpeed) / range
                return normalized * size.height
            }

            // Draw average line (dashed)
            val avgY = yForSpeed(averageSpeedMph)
            drawLine(
                color = avgLineColor,
                start = Offset(0f, avgY),
                end = Offset(size.width, avgY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(8.dp.toPx(), 4.dp.toPx())
                ),
            )

            // Draw avg label
            val avgLabel = "%.1f".format(averageSpeedMph)
            val avgTextResult = textMeasurer.measure(avgLabel, textStyle)
            drawText(
                textLayoutResult = avgTextResult,
                topLeft = Offset(
                    -avgTextResult.size.width.toFloat() - 4.dp.toPx(),
                    avgY - avgTextResult.size.height / 2f,
                ),
            )

            // Draw connecting lines
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = primaryColor,
                    start = Offset(xForTime(points[i].elapsedMinutes), yForSpeed(speeds[i])),
                    end = Offset(xForTime(points[i + 1].elapsedMinutes), yForSpeed(speeds[i + 1])),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            // Draw max speed point
            val maxIndex = speeds.indexOf(maxSpeed)
            if (maxIndex >= 0) {
                val x = xForTime(points[maxIndex].elapsedMinutes)
                val y = yForSpeed(maxSpeed)
                drawCircle(color = maxColor, radius = 6.dp.toPx(), center = Offset(x, y))
                drawCircle(color = maxColor, radius = 4.dp.toPx(), center = Offset(x, y))
            }

            // Draw X-axis time labels (every 5 minutes)
            val intervalMinutes = when {
                maxTime <= 5f -> 1f
                maxTime <= 15f -> 5f
                maxTime <= 30f -> 5f
                else -> 10f
            }
            var tick = 0f
            while (tick <= maxTime) {
                val x = xForTime(tick)
                val label = "${tick.toInt()}"
                val textResult = textMeasurer.measure(label, textStyle)
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x - textResult.size.width / 2f,
                        size.height + 4.dp.toPx(),
                    ),
                )
                tick += intervalMinutes
            }
        }
    }
}
