package com.akshayashokcode.videocompressor.model

import android.net.Uri

sealed class VideoCompressionResult {
    data class Success(
        val uri: Uri,
        val originalSizeBytes: Long,
        val compressedSizeBytes: Long
    ) : VideoCompressionResult()

    data object Cancelled : VideoCompressionResult()

    data class Error(val message: String) : VideoCompressionResult()
}
