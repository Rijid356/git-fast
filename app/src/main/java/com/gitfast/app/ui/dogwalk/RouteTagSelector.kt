package com.gitfast.app.ui.dogwalk

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteTagSelector(
    tags: List<String>,
    selectedTag: String?,
    isAutoDetected: Boolean,
    onSelectTag: (String?) -> Unit,
    onConfirmNewTag: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showNewRouteInput by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Route",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selectedTag ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                placeholder = {
                    Text(
                        text = "No route detected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                singleLine = true,
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                // Clear selection option (only show if something is selected)
                if (selectedTag != null) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "None",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        onClick = {
                            onSelectTag(null)
                            expanded = false
                        },
                    )
                }

                // Route tags sorted by lastUsed DESC (already sorted from ViewModel)
                tags.forEach { tag ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = tag,
                                color = if (tag == selectedTag) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        onClick = {
                            onSelectTag(tag)
                            expanded = false
                        },
                    )
                }

                // "+ New Route" item
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "+ New Route",
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    },
                    onClick = {
                        expanded = false
                        showNewRouteInput = true
                        newTagName = ""
                    },
                )
            }
        }

        if (isAutoDetected && selectedTag != null) {
            Text(
                text = "auto-detected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        if (showNewRouteInput) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Route name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    val trimmed = newTagName.trim()
                    if (trimmed.isNotEmpty()) {
                        onConfirmNewTag(trimmed)
                        showNewRouteInput = false
                        newTagName = ""
                    }
                }) {
                    Text("ADD", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
