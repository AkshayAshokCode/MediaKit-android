package com.akshayashokcode.mediapreviewer.model

import android.net.Uri

sealed class MediaPreviewItem {
    abstract val uri: Uri

    data class Image(override val uri: Uri) : MediaPreviewItem()
    data class Video(override val uri: Uri) : MediaPreviewItem()
    data class Audio(override val uri: Uri) : MediaPreviewItem()
}
