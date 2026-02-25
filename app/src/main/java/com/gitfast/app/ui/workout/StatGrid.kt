package com.gitfast.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StatGrid(
    elapsedTimeFormatted: String,
    distanceFormatted: String,
    averagePaceFormatted: String?,
    stepCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "TIME",
                    value = elapsedTimeFormatted,
                    modifier = Modifier.weight(1f),
                )
                StatItem(
                    label = "DISTANCE",
                    value = distanceFormatted,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "AVG PACE",
                    value = averagePaceFormatted ?: "-- /mi",
                    modifier = Modifier.weight(1f),
                )
                StatItem(
                    label = "STEPS",
                    value = stepCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun LapStatGrid(
    elapsedTimeFormatted: String,
    distanceFormatted: String,
    lapCount: Int,
    averageLapTimeFormatted: String?,
    modifier: Modifier = Modifier,
    ghostLapTimeFormatted: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "TOTAL TIME",
                    value = elapsedTimeFormatted,
                    modifier = Modifier.weight(1f),
                )
                StatItem(
                    label = "TOTAL DIST",
                    value = distanceFormatted,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "LAPS",
                    value = lapCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                if (ghostLapTimeFormatted != null) {
                    StatItem(
                        label = "GHOST",
                        value = ghostLapTimeFormatted,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    StatItem(
                        label = "AVG LAP",
                        value = averageLapTimeFormatted ?: "--:--",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun CooldownStatGrid(
    elapsedTimeFormatted: String,
    distanceFormatted: String,
    lapCount: Int,
    bestLapTimeFormatted: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "TIME",
                    value = elapsedTimeFormatted,
                    modifier = Modifier.weight(1f),
                )
                StatItem(
                    label = "DISTANCE",
                    value = distanceFormatted,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "LAPS",
                    value = lapCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatItem(
                    label = "BEST LAP",
                    value = bestLapTimeFormatted ?: "--:--",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun SprintStatRow(
    sprintCount: Int,
    totalSprintTimeFormatted: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                label = "SPRINTS",
                value = sprintCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatItem(
                label = "SPRINT TIME",
                value = totalSprintTimeFormatted,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}
