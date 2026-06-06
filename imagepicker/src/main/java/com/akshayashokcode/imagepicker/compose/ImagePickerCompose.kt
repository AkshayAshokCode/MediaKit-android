package com.akshayashokcode.imagepicker.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.akshayashokcode.imagepicker.coordinator.showSourceChooserDialog
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi
import com.akshayashokcode.imagepicker.model.ImagePickerResult
import com.akshayashokcode.imagepicker.model.MediaSource
import com.akshayashokcode.imagepicker.util.ImageOrientationUtils
import com.akshayashokcode.imagepicker.util.PermissionUtils
import com.akshayashokcode.mediakitcore.util.TempFileManager

/**
 * Compose-native image picker. Unlike [com.akshayashokcode.imagepicker.entrypoint.ImagePicker],
 * this can be called anywhere inside a `@Composable` — no `onCreate`-before-`setContent`
 * restriction.
 *
 * @param source Gallery, Camera, or Both (shows a source-chooser dialog).
 * @param onResult Invoked on the main thread with the picker result.
 */
@ExperimentalMediaKitApi
@Composable
fun rememberImagePicker(
    source: MediaSource = MediaSource.Gallery,
    onResult: (ImagePickerResult) -> Unit
): ImagePickerHandle {
    val context = LocalContext.current
    val currentSource = rememberUpdatedState(source)
    val currentOnResult = rememberUpdatedState(onResult)
    val tempUri: MutableState<Uri?> = remember { mutableStateOf(null) }
    val authority = "${context.packageName}.imagepicker.provider"

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) currentOnResult.value(ImagePickerResult.Success(uri))
        else currentOnResult.value(ImagePickerResult.Cancelled)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempUri.value
        if (success && uri != null) {
            val bitmap = runCatching {
                ImageOrientationUtils.getOrientedBitmap(context.contentResolver, uri)
            }.getOrNull()
            if (bitmap != null) {
                currentOnResult.value(ImagePickerResult.SuccessWithBitmap(uri, bitmap))
            } else {
                TempFileManager.deleteTempFile(context, uri)
                currentOnResult.value(ImagePickerResult.Error("Failed to decode captured image"))
            }
        } else {
            tempUri.value?.let { TempFileManager.deleteTempFile(context, it) }
            currentOnResult.value(ImagePickerResult.Cancelled)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.all { it }) launchCameraWithTempUri(context, authority, tempUri, cameraLauncher)
        else currentOnResult.value(ImagePickerResult.Error("Camera permission denied"))
    }

    val launchCamera = {
        if (PermissionUtils.isCameraPermissionGranted(context)) {
            launchCameraWithTempUri(context, authority, tempUri, cameraLauncher)
        } else {
            permissionLauncher.launch(PermissionUtils.getRequiredCameraPermissions())
        }
    }

    return remember {
        ImagePickerHandle {
            when (currentSource.value) {
                is MediaSource.Gallery -> galleryLauncher.launch("image/*")
                is MediaSource.Camera -> launchCamera()
                is MediaSource.Both -> showSourceChooserDialog(
                    context = context,
                    onGallery = { galleryLauncher.launch("image/*") },
                    onCamera = launchCamera
                )
            }
        }
    }
}

private fun launchCameraWithTempUri(
    context: android.content.Context,
    authority: String,
    tempUri: MutableState<Uri?>,
    launcher: androidx.activity.result.ActivityResultLauncher<Uri>
) {
    val uri = TempFileManager.createTempImageUri(context, authority) ?: return
    tempUri.value = uri
    launcher.launch(uri)
}
