package com.gitfast.app.ui.analytics.routeperformance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePerformanceScreen(
    onBackClick: () -> Unit,
    onWorkoutClick: (String) -> Unit,
    viewModel: RoutePerformanceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ROUTE STATS",
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                item {
                    RouteTagSelector(
                        tags = uiState.routeTags,
                        selectedTag = uiState.selectedTag,
                        onTagSelected = viewModel::selectRouteTag,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                if (uiState.selectedTag == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Select a route",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else if (uiState.rows.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No sessions on this route",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    uiState.trendSummary?.let { trend ->
                        item {
                            TrendBanner(
                                trend = trend,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    uiState.personalBest?.let { best ->
                        item {
                            PersonalBestRow(
                                best = best,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RectangleShape,
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                TableHeaderRow()
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }

                    itemsIndexed(uiState.rows) { index, row ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RectangleShape,
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column {
                                PerformanceDataRow(
                                    row = row,
                                    onClick = { onWorkoutClick(row.workoutId) },
                                )
                                if (index < uiState.rows.size - 1) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "${uiState.sessionCount} sessions on this route",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteTagSelector(
    tags: List<String>,
    selectedTag: String?,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        TextField(
            value = selectedTag ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(
                    text = "Select a route",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RectangleShape,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            tags.forEach { tag ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        onTagSelected(tag)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TrendBanner(
    trend: TrendSummary,
    modifier: Modifier = Modifier,
) {
    val color = when {
        trend.isConsistent -> MaterialTheme.colorScheme.onSurfaceVariant
        trend.isImproving -> NeonGreen
        else -> AmberAccent
    }
    val text = when {
        trend.isConsistent -> "Consistent pace across sessions"
        trend.isImproving -> "${trend.deltaFormatted} faster on avg over last 3"
        else -> "${trend.deltaFormatted} slower on avg over last 3"
    }

    Surface(
        modifier = modifier,
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun PersonalBestRow(
    best: PerformanceRow,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Personal Best",
                tint = NeonGreen,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BEST:",
                style = MaterialTheme.typography.labelSmall,
                color = NeonGreen,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = best.date,
                style = MaterialTheme.typography.bodySmall,
                color = NeonGreen,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = best.durationFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = NeonGreen,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = best.distanceFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = NeonGreen,
            )
        }
    }
}

@Composable
private fun TableHeaderRow() {
    val style = MaterialTheme.typography.labelSmall
    val color = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Text("DATE", style = style, color = color, modifier = Modifier.weight(1.2f))
        Text("TIME", style = style, color = color, modifier = Modifier.weight(1f))
        Text("DIST", style = style, color = color, modifier = Modifier.weight(1f))
        Text("\u0394 TIME", style = style, color = color, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
private fun PerformanceDataRow(
    row: PerformanceRow,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (row.isPersonalBest) {
                    Modifier.background(NeonGreen.copy(alpha = 0.08f))
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = row.date,
            style = if (row.isMostRecent) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (row.isMostRecent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.2f),
        )
        Text(
            text = row.durationFormatted,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = row.distanceFormatted,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = row.deltaFormatted ?: "\u2014",
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                row.deltaMillis == null -> MaterialTheme.colorScheme.onSurfaceVariant
                row.deltaMillis > 0 -> MaterialTheme.colorScheme.secondary
                row.deltaMillis < 0 -> NeonGreen
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}
