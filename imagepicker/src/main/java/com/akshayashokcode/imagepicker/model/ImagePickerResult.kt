package com.akshayashokcode.imagepicker.model

import android.graphics.Bitmap
import android.net.Uri

/**
 * Represents the final state returned from the MediaKit image picker flow.
 */
sealed class ImagePickerResult {

    /**
     * Successful image selection with resulting Uri.
     */
    data class Success(val uri: Uri) : ImagePickerResult()

    /**
     * Successful image selection with decoded bitmap.
     *
     * Intended for advanced consumers that require immediate bitmap access.
     */
    data class SuccessWithBitmap(
        val uri: Uri,
        val bitmap: Bitmap
    ) : ImagePickerResult()

    /**
     * User cancelled the picker flow.
     */
    data object Cancelled : ImagePickerResult()

    /**
     * Generic picker error.
     *
     * This will later migrate to a sealed error model for stronger typing.
     */
    data class Error(val message: String) : ImagePickerResult()
}
