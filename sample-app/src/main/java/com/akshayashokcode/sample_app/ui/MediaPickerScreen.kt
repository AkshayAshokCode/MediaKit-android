package com.akshayashokcode.sample_app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi
import com.akshayashokcode.mediapicker.compose.rememberMediaPicker
import com.akshayashokcode.mediapicker.model.MediaItem
import com.akshayashokcode.mediapicker.model.MediaPickerResult
import com.akshayashokcode.mediapicker.model.MediaType

private val allTypes = listOf(
    MediaType.Image, MediaType.Video, MediaType.Audio, MediaType.Document, MediaType.All
)

private fun MediaType.label() = when (this) {
    is MediaType.Image -> "Image"
    is MediaType.Video -> "Video"
    is MediaType.Audio -> "Audio"
    is MediaType.Document -> "Document"
    is MediaType.All -> "All"
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMediaKitApi::class)
@Composable
fun MediaPickerScreen(contentPadding: PaddingValues = PaddingValues()) {
    var selectedTypes by remember { mutableStateOf(setOf<MediaType>(MediaType.All)) }
    var allowMultiple by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<MediaPickerResult?>(null) }

    // rememberMediaPicker re-reads selectedTypes and allowMultiple at launch time
    // via the rememberUpdatedState pattern inside the implementation.
    val picker = rememberMediaPicker(
        *selectedTypes.toTypedArray(),
        allowMultiple = allowMultiple
    ) { res -> result = res }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Media types", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            allTypes.forEach { type ->
                FilterChip(
                    selected = selectedTypes.contains(type),
                    onClick = {
                        selectedTypes = if (selectedTypes.contains(type)) {
                            val updated = selectedTypes - type
                            updated.ifEmpty { setOf(MediaType.All) }
                        } else {
                            if (type is MediaType.All) setOf(MediaType.All)
                            else (selectedTypes - MediaType.All) + type
                        }
                    },
                    label = { Text(type.label()) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Allow multiple", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = allowMultiple, onCheckedChange = { allowMultiple = it })
        }

        Button(
            onClick = { picker.launch() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Pick Media", style = MaterialTheme.typography.labelLarge)
        }

        result?.let {
            Spacer(Modifier.height(4.dp))
            Text("Result", style = MaterialTheme.typography.labelLarge)
            ResultSection(it)
        }
    }
}

@Composable
private fun ResultSection(result: MediaPickerResult) {
    when (result) {
        is MediaPickerResult.Success -> MediaItemRow(result.item)
        is MediaPickerResult.MultipleSuccess -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            result.items.forEach { MediaItemRow(it) }
        }
        is MediaPickerResult.Cancelled -> Text(
            "Cancelled",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        is MediaPickerResult.Error -> Text(
            "Error: ${result.message}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun MediaItemRow(item: MediaItem) {
    Text(
        text = when (item) {
            is MediaItem.Image -> "Image · ${item.mimeType}"
            is MediaItem.Video -> "Video · ${item.mimeType} · ${item.durationMs / 1000}s"
            is MediaItem.Audio -> "Audio · ${item.displayName} · ${item.durationMs / 1000}s"
            is MediaItem.Document -> "Document · ${item.displayName} · ${item.sizeBytes / 1024}KB"
            is MediaItem.Unknown -> "Unknown · ${item.mimeType}"
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    )
}
