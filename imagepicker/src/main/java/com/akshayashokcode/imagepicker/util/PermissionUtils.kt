package com.akshayashokcode.imagepicker.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

internal object PermissionUtils {
    /**
     * Checks if all required camera permissions are granted.
     */
    fun isCameraPermissionGranted(context: Context): Boolean {
        return getRequiredCameraPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns the list of permissions required for using the camera.
     */
    fun getRequiredCameraPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.CAMERA
        )
    }
}