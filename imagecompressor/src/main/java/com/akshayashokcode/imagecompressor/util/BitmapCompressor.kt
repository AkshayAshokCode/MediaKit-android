package com.akshayashokcode.imagecompressor.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.akshayashokcode.imagecompressor.model.CompressionOptions
import com.akshayashokcode.imagecompressor.model.ImageCompressionException
import com.akshayashokcode.imagecompressor.model.ImageCompressionResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

internal object BitmapCompressor {

    private const val OUTPUT_DIR = "mediakit-compressed"
    private const val MIN_QUALITY = 30

    fun compress(context: Context, sourceUri: Uri, options: CompressionOptions): ImageCompressionResult {
        val resolver = context.contentResolver

        val originalSize = try {
            resolver.openFileDescriptor(sourceUri, "r")?.use { it.statSize } ?: 0L
        } catch (_: Exception) { 0L }

        val outputDir = File(context.cacheDir, OUTPUT_DIR).also { it.mkdirs() }
        val cacheFile = buildCacheFile(outputDir, sourceUri, options)

        if (cacheFile.exists() && cacheFile.length() > 0L) {
            return ImageCompressionResult.Success(
                uri = cacheFile.toUri(),
                originalSizeBytes = originalSize,
                compressedSizeBytes = cacheFile.length()
            )
        }

        val bitmap = decodeSampled(context, sourceUri, options.maxWidth, options.maxHeight)
            ?: throw ImageCompressionException.DecodingFailed

        try {
            val quality = if (options.maxFileSizeBytes != null) {
                findQualityForSize(bitmap, options)
            } else {
                options.quality
            }

            cacheFile.outputStream().buffered().use { out ->
                val success = bitmap.compress(options.format, quality, out)
                if (!success) throw ImageCompressionException.EncodingFailed
            }

            if (options.preserveExif && options.format == Bitmap.CompressFormat.JPEG) {
                copyExif(context, sourceUri, cacheFile)
            }
        } finally {
            bitmap.recycle()
        }

        return ImageCompressionResult.Success(
            uri = cacheFile.toUri(),
            originalSizeBytes = originalSize,
            compressedSizeBytes = cacheFile.length()
        )
    }

    private fun decodeSampled(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): Bitmap? {
        val resolver = context.contentResolver
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }

        opts.inSampleSize = calculateInSampleSize(opts.outWidth, opts.outHeight, maxWidth, maxHeight)
        opts.inJustDecodeBounds = false

        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Int {
        var sampleSize = 1
        if (height > maxHeight || width > maxWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / sampleSize >= maxHeight && halfWidth / sampleSize >= maxWidth) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }

    private fun findQualityForSize(bitmap: Bitmap, options: CompressionOptions): Int {
        val maxBytes = options.maxFileSizeBytes ?: return options.quality
        var quality = options.quality
        val buffer = ByteArrayOutputStream()
        while (quality >= MIN_QUALITY) {
            buffer.reset()
            bitmap.compress(options.format, quality, buffer)
            if (buffer.size() <= maxBytes) break
            quality -= 5
        }
        return quality.coerceAtLeast(MIN_QUALITY)
    }

    private fun copyExif(context: Context, sourceUri: Uri, destFile: File) {
        try {
            val sourceExif = context.contentResolver.openInputStream(sourceUri)?.use { ExifInterface(it) }
                ?: return
            val destExif = ExifInterface(destFile.absolutePath)
            val tags = listOf(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF
            )
            for (tag in tags) {
                sourceExif.getAttribute(tag)?.let { destExif.setAttribute(tag, it) }
            }
            destExif.saveAttributes()
        } catch (_: IOException) { }
    }

    private fun buildCacheFile(dir: File, uri: Uri, options: CompressionOptions): File {
        val key = "${uri.hashCode()}_${options.hashCode()}"
        val ext = when {
            options.format == Bitmap.CompressFormat.PNG -> "png"
            options.format.name.contains("WEBP") -> "webp"
            else -> "jpg"
        }
        return File(dir, "compressed_${key}.$ext")
    }
}
