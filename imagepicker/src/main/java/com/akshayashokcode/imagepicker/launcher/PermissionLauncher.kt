package com.akshayashokcode.imagepicker.launcher

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.util.PermissionUtils

internal class PermissionLauncher(
    private val context: Context,
    caller: ActivityResultCaller,
    private val onResult: (granted: Boolean) -> Unit,
    private val onError: ((ImagePickerException) -> Unit)? = null
) {

    private val permissionLauncher = caller.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        try {
            val granted = permissions.all { it.value }
            onResult(granted)
        } catch (e: Exception) {
            onError?.invoke(ImagePickerException.PermissionDenied)
            onResult(false)
        }
    }

    fun launchCameraPermissions() {
        try {
            val permissions = PermissionUtils.getRequiredCameraPermissions()
            permissionLauncher.launch(permissions)
        } catch (e: Exception) {
            onError?.invoke(ImagePickerException.PermissionDenied)
            onResult(false)
        }
    }

    fun isCameraPermissionGranted(): Boolean {
        return PermissionUtils.isCameraPermissionGranted(context)
    }
}