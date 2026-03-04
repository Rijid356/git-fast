package com.gitfast.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gitfast.app.R
import kotlin.math.roundToInt

@Composable
fun BoxScope.ScreenshotOverlay(
    onCaptureRequest: () -> Unit,
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val buttonSizePx = with(density) { 40.dp.toPx() }

    var offsetX by remember { mutableFloatStateOf(screenWidthPx - buttonSizePx - with(density) { 16.dp.toPx() }) }
    var offsetY by remember { mutableFloatStateOf(screenHeightPx - buttonSizePx - with(density) { 80.dp.toPx() }) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(40.dp)
            .alpha(0.6f)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, screenWidthPx - buttonSizePx)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, screenHeightPx - buttonSizePx)
                    },
                )
            }
            .clickable {
                if (!isDragging) onCaptureRequest()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camera_pixel),
            contentDescription = "Screenshot",
            tint = Color.Black,
            modifier = Modifier.size(20.dp),
        )
    }
}
