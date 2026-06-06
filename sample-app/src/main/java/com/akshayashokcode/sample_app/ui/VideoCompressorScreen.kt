package com.akshayashokcode.sample_app.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi
import com.akshayashokcode.mediapicker.compose.rememberMediaPicker
import com.akshayashokcode.mediapicker.model.MediaPickerResult
import com.akshayashokcode.mediapicker.model.MediaType
import com.akshayashokcode.videocompressor.entrypoint.VideoCompressor
import com.akshayashokcode.videocompressor.model.VideoCompressionOptions
import com.akshayashokcode.videocompressor.model.VideoCompressionResult

private fun getFileSize(context: android.content.Context, uri: Uri): Long {
    return try {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
    } catch (_: Exception) { 0L }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

@OptIn(ExperimentalMediaKitApi::class)
@Composable
fun VideoCompressorScreen(contentPadding: PaddingValues = PaddingValues()) {
    val context = LocalContext.current
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var progress by remember { mutableIntStateOf(0) }
    var isCompressing by remember { mutableStateOf(false) }
    var result by remember(sourceUri) { mutableStateOf<VideoCompressionResult?>(null) }

    val picker = rememberMediaPicker(MediaType.Video) { res ->
        if (res is MediaPickerResult.Success) sourceUri = res.item.uri
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
            Text(if (sourceUri == null) "Pick Video" else "Pick Another Video",
                style = MaterialTheme.typography.labelLarge)
        }

        if (sourceUri != null) {
            val originalSize = remember(sourceUri) { getFileSize(context, sourceUri!!) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Original size", style = MaterialTheme.typography.bodyMedium)
                Text(formatBytes(originalSize), fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium)
            }

            if (isCompressing) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("$progress%", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Button(
                    onClick = {
                        isCompressing = true
                        result = null
                        progress = 0
                        VideoCompressor.with(context)
                            .source(sourceUri!!)
                            .options(VideoCompressionOptions(maxWidth = 1280, maxHeight = 720, videoBitrateBps = 2_000_000))
                            .onProgress { pct -> progress = pct }
                            .onResult { r -> result = r; isCompressing = false }
                            .onError { isCompressing = false }
                            .compressAsync()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Compress Video", style = MaterialTheme.typography.labelLarge) }
            }

            when (val r = result) {
                is VideoCompressionResult.Success -> {
                    Spacer(Modifier.height(4.dp))
                    Text("Result", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Compressed size", style = MaterialTheme.typography.bodyMedium)
                        Text(formatBytes(r.compressedSizeBytes), fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    val saving = ((r.originalSizeBytes - r.compressedSizeBytes) * 100L /
                            r.originalSizeBytes.coerceAtLeast(1)).toInt()
                    Text("Saved $saving%", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is VideoCompressionResult.Error -> Text("Error: ${r.message}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error)
                else -> Unit
            }
        } else {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Pick a video to compress", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
