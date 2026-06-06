package com.akshayashokcode.mediapicker.util

import androidx.activity.result.contract.ActivityResultContracts
import com.akshayashokcode.mediapicker.model.MediaType

internal object MimeTypeUtils {

    private val documentMimeTypes = listOf(
        "application/pdf",
        "text/plain",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    )

    /** Returns true when all types are Image/Video — use PickVisualMedia. */
    fun isVisualOnly(types: List<MediaType>): Boolean {
        if (types.any { it is MediaType.All }) return false
        return types.all { it is MediaType.Image || it is MediaType.Video }
    }

    /** Resolves the VisualMediaType input for PickVisualMedia/PickMultipleVisualMedia. */
    fun resolveVisualMediaInput(
        types: List<MediaType>
    ): ActivityResultContracts.PickVisualMedia.VisualMediaType {
        val hasImage = types.any { it is MediaType.Image }
        val hasVideo = types.any { it is MediaType.Video }
        return when {
            hasImage && hasVideo -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
            hasVideo -> ActivityResultContracts.PickVisualMedia.VideoOnly
            else -> ActivityResultContracts.PickVisualMedia.ImageOnly
        }
    }

    /** Resolves MIME type array for OpenDocument / OpenMultipleDocuments. */
    fun resolveInputMimeTypes(types: List<MediaType>): Array<String> {
        if (types.any { it is MediaType.All }) return arrayOf("*/*")
        val mimes = mutableSetOf<String>()
        for (type in types) {
            when (type) {
                is MediaType.Image -> mimes.add("image/*")
                is MediaType.Video -> mimes.add("video/*")
                is MediaType.Audio -> mimes.add("audio/*")
                is MediaType.Document -> mimes.addAll(documentMimeTypes)
                is MediaType.All -> return arrayOf("*/*")
            }
        }
        return if (mimes.isEmpty()) arrayOf("*/*") else mimes.toTypedArray()
    }
}
