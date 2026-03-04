package com.gitfast.app.ui.soreness

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity

@Composable
fun MuscleCombobox(
    muscleIntensities: Map<MuscleGroup, SorenessIntensity>,
    onZoneTap: (MuscleGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filtered = remember(query) {
        if (query.isBlank()) {
            MuscleGroup.entries.toList()
        } else {
            MuscleGroup.entries.filter {
                it.displayName.contains(query, ignoreCase = true)
            }
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { expanded = it.isFocused },
            placeholder = {
                Text(
                    text = "Search muscles...",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            singleLine = true,
            shape = RectangleShape,
        )

        if (expanded && filtered.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 56.dp)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                items(filtered) { muscle ->
                    val intensity = muscleIntensities[muscle]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onZoneTap(muscle)
                                query = ""
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (intensity != null) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(intensityColor(intensity)),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = muscle.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (intensity != null) {
                                intensityColor(intensity)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                        if (intensity != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = intensity.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = intensityColor(intensity).copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }
        }
    }
}
