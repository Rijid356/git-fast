package com.gitfast.app.ui.workout

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.gitfast.app.data.model.DogWalkEventType
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DogWalkEventWheel(
    eventCounts: Map<DogWalkEventType, Int>,
    onLogEvent: (DogWalkEventType) -> Unit,
    onUndoEvent: (DogWalkEventType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }
    val totalCount = eventCounts.values.sum()

    val expandProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "expandProgress",
    )

    val fabRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "fabRotation",
    )

    Box(modifier = modifier) {
        // FAB
        Box {
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        expanded = !expanded
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "\uD83D\uDC3E",
                        fontSize = 24.sp,
                        modifier = Modifier.rotate(fabRotation),
                    )
                }
            }

            // Total event count badge
            if (totalCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = totalCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }
        }

        // Expanded wheel — Popup centered directly on the FAB
        if (expanded || expandProgress > 0.01f) {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                ExpandedWheelOverlay(
                    eventCounts = eventCounts,
                    expandProgress = expandProgress,
                    onLogEvent = { eventType ->
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onLogEvent(eventType)
                    },
                    onUndoEvent = { eventType ->
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onUndoEvent(eventType)
                    },
                    onDismiss = { expanded = false },
                )
            }
        }
    }
}

/**
 * Full-screen overlay with scrim + radial wheel of event items.
 * Items orbit from the center of this composable — when used inside a
 * Popup(alignment = Center) anchored on the FAB, the center = the FAB.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandedWheelOverlay(
    eventCounts: Map<DogWalkEventType, Int>,
    expandProgress: Float,
    onLogEvent: (DogWalkEventType) -> Unit,
    onUndoEvent: (DogWalkEventType) -> Unit,
    onDismiss: () -> Unit = {},
    contentAlignment: Alignment = Alignment.Center,
) {
    val density = LocalDensity.current
    val events = DogWalkEventType.entries
    val n = events.size
    val radiusDp = 120.dp
    val radiusPx = with(density) { radiusDp.toPx() }
    val itemSizeDp = 48.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * expandProgress))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = contentAlignment,
    ) {
        events.forEachIndexed { index, eventType ->
            val count = eventCounts[eventType] ?: 0
            // Full circle: start from top (12 o'clock), go clockwise
            val angle = 2.0 * Math.PI * index / n - Math.PI / 2.0
            val x = radiusPx * cos(angle).toFloat()
            val y = radiusPx * sin(angle).toFloat()

            // Staggered animation per item
            val itemDelay = index * 0.04f
            val itemProgress = ((expandProgress - itemDelay) / (1f - itemDelay)).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (x * itemProgress).roundToInt(),
                            y = (y * itemProgress).roundToInt(),
                        )
                    }
                    .graphicsLayer {
                        scaleX = itemProgress
                        scaleY = itemProgress
                        alpha = itemProgress
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    Surface(
                        modifier = Modifier
                            .size(itemSizeDp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .combinedClickable(
                                onClick = { onLogEvent(eventType) },
                                onLongClick = {
                                    if (count > 0) {
                                        onUndoEvent(eventType)
                                    }
                                },
                            ),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = eventType.icon,
                                fontSize = 20.sp,
                            )
                        }
                    }

                    // Per-item count badge
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(20.dp)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                modifier = Modifier.size(20.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                }

                // Short label below the circle
                Text(
                    text = eventType.shortLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
