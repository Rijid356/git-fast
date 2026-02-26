package com.gitfast.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.analysis.RouteComparisonAnalyzer
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when ((uiState as? DetailUiState.Loaded)?.detail?.activityType) {
                            ActivityType.DOG_WALK -> "Dog Walk"
                            ActivityType.DOG_RUN -> "Dog Run"
                            else -> "Run Details"
                        },
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (uiState is DetailUiState.Loaded) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete workout",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            is DetailUiState.NotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Workout not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            is DetailUiState.Deleted -> {
                LaunchedEffect(Unit) {
                    onBackClick()
                }
            }

            is DetailUiState.Loaded -> {
                DetailContent(
                    detail = state.detail,
                    phases = state.phases,
                    lapAnalysis = state.lapAnalysis,
                    routeComparison = state.routeComparison,
                    speedChartPoints = state.speedChartPoints,
                    averageSpeedMph = state.averageSpeedMph,
                    maxSpeedMph = state.maxSpeedMph,
                    sprintLaps = state.sprintLaps,
                    dogWalkEvents = state.dogWalkEvents,
                    onDeleteLap = viewModel::deleteLap,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.deleteWorkout()
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }
}

@Composable
private fun DetailContent(
    detail: WorkoutDetailItem,
    phases: List<com.gitfast.app.util.PhaseAnalyzer.PhaseDisplayItem>,
    lapAnalysis: LapAnalysis?,
    routeComparison: List<RouteComparisonAnalyzer.RouteComparisonItem>,
    speedChartPoints: List<SpeedChartPoint> = emptyList(),
    averageSpeedMph: Float = 0f,
    maxSpeedMph: Float = 0f,
    sprintLaps: List<com.gitfast.app.data.model.Lap> = emptyList(),
    dogWalkEvents: List<com.gitfast.app.data.model.DogWalkEvent> = emptyList(),
    modifier: Modifier = Modifier,
    onDeleteLap: (String) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Date header
        DateHeader(
            dateFormatted = detail.dateFormatted,
            timeFormatted = detail.timeFormatted,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats card
        DetailStatsCard(detail = detail)

        Spacer(modifier = Modifier.height(24.dp))

        // XP earned section
        if (detail.xpEarned > 0) {
            XpEarnedSection(
                xpEarned = detail.xpEarned,
                xpBreakdown = detail.xpBreakdown,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Dog activity metadata (for dog walks/runs)
        if (detail.activityType.isDogActivity) {
            DogWalkMetadataSection(detail = detail)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Sprint analysis (for dog activities with sprints)
        if (detail.activityType.isDogActivity && sprintLaps.isNotEmpty()) {
            SprintAnalysisSection(sprintLaps = sprintLaps)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Dog walk event log
        if (detail.activityType.isDogActivity && dogWalkEvents.isNotEmpty()) {
            DogWalkEventSection(events = dogWalkEvents, walkStartTimeMillis = detail.startTimeMillis)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Phase breakdown (only for runs with multiple phases)
        if (detail.activityType == ActivityType.RUN) {
            PhaseBreakdownSection(phases = phases)
            if (phases.size > 1) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            lapAnalysis?.let {
                LapAnalysisSection(analysis = it, onDeleteLap = onDeleteLap)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Speed over time chart (for any workout with speed data)
        if (speedChartPoints.size >= 2) {
            SpeedOverTimeSection(
                points = speedChartPoints,
                averageSpeedMph = averageSpeedMph,
                maxSpeedMph = maxSpeedMph,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Route map or no-route placeholder
        if (detail.routePoints.size >= 2) {
            RouteMap(
                points = detail.routePoints,
                bounds = detail.routeBounds,
            )
        } else {
            NoRouteContent()
        }

        // Route comparison (only for dog activities with route tag)
        if (detail.activityType.isDogActivity && routeComparison.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            RouteComparisonSection(items = routeComparison)
        }

        // Notes (only for dog walks with notes)
        detail.notes?.let { notes ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // GPS quality footer
        if (detail.gpsPointCount > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            GpsQualityFooter(
                pointCount = detail.gpsPointCount,
                avgAccuracy = detail.avgGpsAccuracy,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DateHeader(
    dateFormatted: String,
    timeFormatted: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateFormatted,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = timeFormatted,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailStatsCard(detail: WorkoutDetailItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "WORKOUT STATS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatCell(
                    label = "DISTANCE",
                    value = detail.distanceFormatted,
                    modifier = Modifier.weight(1f),
                )
                StatCell(
                    label = "TIME",
                    value = detail.durationFormatted,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatCell(
                    label = "AVG PACE",
                    value = detail.avgPaceFormatted,
                    modifier = Modifier.weight(1f),
                )
                StatCell(
                    label = "STEPS",
                    value = detail.stepsFormatted,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun XpEarnedSection(
    xpEarned: Int,
    xpBreakdown: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "XP EARNED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "+$xpEarned XP",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            xpBreakdown?.let { breakdown ->
                Spacer(modifier = Modifier.height(12.dp))
                val entries = breakdown.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                entries.forEachIndexed { index, entry ->
                    Text(
                        text = entry,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (index < entries.lastIndex) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NoRouteContent() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "No route data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GpsQualityFooter(
    pointCount: Int,
    avgAccuracy: Float?,
) {
    val accuracyText = avgAccuracy?.let { String.format(java.util.Locale.US, "%.0fm avg accuracy", it) } ?: ""
    val text = buildString {
        append("$pointCount GPS points")
        if (accuracyText.isNotEmpty()) {
            append(" \u00B7 $accuracyText")
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DogWalkMetadataSection(detail: WorkoutDetailItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            detail.dogName?.let {
                MetadataRow(icon = "\uD83D\uDC15", text = it)
            }
            detail.routeTag?.let {
                MetadataRow(icon = "\uD83D\uDCCD", text = it)
            }
            detail.weatherSummary?.let {
                MetadataRow(icon = "\uD83C\uDF24", text = it)
            }
            detail.energyLevel?.let {
                MetadataRow(icon = "\u26A1", text = "${it.name.lowercase().replaceFirstChar { c -> c.uppercase() }} energy")
            }
        }
    }
}

@Composable
private fun MetadataRow(icon: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = icon, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SpeedOverTimeSection(
    points: List<SpeedChartPoint>,
    averageSpeedMph: Float,
    maxSpeedMph: Float,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SPEED OVER TIME",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            SpeedChart(
                points = points,
                averageSpeedMph = averageSpeedMph,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatCell(
                    label = "AVG SPEED",
                    value = "%.1f MPH".format(averageSpeedMph),
                    modifier = Modifier.weight(1f),
                )
                StatCell(
                    label = "MAX SPEED",
                    value = "%.1f MPH".format(maxSpeedMph),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete workout?")
        },
        text = {
            Text(text = "This action cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "DELETE",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCEL")
            }
        },
    )
}

@Composable
private fun SprintAnalysisSection(
    sprintLaps: List<com.gitfast.app.data.model.Lap>,
) {
    val durations = sprintLaps.mapNotNull { it.durationMillis?.let { d -> (d / 1000).toInt() } }
    if (durations.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SPRINT ANALYSIS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Summary stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(text = "${durations.size}", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(text = "SPRINTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(text = com.gitfast.app.util.formatElapsedTime(durations.sum()), style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(text = "TOTAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(text = com.gitfast.app.util.formatElapsedTime(durations.max()), style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(text = "LONGEST", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Individual sprint list
            sprintLaps.forEachIndexed { index, lap ->
                val durationSec = (lap.durationMillis ?: 0L) / 1000
                val distanceText = lap.distanceMeters?.let {
                    com.gitfast.app.util.formatDistance(it)
                } ?: ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Sprint ${index + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row {
                        if (distanceText.isNotEmpty()) {
                            Text(
                                text = distanceText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = com.gitfast.app.util.formatElapsedTime(durationSec.toInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DogWalkEventSection(
    events: List<com.gitfast.app.data.model.DogWalkEvent>,
    walkStartTimeMillis: Long,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "EVENT LOG (${events.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            events.forEach { event ->
                val offsetMillis = event.timestamp.toEpochMilli() - walkStartTimeMillis
                val offsetSeconds = (offsetMillis / 1000).toInt().coerceAtLeast(0)
                val minutes = offsetSeconds / 60
                val seconds = offsetSeconds % 60
                val timeLabel = "at %d:%02d".format(minutes, seconds)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = event.eventType.icon,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            text = event.eventType.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
