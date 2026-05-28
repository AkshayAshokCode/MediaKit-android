package com.akshayashokcode.imagepicker.coordinator

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.imagepicker.crop.CropLauncher
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.model.ImagePickerResult
import com.akshayashokcode.imagepicker.model.MediaSource
import com.akshayashokcode.imagepicker.picker.CameraImagePicker
import com.akshayashokcode.imagepicker.picker.GalleryImagePicker

/**
 * Coordinates the image picker flow and delegates execution to the appropriate
 * picker implementation.
 *
 * Both pickers are constructed eagerly so registerForActivityResult is called
 * before onStart(), as Android requires.
 */
internal class ImagePickerCoordinator(
    context: Context,
    caller: ActivityResultCaller,
    private val getSource: () -> MediaSource,
    getCropLauncher: () -> CropLauncher?,
    onResult: (ImagePickerResult) -> Unit,
    onError: ((ImagePickerException) -> Unit)? = null
) {

    private val galleryPicker = GalleryImagePicker(
        context = context,
        caller = caller,
        getCropLauncher = getCropLauncher,
        callback = onResult,
        onError = onError
    )

    private val cameraPicker = CameraImagePicker(
        context = context,
        caller = caller,
        getCropLauncher = getCropLauncher,
        callback = onResult,
        onError = onError
    )

    fun launch() {
        when (getSource()) {
            is MediaSource.Gallery -> galleryPicker.launch()
            is MediaSource.Camera -> cameraPicker.launch()
            is MediaSource.Both -> {
                // Future: show a source chooser bottom sheet.
                galleryPicker.launch()
            }
        }
    }
}
