package com.gitfast.app.screenshots.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.detail.LapAnalysis
import com.gitfast.app.ui.detail.LapChartPoint
import com.gitfast.app.ui.detail.LapDisplayItem
import com.gitfast.app.ui.detail.LapTrend
import com.gitfast.app.ui.detail.WorkoutDetailItem
import com.gitfast.app.ui.theme.NeonGreen
import com.gitfast.app.util.PhaseAnalyzer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DetailScreenScreenshotTest : FullScreenScreenshotTestBase() {

    @Test
    fun `Screen Detail Run`() {
        captureScreenshot("Screen_Detail_Run", category = "detail") {
            DetailScreenMock(
                title = "Run Details",
                detail = sampleRunDetail,
                phases = sampleRunPhases,
                lapAnalysis = sampleLapAnalysis,
            )
        }
    }

    @Test
    fun `Screen Detail DogWalk`() {
        captureScreenshot("Screen_Detail_DogWalk", category = "detail") {
            DetailScreenMock(
                title = "Dog Walk",
                detail = sampleDogWalkDetail,
                phases = null,
                lapAnalysis = null,
                routePoints = dogWalkRoutePoints,
            )
        }
    }
}

// ──────────────────────────────────────────────────────────
// Test-only composable that replicates DetailScreen layout
// but uses FakeRouteMap instead of Google Maps
// ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScreenMock(
    title: String,
    detail: WorkoutDetailItem,
    phases: List<PhaseAnalyzer.PhaseDisplayItem>?,
    lapAnalysis: LapAnalysis?,
    routePoints: List<Pair<Float, Float>> = defaultRunRoutePoints,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Date header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = detail.dateFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = detail.timeFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                        StatCell("DISTANCE", detail.distanceFormatted, Modifier.weight(1f))
                        StatCell("TIME", detail.durationFormatted, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatCell("AVG PACE", detail.avgPaceFormatted, Modifier.weight(1f))
                        StatCell("STEPS", detail.stepsFormatted, Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // XP section
            if (detail.xpEarned > 0) {
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
                                text = "+${detail.xpEarned} XP",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Dog walk metadata
            if (detail.activityType == ActivityType.DOG_WALK) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        detail.dogName?.let { MetaRow("\uD83D\uDC15", it) }
                        detail.routeTag?.let { MetaRow("\uD83D\uDCCD", it) }
                        detail.weatherSummary?.let { MetaRow("\uD83C\uDF24", it) }
                        detail.energyLevel?.let {
                            MetaRow(
                                "\u26A1",
                                "${it.name.lowercase().replaceFirstChar { c -> c.uppercase() }} energy",
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Phase breakdown (runs only)
            if (detail.activityType == ActivityType.RUN && phases != null) {
                Text(
                    text = "PHASE BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                phases.forEach { phase ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        shape = RectangleShape,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = phase.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "${phase.durationFormatted}  ${phase.distanceFormatted}  ${phase.paceFormatted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Lap analysis summary (runs only)
            if (lapAnalysis != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "LAP ANALYSIS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${lapAnalysis.lapCount} laps \u2022 Best: ${lapAnalysis.bestLapTime} \u2022 Avg: ${lapAnalysis.averageLapTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // *** Fake route map (Canvas-based, replaces Google Maps) ***
            FakeRouteMap(routePoints = routePoints)

            // GPS quality footer
            if (detail.gpsPointCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val accuracyText = detail.avgGpsAccuracy?.let {
                    String.format(java.util.Locale.US, "%.0fm avg accuracy", it)
                } ?: ""
                Text(
                    text = "${detail.gpsPointCount} GPS points \u00B7 $accuracyText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Notes
            detail.notes?.let { notes ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
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
private fun MetaRow(icon: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = icon, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ──────────────────────────────────────────────────────────
// Default route shapes
// ──────────────────────────────────────────────────────────

private val defaultRunRoutePoints = listOf(
    0.15f to 0.85f, 0.18f to 0.78f, 0.22f to 0.70f, 0.28f to 0.62f,
    0.35f to 0.54f, 0.42f to 0.47f, 0.50f to 0.40f, 0.58f to 0.35f,
    0.65f to 0.30f, 0.72f to 0.26f, 0.78f to 0.22f, 0.84f to 0.20f,
    0.88f to 0.22f, 0.86f to 0.28f, 0.80f to 0.34f, 0.73f to 0.40f,
    0.66f to 0.46f, 0.58f to 0.52f, 0.50f to 0.57f, 0.42f to 0.63f,
    0.35f to 0.68f, 0.28f to 0.74f, 0.22f to 0.80f, 0.18f to 0.84f,
    0.15f to 0.85f,
)

// ──────────────────────────────────────────────────────────
// Sample data
// ──────────────────────────────────────────────────────────

private val sampleRunDetail = WorkoutDetailItem(
    workoutId = "run-001",
    dateFormatted = "Feb 24, 2026 at 7:30 AM",
    timeFormatted = "7:30 AM",
    distanceFormatted = "3.12 mi",
    durationFormatted = "25:30",
    avgPaceFormatted = "8:11 /mi",
    stepsFormatted = "5,432",
    gpsPointCount = 1280,
    avgGpsAccuracy = 4.2f,
    routePoints = emptyList(),
    routeBounds = null,
    activityType = ActivityType.RUN,
    dogName = null,
    routeTag = null,
    weatherSummary = null,
    energyLevel = null,
    notes = null,
    xpEarned = 150,
    xpBreakdown = "Base: 100 XP + Streak: 50 XP",
)

private val sampleDogWalkDetail = WorkoutDetailItem(
    workoutId = "walk-001",
    dateFormatted = "Feb 24, 2026 at 12:00 PM",
    timeFormatted = "12:00 PM",
    distanceFormatted = "1.05 mi",
    durationFormatted = "22:45",
    avgPaceFormatted = "21:40 /mi",
    stepsFormatted = "2,340",
    gpsPointCount = 560,
    avgGpsAccuracy = 5.1f,
    routePoints = emptyList(),
    routeBounds = null,
    activityType = ActivityType.DOG_WALK,
    dogName = "Juniper",
    routeTag = "Park Loop",
    weatherSummary = "Sunny, Cool",
    energyLevel = EnergyLevel.HYPER,
    notes = "Juniper loved the squirrels today",
    xpEarned = 80,
    xpBreakdown = "Base: 60 XP + Dog Walk: 20 XP",
)

private val sampleRunPhases = listOf(
    PhaseAnalyzer.PhaseDisplayItem(
        type = PhaseType.WARMUP, label = "WARMUP",
        durationFormatted = "3:15", distanceFormatted = "0.32 mi", paceFormatted = "10:09 /mi",
    ),
    PhaseAnalyzer.PhaseDisplayItem(
        type = PhaseType.LAPS, label = "LAPS (5)",
        durationFormatted = "18:45", distanceFormatted = "2.40 mi", paceFormatted = "7:49 /mi",
    ),
    PhaseAnalyzer.PhaseDisplayItem(
        type = PhaseType.COOLDOWN, label = "COOLDOWN",
        durationFormatted = "3:30", distanceFormatted = "0.40 mi", paceFormatted = "8:45 /mi",
    ),
)

private val sampleLapAnalysis = LapAnalysis(
    laps = listOf(
        LapDisplayItem("1", 1, "3:52", "0.48 mi", "8:03 /mi", null, null, false, false),
        LapDisplayItem("2", 2, "3:41", "0.48 mi", "7:40 /mi", "▼ -11s", -11, true, false),
        LapDisplayItem("3", 3, "3:48", "0.48 mi", "7:55 /mi", "▲ +7s", 7, false, false),
        LapDisplayItem("4", 4, "3:44", "0.48 mi", "7:47 /mi", "▼ -4s", -4, false, false),
        LapDisplayItem("5", 5, "3:40", "0.48 mi", "7:38 /mi", "▼ -4s", -4, false, false),
    ),
    lapCount = 5,
    bestLapTime = "3:41",
    bestLapNumber = 2,
    slowestLapTime = "3:52",
    slowestLapNumber = 1,
    averageLapTime = "3:45",
    averageLapSeconds = 225,
    trend = LapTrend.GETTING_FASTER,
    trendChartPoints = listOf(
        LapChartPoint(1, 232), LapChartPoint(2, 221), LapChartPoint(3, 228),
        LapChartPoint(4, 224), LapChartPoint(5, 220),
    ),
)
