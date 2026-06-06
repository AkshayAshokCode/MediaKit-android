package com.akshayashokcode.videocompressor.model

data class VideoCompressionOptions(
    val maxWidth: Int = 1280,
    val maxHeight: Int = 720,
    /** Target video bitrate in bits per second. */
    val videoBitrateBps: Int = 2_000_000,
    val frameRate: Int = 30
)
