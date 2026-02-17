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
import androidx.compose.foundation.shape.RoundedCornerShape
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
                        text = if ((uiState as? DetailUiState.Loaded)?.detail?.activityType == ActivityType.DOG_WALK)
                            "Dog Walk" else "Run Details",
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
    modifier: Modifier = Modifier,
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

        // Dog walk metadata (only for dog walks)
        if (detail.activityType == ActivityType.DOG_WALK) {
            DogWalkMetadataSection(detail = detail)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Phase breakdown (only for runs with multiple phases)
        if (detail.activityType == ActivityType.RUN) {
            PhaseBreakdownSection(phases = phases)
            if (phases.size > 1) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            lapAnalysis?.let {
                LapAnalysisSection(analysis = it)
                Spacer(modifier = Modifier.height(24.dp))
            }
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

        // Route comparison (only for dog walks with route tag)
        if (detail.activityType == ActivityType.DOG_WALK && routeComparison.isNotEmpty()) {
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
        shape = RoundedCornerShape(12.dp),
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
private fun NoRouteContent() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
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
    val accuracyText = avgAccuracy?.let { String.format("%.0fm avg accuracy", it) } ?: ""
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
        shape = RoundedCornerShape(12.dp),
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
