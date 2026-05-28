package com.akshayashokcode.imagepicker.launcher

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.model.ImagePickerResult
import com.akshayashokcode.imagepicker.util.AppAvailabilityUtils
import com.akshayashokcode.imagepicker.util.FileUtils
import com.akshayashokcode.imagepicker.util.ImageOrientationUtils

internal class CameraImageLauncher(
    private val context: Context,
    caller: ActivityResultCaller,
    private val callback: (ImagePickerResult) -> Unit,
    private val onError: ((ImagePickerException) -> Unit)? = null
) {

    private var tempImageUri: Uri? = null

    private val permissionLauncher = PermissionLauncher(
        context = context,
        caller = caller,
        onResult = { granted ->
            if (granted) {
                launchCamera()
            } else {
                onError?.invoke(ImagePickerException.PermissionDenied)
                callback(ImagePickerResult.Error("Camera permission denied"))
            }
        },
        onError = { error ->
            onError?.invoke(error)
            callback(ImagePickerResult.Error(error.message ?: "Permission error"))
        }
    )

    private val launcher =
        caller.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uri = tempImageUri
            if (success && uri != null) {
                try {
                    val rotatedBitmap = ImageOrientationUtils
                        .getOrientedBitmap(context.contentResolver, uri)

                    if (rotatedBitmap != null) {
                        callback(ImagePickerResult.SuccessWithBitmap(uri = uri, bitmap = rotatedBitmap))
                    } else {
                        onError?.invoke(ImagePickerException.DecodingFailed)
                        callback(ImagePickerResult.Error("Failed to decode or rotate image"))
                    }
                } catch (e: Exception) {
                    onError?.invoke(ImagePickerException.DecodingFailed)
                    callback(ImagePickerResult.Error("Image rotation failed: ${e.message}"))
                }
            } else {
                callback(ImagePickerResult.Cancelled)
                tempImageUri?.let { FileUtils.deleteTempFile(context, it) }
            }
        }

    fun launch() {
        if (!AppAvailabilityUtils.isCameraAvailable(context)) {
            onError?.invoke(ImagePickerException.AppNotFound)
            callback(ImagePickerResult.Error("No camera app found to capture image"))
            return
        }

        if (permissionLauncher.isCameraPermissionGranted()) {
            launchCamera()
        } else {
            permissionLauncher.launchCameraPermissions()
        }
    }

    private fun launchCamera() {
        val imageUri = FileUtils.createTempImageUri(context)
        if (imageUri == null) {
            onError?.invoke(ImagePickerException.FileCreationFailed)
            callback(ImagePickerResult.Error("Unable to create temporary file for captured image"))
            return
        }
        tempImageUri = imageUri

        try {
            launcher.launch(imageUri)
        } catch (e: Exception) {
            onError?.invoke(ImagePickerException.IntentFailed)
            callback(ImagePickerResult.Error("No camera found to handle image capture"))
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        tempImageUri?.let {
            outState.putString(KEY_TEMP_URI, it.toString())
        }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.getString(KEY_TEMP_URI)?.let {
            tempImageUri = it.toUri()
        }
    }

    companion object {
        private const val KEY_TEMP_URI = "camera_temp_uri"
    }
}
