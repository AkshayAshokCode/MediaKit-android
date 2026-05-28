package com.akshayashokcode.imagepicker.picker

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.imagepicker.launcher.CropImageLauncher
import com.akshayashokcode.imagepicker.launcher.GalleryImageLauncher
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.model.ImagePickerResult

/**
 * Gallery-backed picker implementation.
 */
internal class GalleryImagePicker(
    private val context: Context,
    private val caller: ActivityResultCaller,
    private val crop: Boolean,
    private val callback: (ImagePickerResult) -> Unit,
    private val onError: ((ImagePickerException) -> Unit)? = null
) {

    /**
     * Handles crop flow when crop=true.
     */
    private val cropLauncher by lazy {

        CropImageLauncher(
            context = context,
            caller = caller,
            callback = callback
        )
    }

    /**
     * Handles gallery selection.
     */
    private val launcher = GalleryImageLauncher(
            context = context,
            caller = caller,
            callback = { result ->

                when (result) {

                    is ImagePickerResult.Success -> {

                        handleCropIfNeeded(result.uri)
                    }

                    else -> {
                        callback(result)
                    }
                }
            },
            onError = onError
        )

    fun launch() {
        launcher.launch()
    }

    /**
     * Launch cropper flow if enabled.
     */
    private fun handleCropIfNeeded(uri: Uri) {

        if (crop) {

            cropLauncher.launch(uri)

        } else {

            callback(
                ImagePickerResult.Success(uri)
            )
        }
    }
}
