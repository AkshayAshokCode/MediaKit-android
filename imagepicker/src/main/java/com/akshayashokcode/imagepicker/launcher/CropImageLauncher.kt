package com.akshayashokcode.imagepicker.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import com.akshayashokcode.imagecropper.CropperActivity
import com.akshayashokcode.imagepicker.model.ImagePickerResult

internal class CropImageLauncher(
    private val context: Context,
    caller: ActivityResultCaller,
    private val callback: (ImagePickerResult) -> Unit
) {

    private var pendingUri: Uri? = null

    private val launcher = caller.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val data = result.data

        val croppedUriString =
            data?.getStringExtra(
                CropperActivity.EXTRA_OUTPUT_URI
            )

        if (croppedUriString != null) {

            callback(
                ImagePickerResult.Success(
                    Uri.parse(croppedUriString)
                )
            )

        } else {

            callback(ImagePickerResult.Cancelled)
        }
    }

    fun launch(uri: Uri) {

        pendingUri = uri

        val intent = Intent(
            context,
            CropperActivity::class.java
        ).apply {

            putExtra(
                CropperActivity.EXTRA_INPUT_URI,
                uri.toString()
            )
        }

        launcher.launch(intent)
    }
}
