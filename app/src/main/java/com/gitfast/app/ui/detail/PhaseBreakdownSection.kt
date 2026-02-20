package com.gitfast.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.ui.components.SectionHeader
import com.gitfast.app.util.PhaseAnalyzer

@Composable
fun PhaseBreakdownSection(
    phases: List<PhaseAnalyzer.PhaseDisplayItem>
) {
    // Don't show if there's only one phase (pre-lap-tracking workouts)
    if (phases.size <= 1) return

    Column {
        SectionHeader(text = "PHASE BREAKDOWN")

        Spacer(modifier = Modifier.height(8.dp))

        phases.forEach { phase ->
            PhaseCard(phase = phase)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun PhaseCard(phase: PhaseAnalyzer.PhaseDisplayItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = phase.label,
                style = MaterialTheme.typography.labelMedium,
                color = when (phase.type) {
                    PhaseType.WARMUP -> MaterialTheme.colorScheme.tertiary
                    PhaseType.LAPS -> MaterialTheme.colorScheme.primary
                    PhaseType.COOLDOWN -> MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.weight(1.2f)
            )
            Text(
                text = phase.durationFormatted,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = phase.distanceFormatted,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = phase.paceFormatted,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
