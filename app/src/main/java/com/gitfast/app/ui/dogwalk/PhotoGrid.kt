package com.gitfast.app.ui.dogwalk

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

data class WalkPhoto(
    val id: String,
    val filePath: String,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoGrid(
    photos: List<WalkPhoto>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Photos",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            photos.forEach { photo ->
                PhotoThumbnail(
                    photo = photo,
                    onRemove = { onRemovePhoto(photo.id) },
                )
            }
            AddPhotoButton(onClick = onAddPhoto)
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: WalkPhoto,
    onRemove: () -> Unit,
) {
    Box(modifier = Modifier.size(80.dp)) {
        Surface(
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize(),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(photo.filePath))
                    .crossfade(true)
                    .size(160)
                    .build(),
                contentDescription = "Walk photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Remove button overlay
        Surface(
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .clickable { onRemove() },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "X",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                )
            }
        }
    }
}

@Composable
private fun AddPhotoButton(onClick: () -> Unit) {
    val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Surface(
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .size(80.dp)
            .clickable { onClick() },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "ADD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
