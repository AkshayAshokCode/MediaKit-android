package com.akshayashokcode.imagepicker.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

internal object ImageOrientationUtils {

    fun getOrientedBitmap(contentResolver: ContentResolver, imageUri: Uri): Bitmap? {
        return try {
            val originalBitmap = decodeBitmap(contentResolver, imageUri) ?: return null
            val exif = contentResolver.openInputStream(imageUri)?.use { ExifInterface(it) }
                ?: return originalBitmap
            rotateBitmapIfNeeded(originalBitmap, exif)
        } catch (e: IOException) {
            Log.e("ImageOrientationUtils", "Failed to get oriented bitmap", e)
            null
        }
    }

    private fun decodeBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            Log.e("ImageOrientationUtils", "Failed to decode bitmap", e)
            null
        }
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, exif: ExifInterface): Bitmap {
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        return if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }
}