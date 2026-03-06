package com.gitfast.app.ui.dogwalk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherData
import com.gitfast.app.data.model.WeatherTemp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherCard(
    weatherData: WeatherData?,
    isEditing: Boolean,
    selectedCondition: WeatherCondition?,
    selectedTemp: WeatherTemp?,
    onToggleEdit: () -> Unit,
    onConditionSelected: (WeatherCondition) -> Unit,
    onTempSelected: (WeatherTemp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Weather",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        if (weatherData != null && !isEditing) {
            // Auto-detected weather card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = weatherIconForCode(weatherData.iconCode),
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${weatherData.tempF}\u00B0F",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weatherData.condition,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Wind: ${weatherData.windSpeedMph} mph",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Humidity: ${weatherData.humidity}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedButton(onClick = onToggleEdit) {
                        Text(
                            text = "EDIT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        } else {
            // Manual picker (fallback or edit mode)
            if (weatherData != null) {
                Text(
                    text = "auto-detected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val conditions = listOf(
                    WeatherCondition.SUNNY to "\u2600\uFE0F",
                    WeatherCondition.CLOUDY to "\u2601\uFE0F",
                    WeatherCondition.RAINY to "\uD83C\uDF27\uFE0F",
                    WeatherCondition.SNOWY to "\u2744\uFE0F",
                    WeatherCondition.WINDY to "\uD83D\uDCA8",
                )
                conditions.forEach { (condition, icon) ->
                    FilterChip(
                        selected = selectedCondition == condition,
                        onClick = { onConditionSelected(condition) },
                        label = { Text(icon) },
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

            AnimatedVisibility(visible = weatherData != null) {
                OutlinedButton(
                    onClick = onToggleEdit,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        text = "DONE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

private fun weatherIconForCode(iconCode: String): String = when {
    iconCode.startsWith("01") -> "\u2600\uFE0F"      // clear
    iconCode.startsWith("02") -> "\uD83C\uDF24\uFE0F" // few clouds
    iconCode.startsWith("03") || iconCode.startsWith("04") -> "\u2601\uFE0F" // clouds
    iconCode.startsWith("09") || iconCode.startsWith("10") -> "\uD83C\uDF27\uFE0F" // rain
    iconCode.startsWith("11") -> "\u26C8\uFE0F"       // thunderstorm
    iconCode.startsWith("13") -> "\u2744\uFE0F"       // snow
    iconCode.startsWith("50") -> "\uD83C\uDF2B\uFE0F" // mist/fog
    else -> "\uD83C\uDF24\uFE0F"
}
