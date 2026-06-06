package com.akshayashokcode.mediapicker.model

import com.akshayashokcode.mediakitcore.exception.MediaKitException

sealed class MediaPickerException(message: String) : MediaKitException(message) {

    data object AppNotFound : MediaPickerException("No media picker app available.")

    data object InvalidUri : MediaPickerException("Returned URI is null or unreadable.")

    data object RestrictedFile : MediaPickerException("Selected file is restricted by the configured filters.")

    class Unknown(
        message: String,
        cause: Throwable? = null
    ) : MediaPickerException(message) {
        init { cause?.let { initCause(it) } }
    }
}
