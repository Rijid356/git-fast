package com.gitfast.app.ui.analytics.trends

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    onBackClick: () -> Unit,
    viewModel: TrendsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trends",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.isEmpty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No workout data yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    PeriodToggle(
                        selected = uiState.period,
                        onSelect = viewModel::setPeriod,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ActivityFilterRow(
                        selected = uiState.filter,
                        onSelect = viewModel::setFilter,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    uiState.comparison?.let { comparison ->
                        ComparisonSection(comparison = comparison)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = "DISTANCE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PixelBarChart(bars = uiState.distanceBars)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "WORKOUTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PixelBarChart(bars = uiState.workoutBars)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PeriodToggle(
    selected: TrendPeriod,
    onSelect: (TrendPeriod) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TrendPeriod.entries.forEach { period ->
            val isSelected = period == selected
            Surface(
                modifier = Modifier.weight(1f),
                shape = RectangleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                onClick = { onSelect(period) },
            ) {
                Text(
                    text = period.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ActivityFilterRow(
    selected: ActivityFilter,
    onSelect: (ActivityFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActivityFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Surface(
                shape = RectangleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                onClick = { onSelect(filter) },
            ) {
                Text(
                    text = filter.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun ComparisonSection(comparison: ComparisonDisplay) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "VS PREVIOUS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ComparisonCard(
                label = "DISTANCE",
                current = comparison.currentDistance,
                previous = comparison.previousDistance,
                delta = comparison.distanceDelta,
                deltaPositive = comparison.distanceDeltaPositive,
                modifier = Modifier.weight(1f),
            )
            ComparisonCard(
                label = "WORKOUTS",
                current = comparison.currentWorkouts,
                previous = comparison.previousWorkouts,
                delta = comparison.workoutsDelta,
                deltaPositive = comparison.workoutsDeltaPositive,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ComparisonCard(
                label = "DURATION",
                current = comparison.currentDuration,
                previous = comparison.previousDuration,
                delta = comparison.durationDelta,
                deltaPositive = comparison.durationDeltaPositive,
                modifier = Modifier.weight(1f),
            )
            ComparisonCard(
                label = "AVG PACE",
                current = comparison.currentPace ?: "--",
                previous = comparison.previousPace,
                delta = comparison.paceDelta,
                deltaPositive = comparison.paceDeltaPositive,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ComparisonCard(
    label: String,
    current: String,
    previous: String?,
    delta: String?,
    deltaPositive: Boolean?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = current,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (previous != null || delta != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (delta != null) {
                        val deltaColor = when (deltaPositive) {
                            true -> NeonGreen
                            false -> AmberAccent
                            null -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = delta,
                            style = MaterialTheme.typography.labelSmall,
                            color = deltaColor,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (previous != null) {
                        Text(
                            text = "was $previous",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
