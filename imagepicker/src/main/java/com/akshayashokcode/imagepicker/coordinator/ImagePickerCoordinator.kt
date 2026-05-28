package com.akshayashokcode.imagepicker.coordinator

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.model.ImagePickerResult
import com.akshayashokcode.imagepicker.model.MediaSource
import com.akshayashokcode.imagepicker.picker.CameraImagePicker
import com.akshayashokcode.imagepicker.picker.GalleryImagePicker

/**
 * Coordinates the image picker flow and delegates execution to the appropriate
 * picker implementation.
 */
internal class ImagePickerCoordinator(
    private val context: Context,
    private val caller: ActivityResultCaller,
    private val source: MediaSource,
    private val crop: Boolean,
    private val onResult: (ImagePickerResult) -> Unit,
    private val onError: ((ImagePickerException) -> Unit)? = null
) {

    private val galleryPicker by lazy {
        GalleryImagePicker(
            context = context,
            caller = caller,
            crop = crop,
            callback = onResult,
            onError = onError
        )
    }

    private val cameraPicker by lazy {
        CameraImagePicker(
            context = context,
            caller = caller,
            crop = crop,
            callback = onResult,
            onError = onError
        )
    }

    /**
     * Starts the configured picker flow.
     */
    fun launch() {
        when (source) {
            is MediaSource.Gallery -> galleryPicker.launch()
            is MediaSource.Camera -> cameraPicker.launch()
            is MediaSource.Both -> {
                // Future enhancement:
                // Provide a source chooser bottom sheet/dialog.
                galleryPicker.launch()
            }
        }
    }
}
