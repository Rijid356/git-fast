package com.gitfast.app.ui.dogwalk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherSelector(
    selectedCondition: WeatherCondition?,
    selectedTemp: WeatherTemp?,
    onConditionSelected: (WeatherCondition) -> Unit,
    onTempSelected: (WeatherTemp) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Weather",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val conditions = listOf(
                WeatherCondition.SUNNY to "\u2600\uFE0F Sunny",
                WeatherCondition.CLOUDY to "\u2601\uFE0F Cloudy",
                WeatherCondition.RAINY to "\uD83C\uDF27 Rainy",
                WeatherCondition.SNOWY to "\u2744\uFE0F Snowy",
                WeatherCondition.WINDY to "\uD83D\uDCA8 Windy",
            )
            conditions.forEach { (condition, label) ->
                FilterChip(
                    selected = selectedCondition == condition,
                    onClick = { onConditionSelected(condition) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.secondary,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val temps = listOf(
                WeatherTemp.HOT to "Hot",
                WeatherTemp.WARM to "Warm",
                WeatherTemp.MILD to "Mild",
                WeatherTemp.COOL to "Cool",
                WeatherTemp.COLD to "Cold",
            )
            temps.forEach { (temp, label) ->
                FilterChip(
                    selected = selectedTemp == temp,
                    onClick = { onTempSelected(temp) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.secondary,
                    ),
                )
            }
        }
    }
}
