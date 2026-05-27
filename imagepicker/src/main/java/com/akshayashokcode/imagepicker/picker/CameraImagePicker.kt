package com.akshayashokcode.imagepicker.picker

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.imagepicker.launcher.CameraImageLauncher
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.model.ImagePickerResult

/**
 * Camera-backed picker implementation.
 */
internal class CameraImagePicker(
    private val context: Context,
    private val caller: ActivityResultCaller,
    private val callback: (ImagePickerResult) -> Unit,
    private val onError: ((ImagePickerException) -> Unit)? = null
) {

    private val launcher by lazy {
        CameraImageLauncher(
            context = context,
            caller = caller,
            callback = callback,
            onError = onError
        )
    }

    fun launch() {
        launcher.launch()
    }
}
