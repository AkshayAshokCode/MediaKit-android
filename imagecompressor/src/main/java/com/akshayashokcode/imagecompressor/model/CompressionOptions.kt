package com.akshayashokcode.imagecompressor.model

import android.graphics.Bitmap

data class CompressionOptions(
    val maxWidth: Int = 1920,
    val maxHeight: Int = 1920,
    val quality: Int = 85,
    val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    /** Iteratively reduces quality down to 30 until the output is within this limit. */
    val maxFileSizeBytes: Long? = null,
    val preserveExif: Boolean = false
)
