package com.akshayashokcode.imagepicker.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore

internal object AppAvailabilityUtils {
    fun isGalleryAvailable(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        val resolved = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved.isNotEmpty()
    }

    fun isCameraAvailable(context: Context): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resolved = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved.isNotEmpty()
    }
}