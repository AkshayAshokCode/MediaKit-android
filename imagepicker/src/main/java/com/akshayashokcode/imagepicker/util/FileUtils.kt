package com.akshayashokcode.imagepicker.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

internal object FileUtils {
    fun createTempImageUri(context: Context): Uri? {
        return try {
            val file = File.createTempFile("camera_image_", ".jpg", context.cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
            FileProvider.getUriForFile(context, "${context.packageName}.imagepicker.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteTempFile(context: Context, uri: Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}