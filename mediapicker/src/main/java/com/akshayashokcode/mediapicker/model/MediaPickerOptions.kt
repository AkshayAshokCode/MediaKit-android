package com.akshayashokcode.mediapicker.model

internal data class MediaPickerOptions(
    val mediaTypes: List<MediaType> = listOf(MediaType.All),
    val restrictMimeTypes: List<String> = emptyList(),
    val restrictExtensions: List<String> = emptyList(),
    val allowMultiple: Boolean = false
)
