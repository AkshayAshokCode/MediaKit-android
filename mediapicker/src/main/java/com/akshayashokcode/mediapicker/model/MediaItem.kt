package com.akshayashokcode.mediapicker.model

import android.net.Uri

sealed class MediaItem {
    abstract val uri: Uri
    abstract val mimeType: String

    data class Image(
        override val uri: Uri,
        override val mimeType: String
    ) : MediaItem()

    data class Video(
        override val uri: Uri,
        override val mimeType: String,
        val durationMs: Long
    ) : MediaItem()

    data class Audio(
        override val uri: Uri,
        override val mimeType: String,
        val durationMs: Long,
        val displayName: String
    ) : MediaItem()

    data class Document(
        override val uri: Uri,
        override val mimeType: String,
        val displayName: String,
        val sizeBytes: Long
    ) : MediaItem()

    data class Unknown(
        override val uri: Uri,
        override val mimeType: String
    ) : MediaItem()
}
