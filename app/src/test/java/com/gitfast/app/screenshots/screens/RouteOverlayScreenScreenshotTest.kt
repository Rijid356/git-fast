package com.gitfast.app.screenshots.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.theme.CyanAccent
import com.gitfast.app.ui.theme.NeonGreen
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RouteOverlayScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen RouteOverlay`() {
        captureScreenshot("Screen_RouteOverlay") {
            RouteOverlayMock()
        }
    }
}

// Route trace colors matching the app's automatic palette
private val traceColors = listOf(
    Color(0xFF39FF14), // neon green (most recent)
    Color(0xFF58A6FF), // cyan
    Color(0xFFF0883E), // amber
    Color(0xFFBC8CFF), // purple
)

private data class MockTrace(
    val date: String,
    val duration: String,
    val distance: String,
    val color: Color,
    val points: List<Pair<Float, Float>>, // normalized 0..1 coords
)

private val mockTraces = listOf(
    MockTrace(
        date = "Feb 24, 2026",
        duration = "20:15",
        distance = "2.51 mi",
        color = traceColors[0],
        points = listOf(
            0.22f to 0.82f, 0.25f to 0.72f, 0.30f to 0.63f, 0.38f to 0.55f,
            0.45f to 0.48f, 0.52f to 0.42f, 0.58f to 0.38f, 0.65f to 0.35f,
            0.72f to 0.30f, 0.78f to 0.28f, 0.82f to 0.32f, 0.80f to 0.40f,
            0.75f to 0.48f, 0.68f to 0.55f, 0.60f to 0.60f, 0.52f to 0.65f,
            0.45f to 0.70f, 0.38f to 0.75f, 0.30f to 0.78f, 0.22f to 0.82f,
        ),
    ),
    MockTrace(
        date = "Feb 18, 2026",
        duration = "20:57",
        distance = "2.51 mi",
        color = traceColors[1],
        points = listOf(
            0.20f to 0.80f, 0.24f to 0.70f, 0.28f to 0.61f, 0.35f to 0.53f,
            0.42f to 0.46f, 0.50f to 0.40f, 0.56f to 0.36f, 0.63f to 0.33f,
            0.70f to 0.29f, 0.76f to 0.26f, 0.80f to 0.30f, 0.78f to 0.38f,
            0.73f to 0.46f, 0.66f to 0.53f, 0.58f to 0.58f, 0.50f to 0.63f,
            0.43f to 0.68f, 0.36f to 0.73f, 0.28f to 0.77f, 0.20f to 0.80f,
        ),
    ),
    MockTrace(
        date = "Feb 10, 2026",
        duration = "19:42",
        distance = "2.51 mi",
        color = traceColors[2],
        points = listOf(
            0.24f to 0.84f, 0.27f to 0.74f, 0.32f to 0.65f, 0.40f to 0.57f,
            0.47f to 0.50f, 0.54f to 0.44f, 0.60f to 0.40f, 0.67f to 0.37f,
            0.74f to 0.32f, 0.80f to 0.30f, 0.84f to 0.34f, 0.82f to 0.42f,
            0.77f to 0.50f, 0.70f to 0.57f, 0.62f to 0.62f, 0.54f to 0.67f,
            0.47f to 0.72f, 0.40f to 0.77f, 0.32f to 0.80f, 0.24f to 0.84f,
        ),
    ),
    MockTrace(
        date = "Feb 3, 2026",
        duration = "22:10",
        distance = "2.51 mi",
        color = traceColors[3],
        points = listOf(
            0.18f to 0.78f, 0.22f to 0.68f, 0.26f to 0.59f, 0.33f to 0.51f,
            0.40f to 0.44f, 0.48f to 0.38f, 0.54f to 0.34f, 0.61f to 0.31f,
            0.68f to 0.27f, 0.74f to 0.24f, 0.78f to 0.28f, 0.76f to 0.36f,
            0.71f to 0.44f, 0.64f to 0.51f, 0.56f to 0.56f, 0.48f to 0.61f,
            0.41f to 0.66f, 0.34f to 0.71f, 0.26f to 0.75f, 0.18f to 0.78f,
        ),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteOverlayMock() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ROUTE MAP",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Route tag indicator (simulates the dropdown showing "Park Loop")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = "Park Loop",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp),
                )
            }

            // Canvas "map" with route overlays
            FakeMapCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legend (mirrors TraceLegend from the real screen)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                mockTraces.forEach { trace ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(trace.color),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = trace.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = trace.duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = trace.distance,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FakeMapCanvas(modifier: Modifier = Modifier) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = Color(0xFF1A2332) // subtle street grid
    val parkFill = Color(0xFF0E2318) // dark green park area

    Surface(
        modifier = modifier,
        shape = RectangleShape,
        color = surfaceColor,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Park area (rough polygon behind the routes)
            val parkPath = Path().apply {
                moveTo(w * 0.15f, h * 0.20f)
                lineTo(w * 0.90f, h * 0.15f)
                lineTo(w * 0.92f, h * 0.90f)
                lineTo(w * 0.10f, h * 0.92f)
                close()
            }
            drawPath(parkPath, parkFill)

            // Street grid (horizontal)
            for (i in 1..6) {
                val y = h * (i / 7f)
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }
            // Street grid (vertical)
            for (i in 1..5) {
                val x = w * (i / 6f)
                drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            }

            // Draw route traces (oldest first so newest is on top)
            mockTraces.reversed().forEach { trace ->
                val path = Path().apply {
                    trace.points.forEachIndexed { idx, (nx, ny) ->
                        val x = nx * w
                        val y = ny * h
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path,
                    color = trace.color.copy(alpha = 0.7f),
                    style = Stroke(
                        width = 6f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }

            // Start/end marker (small circle where routes begin)
            val startX = mockTraces[0].points.first().first * w
            val startY = mockTraces[0].points.first().second * h
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = Offset(startX, startY),
            )
            drawCircle(
                color = NeonGreen,
                radius = 5f,
                center = Offset(startX, startY),
            )
        }
    }
}
