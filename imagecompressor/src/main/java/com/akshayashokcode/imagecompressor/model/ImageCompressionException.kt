package com.akshayashokcode.imagecompressor.model

import com.akshayashokcode.mediakitcore.exception.MediaKitException

sealed class ImageCompressionException(message: String) : MediaKitException(message) {

    data object InvalidSource : ImageCompressionException("Source URI is null or unreadable.")

    data object DecodingFailed : ImageCompressionException("Failed to decode source bitmap.")

    data object EncodingFailed : ImageCompressionException("Failed to encode compressed image.")

    data object FileCreationFailed : ImageCompressionException("Unable to create cache file.")

    class Unknown(
        message: String,
        cause: Throwable? = null
    ) : ImageCompressionException(message) {
        init { cause?.let { initCause(it) } }
    }
}
