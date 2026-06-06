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
import androidx.compose.material3.CircularProgressIndicator
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
import com.akshayashokcode.imagecompressor.entrypoint.ImageCompressor
import com.akshayashokcode.imagecompressor.model.CompressionOptions
import com.akshayashokcode.imagecompressor.model.ImageCompressionResult
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi
import com.akshayashokcode.imagepicker.compose.rememberImagePicker
import com.akshayashokcode.imagepicker.model.ImagePickerResult
import com.akshayashokcode.imagepicker.model.MediaSource

@OptIn(ExperimentalMediaKitApi::class)
@Composable
fun ImageCompressorScreen(contentPadding: PaddingValues = PaddingValues()) {
    val context = LocalContext.current
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var isCompressing by remember { mutableStateOf(false) }
    var compressionResult by remember(sourceUri) { mutableStateOf<ImageCompressionResult?>(null) }

    val sourcePicker = rememberImagePicker(source = MediaSource.Gallery) { result ->
        if (result is ImagePickerResult.Success) sourceUri = result.uri
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
            onClick = { sourcePicker.launch() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                if (sourceUri == null) "Pick Image" else "Pick Another Image",
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (sourceUri != null) {
            val originalSize = remember(sourceUri) { getFileSize(context, sourceUri!!) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Original size", style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatBytes(originalSize),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = {
                    isCompressing = true
                    compressionResult = null
                    ImageCompressor.with(context)
                        .source(sourceUri!!)
                        .options(CompressionOptions(maxWidth = 1920, maxHeight = 1920, quality = 85))
                        .onResult { result ->
                            compressionResult = result
                            isCompressing = false
                        }
                        .onError { isCompressing = false }
                        .compressAsync()
                },
                enabled = !isCompressing,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isCompressing) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Compress", style = MaterialTheme.typography.labelLarge)
                }
            }

            when (val r = compressionResult) {
                is ImageCompressionResult.Success -> {
                    Spacer(Modifier.height(4.dp))
                    Text("Result", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Compressed size", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            formatBytes(r.compressedSizeBytes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    val saving = ((r.originalSizeBytes - r.compressedSizeBytes) * 100L /
                            r.originalSizeBytes.coerceAtLeast(1)).toInt()
                    Text(
                        "Saved $saving%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is ImageCompressionResult.Error -> Text(
                    "Error: ${r.message}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                else -> Unit
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Pick an image to compress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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
