package com.akshayashokcode.imagepicker.picker

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.imagepicker.crop.CropLauncher
import com.akshayashokcode.imagepicker.launcher.CameraImageLauncher
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.model.ImagePickerResult

internal class CameraImagePicker(
    context: Context,
    caller: ActivityResultCaller,
    private val getCropLauncher: () -> CropLauncher?,
    private val callback: (ImagePickerResult) -> Unit,
    private val onError: ((ImagePickerException) -> Unit)? = null
) {

    private val launcher = CameraImageLauncher(
        context = context,
        caller = caller,
        callback = { result ->
            when (result) {
                is ImagePickerResult.Success -> handleCropIfNeeded(result.uri)
                is ImagePickerResult.SuccessWithBitmap -> handleCropIfNeeded(result.uri)
                else -> callback(result)
            }
        },
        onError = onError
    )

    fun launch() {
        launcher.launch()
    }

    private fun handleCropIfNeeded(uri: Uri) {
        val cropLauncher = getCropLauncher()
        if (cropLauncher != null) {
            cropLauncher.launch(uri)
        } else {
            callback(ImagePickerResult.Success(uri))
        }
    }
}
