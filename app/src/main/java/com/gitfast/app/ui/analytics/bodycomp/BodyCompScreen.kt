package com.gitfast.app.ui.analytics.bodycomp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.gitfast.app.ui.components.GitFastTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.NeonGreen

private val CyanAccent = Color(0xFF58A6FF)
private val NeonGreenDim = NeonGreen.copy(alpha = 0.4f)
private val CyanDim = CyanAccent.copy(alpha = 0.4f)

@Composable
fun BodyCompScreen(
    onBackClick: () -> Unit,
    viewModel: BodyCompViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.syncMessage) {
        uiState.syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GitFastTopAppBar(title = "Body Comp", onBackClick = onBackClick)
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No body comp data yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Log your weight in Health Connect,\nthen tap Sync to pull it in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.sync() },
                            enabled = !uiState.isSyncing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text("Sync Now")
                            }
                        }
                    }
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
                    Spacer(modifier = Modifier.height(16.dp))

                    CurrentStatsCard(uiState)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.weightBars.isNotEmpty()) {
                        Text(
                            text = "WEIGHT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BodyCompBarChart(bars = uiState.weightBars, barColor = NeonGreen, barColorDim = NeonGreenDim)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (uiState.bodyFatBars.isNotEmpty()) {
                        Text(
                            text = "BODY FAT %",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BodyCompBarChart(bars = uiState.bodyFatBars, barColor = CyanAccent, barColorDim = CyanDim)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    CompositionBreakdown(uiState)
                    Spacer(modifier = Modifier.height(16.dp))

                    StatsSummary(uiState)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.weighInStreak > 0) {
                        StreakSection(uiState.weighInStreak)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodToggle(
    selected: BodyCompPeriod,
    onSelect: (BodyCompPeriod) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BodyCompPeriod.entries.forEach { period ->
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
                    text = period.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CurrentStatsCard(uiState: BodyCompUiState) {
    val reading = uiState.latestReading ?: return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "LATEST",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                uiState.latestDateFormatted?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                reading.weightLbs?.let {
                    StatItem(
                        value = "%.1f".format(it),
                        unit = "lbs",
                        modifier = Modifier.weight(1f),
                    )
                }
                reading.bodyFatPercent?.let {
                    StatItem(
                        value = "%.1f".format(it),
                        unit = "% BF",
                        modifier = Modifier.weight(1f),
                    )
                }
                reading.bmi?.let {
                    StatItem(
                        value = "%.1f".format(it),
                        unit = "BMI",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (reading.leanBodyMassLbs != null || reading.boneMassLbs != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    reading.leanBodyMassLbs?.let {
                        StatItem(
                            value = "%.1f".format(it),
                            unit = "lean lbs",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    reading.boneMassLbs?.let {
                        StatItem(
                            value = "%.1f".format(it),
                            unit = "bone lbs",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    reading.bmrKcalPerDay?.let {
                        StatItem(
                            value = "%.0f".format(it),
                            unit = "BMR",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BodyCompBarChart(
    bars: List<BodyCompChartBar>,
    barColor: Color,
    barColorDim: Color,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val maxValue = remember(bars) {
        bars.maxOfOrNull { it.value }?.coerceAtLeast(0.01f) ?: 1f
    }
    val minValue = remember(bars) {
        bars.minOfOrNull { it.value } ?: 0f
    }
    // For weight charts, scale from minValue-margin to maxValue+margin
    val range = (maxValue - minValue).coerceAtLeast(1f)
    val chartMin = minValue - range * 0.1f
    val chartMax = maxValue + range * 0.1f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            if (bars.isEmpty()) return@Canvas

            val topPadding = 16.dp.toPx()
            val bottomPadding = 20.dp.toPx()
            val barSpacing = 4.dp.toPx()
            val availableWidth = size.width
            val barWidth = (availableWidth - barSpacing * (bars.size - 1)) / bars.size
            val chartHeight = size.height - topPadding - bottomPadding

            bars.forEachIndexed { index, bar ->
                val x = index * (barWidth + barSpacing)
                val color = if (bar.isCurrent) barColor else barColorDim

                val normalized = ((bar.value - chartMin) / (chartMax - chartMin)).coerceIn(0.05f, 1f)
                val barHeight = normalized * chartHeight

                val barTop = topPadding + (chartHeight - barHeight)

                drawRect(
                    color = color,
                    topLeft = Offset(x, barTop),
                    size = Size(barWidth, barHeight),
                )

                // Value label above bar
                val valueMeasured = textMeasurer.measure(
                    text = bar.displayValue,
                    style = TextStyle(fontSize = 7.sp, textAlign = TextAlign.Center),
                )
                drawText(
                    textLayoutResult = valueMeasured,
                    color = labelColor,
                    topLeft = Offset(
                        x + (barWidth - valueMeasured.size.width) / 2,
                        barTop - valueMeasured.size.height - 2.dp.toPx(),
                    ),
                )

                // Period label below bar
                val labelMeasured = textMeasurer.measure(
                    text = bar.label,
                    style = TextStyle(fontSize = 7.sp, textAlign = TextAlign.Center),
                )
                drawText(
                    textLayoutResult = labelMeasured,
                    color = labelColor,
                    topLeft = Offset(
                        x + (barWidth - labelMeasured.size.width) / 2,
                        size.height - bottomPadding + 4.dp.toPx(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun CompositionBreakdown(uiState: BodyCompUiState) {
    if (uiState.fatMassLbs == null && uiState.leanMassLbs == null && uiState.boneMassLbs == null) return

    Column {
        Text(
            text = "COMPOSITION",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiState.fatMassLbs?.let {
                CompCard(
                    label = "FAT",
                    value = "$it lbs",
                    color = AmberAccent,
                    modifier = Modifier.weight(1f),
                )
            }
            uiState.leanMassLbs?.let {
                CompCard(
                    label = "LEAN",
                    value = "$it lbs",
                    color = NeonGreen,
                    modifier = Modifier.weight(1f),
                )
            }
            uiState.boneMassLbs?.let {
                CompCard(
                    label = "BONE",
                    value = "$it lbs",
                    color = CyanAccent,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CompCard(
    label: String,
    value: String,
    color: Color,
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
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
            )
        }
    }
}

@Composable
private fun StatsSummary(uiState: BodyCompUiState) {
    Column {
        Text(
            text = "STATS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatsRow("Weigh-ins", "${uiState.totalWeighIns}")
                StatsRow("Avg weight", "${uiState.avgWeightLbs} lbs")
                StatsRow(
                    label = "Change",
                    value = uiState.weightDelta,
                    valueColor = when (uiState.weightDeltaPositive) {
                        true -> NeonGreen
                        false -> AmberAccent
                        null -> null
                    },
                )
                StatsRow("Low", "${uiState.minWeightLbs} lbs")
                StatsRow("High", "${uiState.maxWeightLbs} lbs")
            }
        }
    }
}

@Composable
private fun StatsRow(
    label: String,
    value: String,
    valueColor: Color? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StreakSection(streak: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "WEIGH-IN STREAK",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$streak day${if (streak != 1) "s" else ""}",
                    style = MaterialTheme.typography.titleLarge,
                    color = AmberAccent,
                )
            }
        }
    }
}
