package com.gitfast.app.ui.analytics.trends

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitfast.app.ui.theme.NeonGreen
import com.gitfast.app.ui.theme.NeonGreenDim

@Composable
fun PixelBarChart(
    bars: List<ChartBar>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)
    val valueStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val maxValue = remember(bars) {
        bars.maxOfOrNull { it.value }?.coerceAtLeast(0.01f) ?: 1f
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            if (bars.isEmpty()) return@Canvas

            val topPadding = 16.dp.toPx()
            val bottomPadding = 20.dp.toPx()
            val barSpacing = 4.dp.toPx()
            val availableWidth = size.width
            val barWidth = (availableWidth - barSpacing * (bars.size - 1)) / bars.size
            val chartHeight = size.height - topPadding - bottomPadding

            bars.forEachIndexed { index, bar ->
                val x = index * (barWidth + barSpacing)
                val barColor = if (bar.isCurrent) NeonGreen else NeonGreenDim

                // Bar height
                val barHeight = if (bar.value > 0f) {
                    (bar.value / maxValue) * chartHeight
                } else {
                    2.dp.toPx() // thin line for zero
                }

                val barTop = topPadding + (chartHeight - barHeight)

                // Draw bar
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, barTop),
                    size = Size(barWidth, barHeight),
                )

                // Value label above bar
                val valueMeasured = textMeasurer.measure(
                    text = bar.displayValue,
                    style = TextStyle(
                        fontSize = 7.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
                drawText(
                    textLayoutResult = valueMeasured,
                    color = labelColor,
                    topLeft = Offset(
                        x + (barWidth - valueMeasured.size.width) / 2,
                        barTop - valueMeasured.size.height - 2.dp.toPx(),
                    ),
                )

                // Period label below bar
                val labelMeasured = textMeasurer.measure(
                    text = bar.label,
                    style = TextStyle(
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center,
                    ),
                )
                drawText(
                    textLayoutResult = labelMeasured,
                    color = labelColor,
                    topLeft = Offset(
                        x + (barWidth - labelMeasured.size.width) / 2,
                        size.height - bottomPadding + 4.dp.toPx(),
                    ),
                )
            }
        }
    }
}
