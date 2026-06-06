package com.akshayashokcode.imagecompressor.builder

import android.content.Context
import android.net.Uri
import com.akshayashokcode.imagecompressor.model.CompressionOptions
import com.akshayashokcode.imagecompressor.model.ImageCompressionException
import com.akshayashokcode.imagecompressor.model.ImageCompressionResult
import com.akshayashokcode.imagecompressor.util.BitmapCompressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fluent builder for configuring and running an image compression operation.
 *
 * Unlike the picker builders, this does not require `ActivityResultCaller` and can
 * be constructed anywhere — including inside a Composable.
 */
class ImageCompressorBuilder private constructor(private val context: Context) {

    private var sourceUri: Uri? = null
    private var options: CompressionOptions = CompressionOptions()
    private var onResult: ((ImageCompressionResult) -> Unit)? = null
    private var onError: ((ImageCompressionException) -> Unit)? = null

    companion object {
        fun with(context: Context): ImageCompressorBuilder =
            ImageCompressorBuilder(context.applicationContext)
    }

    fun source(uri: Uri): ImageCompressorBuilder = apply { sourceUri = uri }

    fun options(opts: CompressionOptions): ImageCompressorBuilder = apply { options = opts }

    fun onResult(callback: (ImageCompressionResult) -> Unit): ImageCompressorBuilder = apply {
        onResult = callback
    }

    fun onError(callback: (ImageCompressionException) -> Unit): ImageCompressorBuilder = apply {
        onError = callback
    }

    /**
     * Compresses the image on [Dispatchers.IO] and delivers the result on the main thread
     * via the [onResult] callback. Fire-and-forget — the caller does not need to be in a
     * coroutine context.
     */
    fun compressAsync() {
        requireNotNull(onResult) { "You must provide a result callback using onResult()" }
        val uri = requireNotNull(sourceUri) { "You must provide a source URI using source()" }
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { BitmapCompressor.compress(context, uri, options) }
            }
            result.fold(
                onSuccess = { onResult?.invoke(it) },
                onFailure = { e ->
                    val ex = if (e is ImageCompressionException) e
                    else ImageCompressionException.Unknown(e.message ?: "Compression failed", e)
                    onError?.invoke(ex)
                    onResult?.invoke(ImageCompressionResult.Error(ex.message ?: "Compression failed"))
                }
            )
        }
    }

    /**
     * Compresses the image on [Dispatchers.IO]. Intended for callers already in a
     * coroutine context (e.g. a [LaunchedEffect]).
     */
    suspend fun compress(): ImageCompressionResult = withContext(Dispatchers.IO) {
        val uri = checkNotNull(sourceUri) { "You must provide a source URI using source()" }
        try {
            BitmapCompressor.compress(context, uri, options)
        } catch (e: ImageCompressionException) {
            onError?.invoke(e)
            ImageCompressionResult.Error(e.message ?: "Compression failed")
        } catch (e: Exception) {
            val ex = ImageCompressionException.Unknown(e.message ?: "Unexpected error", e)
            onError?.invoke(ex)
            ImageCompressionResult.Error(e.message ?: "Unexpected error")
        }
    }
}
