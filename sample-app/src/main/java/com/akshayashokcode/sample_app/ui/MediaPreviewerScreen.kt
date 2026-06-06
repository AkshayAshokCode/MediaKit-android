package com.akshayashokcode.sample_app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi
import com.akshayashokcode.mediapicker.compose.rememberMediaPicker
import com.akshayashokcode.mediapicker.model.MediaItem
import com.akshayashokcode.mediapicker.model.MediaPickerResult
import com.akshayashokcode.mediapicker.model.MediaType
import com.akshayashokcode.mediapreviewer.entrypoint.MediaPreviewer
import com.akshayashokcode.mediapreviewer.model.MediaPreviewItem
import com.akshayashokcode.mediapreviewer.model.PreviewOptions

@OptIn(ExperimentalMediaKitApi::class)
@Composable
fun MediaPreviewerScreen(contentPadding: PaddingValues = PaddingValues()) {
    val context = LocalContext.current
    var pickedItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    val picker = rememberMediaPicker(
        MediaType.Image, MediaType.Video,
        allowMultiple = true
    ) { result ->
        when (result) {
            is MediaPickerResult.MultipleSuccess -> pickedItems = result.items
            is MediaPickerResult.Success -> pickedItems = listOf(result.item)
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = { picker.launch() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Pick Images & Videos", style = MaterialTheme.typography.labelLarge)
        }

        if (pickedItems.isNotEmpty()) {
            Text(
                "${pickedItems.size} item(s) selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            pickedItems.forEach { item ->
                Text(
                    text = when (item) {
                        is MediaItem.Image -> "Image · ${item.mimeType}"
                        is MediaItem.Video -> "Video · ${item.mimeType}"
                        else -> item.mimeType
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    val previewItems = pickedItems.map { item ->
                        when (item) {
                            is MediaItem.Image -> MediaPreviewItem.Image(item.uri)
                            is MediaItem.Video -> MediaPreviewItem.Video(item.uri)
                            else -> MediaPreviewItem.Image(item.uri)
                        }
                    }
                    val activity = context as ComponentActivity
                    MediaPreviewer.with(context, activity)
                        .items(previewItems)
                        .options(PreviewOptions(showShareButton = true, zoomEnabled = true))
                        .launch()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Open Preview", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Pick images and videos to preview them fullscreen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
