package com.akshayashokcode.mediakitcore.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore

object AppAvailabilityUtils {

    fun isCameraAvailable(context: Context): Boolean =
        isIntentAvailable(context, Intent(MediaStore.ACTION_IMAGE_CAPTURE))

    fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager
                .queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
                .isNotEmpty()
        } else {
            @Suppress("DEPRECATION")
            context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .isNotEmpty()
        }
    }
}
