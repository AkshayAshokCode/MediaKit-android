package com.akshayashokcode.videocompressor.builder

import android.content.Context
import android.net.Uri
import com.akshayashokcode.videocompressor.model.VideoCompressionException
import com.akshayashokcode.videocompressor.model.VideoCompressionOptions
import com.akshayashokcode.videocompressor.model.VideoCompressionResult
import com.akshayashokcode.videocompressor.util.VideoTranscoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Fluent builder for configuring and running a video compression operation.
 *
 * Does not require [ActivityResultCaller] — can be constructed anywhere.
 * Call [cancel] to abort an in-progress [compressAsync] operation.
 */
class VideoCompressorBuilder private constructor(private val context: Context) {

    private var sourceUri: Uri? = null
    private var options = VideoCompressionOptions()
    private var onProgress: ((Int) -> Unit)? = null
    private var onResult: ((VideoCompressionResult) -> Unit)? = null
    private var onError: ((VideoCompressionException) -> Unit)? = null
    private val cancelled = AtomicBoolean(false)
    private var activeJob: Job? = null

    companion object {
        fun with(context: Context): VideoCompressorBuilder =
            VideoCompressorBuilder(context.applicationContext)
    }

    fun source(uri: Uri): VideoCompressorBuilder = apply { sourceUri = uri; cancelled.set(false) }
    fun options(opts: VideoCompressionOptions): VideoCompressorBuilder = apply { options = opts }
    fun onProgress(callback: (Int) -> Unit): VideoCompressorBuilder = apply { onProgress = callback }
    fun onResult(callback: (VideoCompressionResult) -> Unit): VideoCompressorBuilder = apply { onResult = callback }
    fun onError(callback: (VideoCompressionException) -> Unit): VideoCompressorBuilder = apply { onError = callback }

    /**
     * Compresses on [Dispatchers.IO] and delivers result/progress on the main thread.
     * Returns the [Job] so callers can observe completion; use [cancel] to abort.
     */
    fun compressAsync(): Job {
        requireNotNull(onResult) { "You must provide a result callback using onResult()" }
        val uri = requireNotNull(sourceUri) { "You must provide a source URI using source()" }
        cancelled.set(false)

        val job = CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            val progressMain: ((Int) -> Unit)? = onProgress?.let { cb ->
                { pct: Int -> cb(pct) }
            }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    VideoTranscoder.transcode(context, uri, options, progressMain, cancelled::get)
                }
            }
            result.fold(
                onSuccess = { onResult?.invoke(it) },
                onFailure = { e ->
                    val ex = if (e is VideoCompressionException) e
                    else VideoCompressionException.Unknown(e.message ?: "Compression failed", e)
                    onError?.invoke(ex)
                    onResult?.invoke(VideoCompressionResult.Error(ex.message ?: "Compression failed"))
                }
            )
        }
        activeJob = job
        return job
    }

    /** Suspend API — run inside a coroutine or [LaunchedEffect]. */
    suspend fun compress(): VideoCompressionResult = withContext(Dispatchers.IO) {
        val uri = checkNotNull(sourceUri) { "You must provide a source URI using source()" }
        try {
            VideoTranscoder.transcode(context, uri, options, onProgress, cancelled::get)
        } catch (e: VideoCompressionException) {
            onError?.invoke(e)
            VideoCompressionResult.Error(e.message ?: "Compression failed")
        } catch (e: Exception) {
            val ex = VideoCompressionException.Unknown(e.message ?: "Unexpected error", e)
            onError?.invoke(ex)
            VideoCompressionResult.Error(e.message ?: "Unexpected error")
        }
    }

    /** Cancels an active [compressAsync] operation. */
    fun cancel() {
        cancelled.set(true)
        activeJob?.cancel()
        activeJob = null
    }
}
