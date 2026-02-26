package com.gitfast.app.ui.soreness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.ui.theme.NeonGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MuscleGroupSelector(
    selected: Set<MuscleGroup>,
    onToggle: (MuscleGroup) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MuscleGroup.entries.forEach { group ->
            val isSelected = group in selected
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(group) },
                enabled = enabled,
                label = {
                    Text(
                        text = group.displayName,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                    selectedLabelColor = NeonGreen,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
