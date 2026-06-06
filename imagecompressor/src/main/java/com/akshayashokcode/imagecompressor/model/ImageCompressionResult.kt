package com.akshayashokcode.imagecompressor.model

import android.net.Uri

sealed class ImageCompressionResult {
    data class Success(
        val uri: Uri,
        val originalSizeBytes: Long,
        val compressedSizeBytes: Long
    ) : ImageCompressionResult()

    data object Cancelled : ImageCompressionResult()

    data class Error(val message: String) : ImageCompressionResult()
}
