package com.akshayashokcode.imagepicker.launcher

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.model.ImagePickerResult

internal class GalleryImageLauncher(
    caller: ActivityResultCaller,
    private val callback: (ImagePickerResult) -> Unit,
    private val onError: ((ImagePickerException) -> Unit)? = null
) {

    private val launcher =
        caller.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                callback(ImagePickerResult.Success(uri))
            } else {
                callback(ImagePickerResult.Cancelled)
            }
        }

    fun launch() {
        try {
            launcher.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            onError?.invoke(ImagePickerException.AppNotFound)
            callback(ImagePickerResult.Error("No app found to handle image selection"))
        } catch (e: Exception) {
            onError?.invoke(ImagePickerException.Unknown("Unexpected error: ${e.message}"))
            callback(ImagePickerResult.Error("Unexpected error during image selection"))
        }
    }
}