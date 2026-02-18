package com.gitfast.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gitfast.app.analysis.RouteComparisonAnalyzer
import com.gitfast.app.ui.components.SectionHeader

@Composable
fun RouteComparisonSection(items: List<RouteComparisonAnalyzer.RouteComparisonItem>) {
    if (items.size <= 1) return  // Only current walk, no comparison needed

    Column {
        SectionHeader(text = "ROUTE COMPARISON")

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header row
                ComparisonHeaderRow()
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                items.forEach { item ->
                    ComparisonDataRow(item)
                    if (item != items.last()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonHeaderRow() {
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
private fun ComparisonDataRow(item: RouteComparisonAnalyzer.RouteComparisonItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Text(
            text = item.dateFormatted,
            style = if (item.isCurrentWalk) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (item.isCurrentWalk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.2f),
        )
        Text(
            text = item.durationFormatted,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.distanceFormatted,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.deltaFormatted ?: "\u2014",
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                item.deltaMillis == null -> MaterialTheme.colorScheme.onSurfaceVariant
                item.deltaMillis > 0 -> MaterialTheme.colorScheme.secondary  // previous was slower
                item.deltaMillis < 0 -> MaterialTheme.colorScheme.tertiary   // previous was faster
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}
