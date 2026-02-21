package com.gitfast.app.ui.analytics

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

data class AnalyticsSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val enabled: Boolean = false,
)

private val sections = listOf(
    AnalyticsSection("ROUTE MAP", "Compare GPS traces", Icons.Default.Place, enabled = true),
    AnalyticsSection("ROUTE STATS", "Performance by route", Icons.Default.Info, enabled = true),
    AnalyticsSection("RECORDS", "Personal bests", Icons.Default.Star),
    AnalyticsSection("TRENDS", "Weekly & monthly", Icons.Default.Favorite),
    AnalyticsSection("WEATHER", "Impact analysis", Icons.Default.Warning),
    AnalyticsSection("TRAINING", "Phase & energy", Icons.Default.Person),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsHubScreen(
    onBackClick: () -> Unit,
    onRouteMapClick: () -> Unit = {},
    onRouteStatsClick: () -> Unit = {},
    viewModel: AnalyticsHubViewModel = hiltViewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                LifetimeStatsSection(stats = stats)
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "INTEL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            items(sections) { section ->
                SectionCard(
                    section = section,
                    onClick = {
                        when {
                            section.enabled && section.title == "ROUTE MAP" -> onRouteMapClick()
                            section.enabled && section.title == "ROUTE STATS" -> onRouteStatsClick()
                            !section.enabled -> scope.launch {
                                snackbarHostState.showSnackbar("Coming soon!")
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun LifetimeStatsSection(stats: AnalyticsHubViewModel.AnalyticsStats) {
    Column {
        Text(
            text = "LIFETIME STATS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatBox(
                value = "${stats.totalWorkouts}",
                label = "WORKOUTS",
                modifier = Modifier.weight(1f),
            )
            StatBox(
                value = stats.totalDistanceFormatted,
                label = "DISTANCE",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatBox(
                value = stats.totalDurationFormatted,
                label = "TIME",
                modifier = Modifier.weight(1f),
            )
            StatBox(
                value = if (stats.bestStreak > 0) "${stats.bestStreak} day" else "--",
                label = "BEST STREAK",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionCard(
    section: AnalyticsSection,
    onClick: () -> Unit,
) {
    val alpha = if (section.enabled) 1f else 0.5f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RectangleShape,
            )
            .clickable(onClick = onClick),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = section.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!section.enabled) {
                Text(
                    text = "SOON",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
        }
    }
}
