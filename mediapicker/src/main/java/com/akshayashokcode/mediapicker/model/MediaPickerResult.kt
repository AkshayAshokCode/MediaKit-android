package com.akshayashokcode.mediapicker.model

sealed class MediaPickerResult {
    data class Success(val item: MediaItem) : MediaPickerResult()
    data class MultipleSuccess(val items: List<MediaItem>) : MediaPickerResult()
    data object Cancelled : MediaPickerResult()
    data class Error(val message: String) : MediaPickerResult()
}
