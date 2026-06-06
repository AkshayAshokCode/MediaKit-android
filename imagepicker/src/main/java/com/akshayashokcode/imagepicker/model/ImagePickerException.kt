package com.akshayashokcode.imagepicker.model

import com.akshayashokcode.mediakitcore.exception.MediaKitException

sealed class ImagePickerException(message: String) : MediaKitException(message) {

    /**
     * Required runtime permission was denied.
     */
    data object PermissionDenied : ImagePickerException(
        "Required permission was denied."
    )

    /**
     * No compatible application is available to handle the requested action.
     */
    data object AppNotFound : ImagePickerException(
        "Required app (camera or gallery) is not available."
    )

    /**
     * Temporary file creation failed before launching camera capture.
     */
    data object FileCreationFailed : ImagePickerException(
        "Could not create temporary file."
    )

    /**
     * The returned image Uri was invalid or null.
     */
    data object InvalidUri : ImagePickerException(
        "Invalid or null URI received."
    )

    /**
     * Bitmap decoding or orientation correction failed.
     */
    data object RotationFailed : ImagePickerException(
        "Failed to decode or rotate the image."
    )

    /**
     * Cleanup of temporary files failed.
     */
    data object FileDeletionFailed : ImagePickerException(
        "Failed to delete temporary image file."
    )

    /**
     * Image decoding pipeline failed.
     */
    data object DecodingFailed : ImagePickerException(
        "Failed to decode or rotate image."
    )

    /**
     * Intent launch failed unexpectedly.
     */
    data object IntentFailed : ImagePickerException(
        "Failed to launch intent for image capture or selection."
    )

    /**
     * Generic fallback error.
     */
    class Unknown(
        message: String,
        cause: Throwable? = null
    ) : ImagePickerException(message) {
        init {
            cause?.let { initCause(it) }
        }
    }
}
