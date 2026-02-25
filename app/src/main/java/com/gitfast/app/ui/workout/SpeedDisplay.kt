package com.gitfast.app.ui.workout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpeedDisplay(
    currentSpeedFormatted: String?,
    currentPaceFormatted: String?,
    modifier: Modifier = Modifier,
) {
    // Split "7.7 MPH" into number and unit to avoid overlap at large font sizes
    val speedValue = currentSpeedFormatted?.replace(" MPH", "") ?: "--"
    val hasSpeed = currentSpeedFormatted != null

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = speedValue,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
            color = if (hasSpeed) Color.White else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "MPH",
            style = MaterialTheme.typography.headlineMedium,
            color = if (hasSpeed) Color.White else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = currentPaceFormatted ?: "-- /mi",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
