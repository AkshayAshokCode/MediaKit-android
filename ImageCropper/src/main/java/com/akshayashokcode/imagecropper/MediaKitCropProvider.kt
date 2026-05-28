package com.akshayashokcode.imagecropper

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.akshayashokcode.imagepicker.crop.CropLauncher
import com.akshayashokcode.imagepicker.crop.ImageCropProvider
import com.akshayashokcode.imagepicker.model.ImagePickerResult

/**
 * Built-in [ImageCropProvider] that delegates to [CropperActivity].
 *
 * Pass an instance to [com.akshayashokcode.imagepicker.builder.ImagePickerBuilder.crop]
 * in `Activity.onCreate` before `setContent`.
 *
 * All parameters have sensible defaults — pass only what you want to change:
 * ```
 * MediaKitCropProvider()                                   // defaults
 * MediaKitCropProvider(CropperOptions(showRotateButtons = true))
 * MediaKitCropProvider(CropperOptions(
 *     aspectRatios = listOf(AspectRatio.Free, AspectRatio.Square),
 *     outputFormat = OutputFormat.PNG
 * ))
 * ```
 */
class MediaKitCropProvider(
    private val options: CropperOptions = CropperOptions()
) : ImageCropProvider {

    override fun createLauncher(
        context: Context,
        caller: ActivityResultCaller,
        callback: (ImagePickerResult) -> Unit
    ): CropLauncher {
        val launcher = caller.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val croppedUriString = result.data?.getStringExtra(CropperActivity.EXTRA_OUTPUT_URI)
            if (croppedUriString != null) {
                callback(ImagePickerResult.Success(croppedUriString.toUri()))
            } else {
                callback(ImagePickerResult.Cancelled)
            }
        }

        return CropLauncher { uri ->
            val intent = Intent(context, CropperActivity::class.java).apply {
                putExtra(CropperActivity.EXTRA_INPUT_URI, uri.toString())
                putExtra(CropperActivity.EXTRA_OPTIONS, options)
            }
            launcher.launch(intent)
        }
    }
}
