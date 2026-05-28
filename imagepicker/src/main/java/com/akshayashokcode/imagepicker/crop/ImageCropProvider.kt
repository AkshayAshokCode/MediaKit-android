package com.akshayashokcode.imagepicker.crop

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.imagepicker.model.ImagePickerResult

/**
 * Contract for supplying a crop step to the image picker flow.
 *
 * Implement this interface (or use [com.akshayashokcode.imagecropper.MediaKitCropProvider]
 * from the imagecropper module) and pass the instance to
 * [com.akshayashokcode.imagepicker.builder.ImagePickerBuilder.crop].
 *
 * [createLauncher] is called once during picker construction, before the
 * activity reaches STARTED, so [ActivityResultCaller.registerForActivityResult]
 * is safe to call inside it.
 */
interface ImageCropProvider {
    fun createLauncher(
        context: Context,
        caller: ActivityResultCaller,
        callback: (ImagePickerResult) -> Unit
    ): CropLauncher
}

/**
 * Thin handle returned by [ImageCropProvider.createLauncher].
 * Called with the raw image URI once gallery/camera selection succeeds.
 */
fun interface CropLauncher {
    fun launch(uri: Uri)
}
