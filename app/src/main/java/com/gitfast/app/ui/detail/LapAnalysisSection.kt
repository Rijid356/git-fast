package com.gitfast.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.ui.components.SectionHeader

@Composable
fun LapAnalysisSection(analysis: LapAnalysis) {
    Column {
        SectionHeader(text = "LAP ANALYSIS")

        Spacer(modifier = Modifier.height(8.dp))

        LapSummaryRow(analysis)

        Spacer(modifier = Modifier.height(12.dp))

        LapTrendIndicator(analysis.trend)

        Spacer(modifier = Modifier.height(16.dp))

        if (analysis.trendChartPoints.size >= 2) {
            LapTrendChart(
                points = analysis.trendChartPoints,
                averageSeconds = analysis.averageLapSeconds
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        LapTable(laps = analysis.laps)
    }
}

@Composable
fun LapSummaryRow(analysis: LapAnalysis) {
    Text(
        text = "${analysis.lapCount} laps  •  Best: ${analysis.bestLapTime}  •  Avg: ${analysis.averageLapTime}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun LapTrendIndicator(trend: LapTrend) {
    val (text, color) = when (trend) {
        LapTrend.GETTING_FASTER -> "Getting faster ▲" to MaterialTheme.colorScheme.secondary
        LapTrend.GETTING_SLOWER -> "Getting slower ▼" to MaterialTheme.colorScheme.tertiary
        LapTrend.CONSISTENT -> "Consistent pace ─" to MaterialTheme.colorScheme.onSurfaceVariant
        LapTrend.TOO_FEW_LAPS -> "Not enough laps for trend" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color
    )
}
