package com.gitfast.app.ui.soreness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.DarkSurfaceVariant
import com.gitfast.app.ui.theme.ErrorRed
import com.gitfast.app.ui.theme.NeonGreen
import com.gitfast.app.ui.theme.OutlineGray

private val UnselectedColor = DarkSurfaceVariant
private val HeadColor = OutlineGray

@Composable
fun BodyMapCanvas(
    muscleIntensities: Map<MuscleGroup, SorenessIntensity>,
    showingFront: Boolean,
    onZoneTap: (MuscleGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val zones = remember(showingFront) { if (showingFront) FRONT_ZONES else BACK_ZONES }

    Canvas(
        modifier = modifier
            .pointerInput(showingFront) {
                detectTapGestures { tapOffset ->
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    // Find which zone was tapped (check in reverse for z-order)
                    for (zone in zones.reversed()) {
                        val scaledRect = scaleRect(zone.relativeRect, canvasWidth, canvasHeight)
                        if (scaledRect.contains(tapOffset)) {
                            onZoneTap(zone.muscleGroup)
                            break
                        }
                    }
                }
            },
    ) {
        val w = size.width
        val h = size.height

        // Draw decorative head circle
        drawCircle(
            color = HeadColor,
            radius = w * 0.06f,
            center = Offset(w * 0.5f, h * 0.05f),
            style = Stroke(width = 2f),
        )

        // Draw body outline (simplified silhouette)
        drawBodyOutline(w, h)

        // Draw zones
        for (zone in zones) {
            val intensity = muscleIntensities[zone.muscleGroup]
            val fillColor = intensityFillColor(intensity)
            val scaledRect = scaleRect(zone.relativeRect, w, h)

            // Filled zone rect
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(scaledRect.left, scaledRect.top),
                size = Size(scaledRect.width, scaledRect.height),
                cornerRadius = CornerRadius(4f, 4f),
            )

            // Zone border
            drawRoundRect(
                color = if (intensity != null) fillColor.copy(alpha = 0.8f) else OutlineGray,
                topLeft = Offset(scaledRect.left, scaledRect.top),
                size = Size(scaledRect.width, scaledRect.height),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 1.5f),
            )

            // Zone label
            drawZoneLabel(textMeasurer, zone.label, scaledRect, intensity)
        }
    }
}

private fun DrawScope.drawBodyOutline(w: Float, h: Float) {
    // Neck
    drawLine(
        color = OutlineGray,
        start = Offset(w * 0.45f, h * 0.08f),
        end = Offset(w * 0.45f, h * 0.10f),
        strokeWidth = 1.5f,
    )
    drawLine(
        color = OutlineGray,
        start = Offset(w * 0.55f, h * 0.08f),
        end = Offset(w * 0.55f, h * 0.10f),
        strokeWidth = 1.5f,
    )
}

private fun DrawScope.drawZoneLabel(
    textMeasurer: TextMeasurer,
    label: String,
    rect: Rect,
    intensity: SorenessIntensity?,
) {
    val labelColor = when {
        intensity != null -> Color.White
        else -> OutlineGray.copy(alpha = 0.7f)
    }
    val fontSize = (rect.width / 5f).coerceIn(8f, 14f).sp
    val style = TextStyle(
        color = labelColor,
        fontSize = fontSize,
        textAlign = TextAlign.Center,
    )
    val layoutResult = textMeasurer.measure(label, style)
    val x = rect.left + (rect.width - layoutResult.size.width) / 2
    val y = rect.top + (rect.height - layoutResult.size.height) / 2
    drawText(layoutResult, topLeft = Offset(x, y))
}

private fun intensityFillColor(intensity: SorenessIntensity?): Color {
    return when (intensity) {
        SorenessIntensity.MILD -> NeonGreen.copy(alpha = 0.35f)
        SorenessIntensity.MODERATE -> AmberAccent.copy(alpha = 0.40f)
        SorenessIntensity.SEVERE -> ErrorRed.copy(alpha = 0.45f)
        null -> UnselectedColor.copy(alpha = 0.3f)
    }
}

private fun scaleRect(relative: Rect, canvasWidth: Float, canvasHeight: Float): Rect {
    return Rect(
        left = relative.left * canvasWidth,
        top = relative.top * canvasHeight,
        right = relative.right * canvasWidth,
        bottom = relative.bottom * canvasHeight,
    )
}

internal fun intensityColor(intensity: SorenessIntensity): Color {
    return when (intensity) {
        SorenessIntensity.MILD -> NeonGreen
        SorenessIntensity.MODERATE -> AmberAccent
        SorenessIntensity.SEVERE -> ErrorRed
    }
}
