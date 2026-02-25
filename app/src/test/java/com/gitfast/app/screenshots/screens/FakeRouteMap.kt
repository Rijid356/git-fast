package com.gitfast.app.screenshots.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gitfast.app.ui.theme.NeonGreen

/**
 * Canvas-based mock of RouteMap for screenshot tests.
 * Draws a single GPS route with start/end markers on a dark "map" background,
 * matching the look of the real RouteMap composable.
 */
@Composable
fun FakeRouteMap(
    modifier: Modifier = Modifier,
    mapHeight: Dp = 300.dp,
    routeColor: Color = NeonGreen,
    routePoints: List<Pair<Float, Float>> = defaultRoutePoints,
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = Color(0xFF1A2332)
    val parkFill = Color(0xFF0E2318)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(mapHeight),
        shape = RectangleShape,
        color = surfaceColor,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Park area background
            val parkPath = Path().apply {
                moveTo(w * 0.08f, h * 0.15f)
                lineTo(w * 0.92f, h * 0.10f)
                lineTo(w * 0.95f, h * 0.92f)
                lineTo(w * 0.05f, h * 0.95f)
                close()
            }
            drawPath(parkPath, parkFill)

            // Street grid
            for (i in 1..5) {
                val y = h * (i / 6f)
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }
            for (i in 1..4) {
                val x = w * (i / 5f)
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            }

            // Route polyline
            val path = Path().apply {
                routePoints.forEachIndexed { idx, (nx, ny) ->
                    val x = nx * w
                    val y = ny * h
                    if (idx == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(
                path,
                color = routeColor,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )

            // Start marker (green)
            val startX = routePoints.first().first * w
            val startY = routePoints.first().second * h
            drawCircle(Color(0xFF1B5E20), radius = 14f, center = Offset(startX, startY))
            drawCircle(Color(0xFF4CAF50), radius = 10f, center = Offset(startX, startY))
            drawCircle(Color.White, radius = 4f, center = Offset(startX, startY))

            // End marker (red)
            val endX = routePoints.last().first * w
            val endY = routePoints.last().second * h
            drawCircle(Color(0xFFB71C1C), radius = 14f, center = Offset(endX, endY))
            drawCircle(Color(0xFFF44336), radius = 10f, center = Offset(endX, endY))
            drawCircle(Color.White, radius = 4f, center = Offset(endX, endY))
        }
    }
}

// An out-and-back run route shape
private val defaultRoutePoints = listOf(
    0.15f to 0.85f,
    0.18f to 0.78f,
    0.22f to 0.70f,
    0.28f to 0.62f,
    0.35f to 0.54f,
    0.42f to 0.47f,
    0.50f to 0.40f,
    0.58f to 0.35f,
    0.65f to 0.30f,
    0.72f to 0.26f,
    0.78f to 0.22f,
    0.84f to 0.20f,
    0.88f to 0.22f,
    0.86f to 0.28f,
    0.80f to 0.34f,
    0.73f to 0.40f,
    0.66f to 0.46f,
    0.58f to 0.52f,
    0.50f to 0.57f,
    0.42f to 0.63f,
    0.35f to 0.68f,
    0.28f to 0.74f,
    0.22f to 0.80f,
    0.18f to 0.84f,
    0.15f to 0.85f,
)

// A loopy dog walk route shape
val dogWalkRoutePoints = listOf(
    0.20f to 0.80f,
    0.25f to 0.72f,
    0.32f to 0.65f,
    0.38f to 0.58f,
    0.45f to 0.52f,
    0.52f to 0.48f,
    0.60f to 0.45f,
    0.68f to 0.42f,
    0.74f to 0.40f,
    0.78f to 0.44f,
    0.75f to 0.50f,
    0.70f to 0.55f,
    0.64f to 0.58f,
    0.58f to 0.62f,
    0.52f to 0.58f,
    0.48f to 0.54f,
    0.44f to 0.60f,
    0.40f to 0.66f,
    0.35f to 0.72f,
    0.28f to 0.77f,
    0.22f to 0.80f,
    0.20f to 0.80f,
)
