package com.akshayashokcode.videocompressor.model

import com.akshayashokcode.mediakitcore.exception.MediaKitException

sealed class VideoCompressionException(message: String) : MediaKitException(message) {
    data object InvalidSource : VideoCompressionException("Source URI is null or unreadable.")
    data object NoVideoTrack : VideoCompressionException("No video track found in source file.")
    data object CompressionFailed : VideoCompressionException("Video compression pipeline failed.")
    data object FileCreationFailed : VideoCompressionException("Unable to create output file.")
    data object Cancelled : VideoCompressionException("Compression was cancelled.")
    class Unknown(message: String, cause: Throwable? = null) : VideoCompressionException(message) {
        init { cause?.let { initCause(it) } }
    }
}
