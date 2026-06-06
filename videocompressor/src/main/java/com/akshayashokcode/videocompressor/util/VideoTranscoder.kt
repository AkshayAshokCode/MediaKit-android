package com.akshayashokcode.videocompressor.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.view.Surface
import androidx.core.net.toUri
import com.akshayashokcode.videocompressor.model.VideoCompressionException
import com.akshayashokcode.videocompressor.model.VideoCompressionOptions
import com.akshayashokcode.videocompressor.model.VideoCompressionResult
import java.io.File
import java.nio.ByteBuffer

internal object VideoTranscoder {

    private const val TIMEOUT_US = 10_000L
    private const val OUTPUT_MIME = "video/avc"
    private const val OUTPUT_DIR = "mediakit-compressed-video"

    fun transcode(
        context: Context,
        sourceUri: Uri,
        options: VideoCompressionOptions,
        onProgress: ((Int) -> Unit)?,
        isCancelled: () -> Boolean
    ): VideoCompressionResult {
        val extractor = MediaExtractor()
        var decoder: MediaCodec? = null
        var encoder: MediaCodec? = null
        var encoderSurface: Surface? = null
        var muxer: MediaMuxer? = null
        var muxerStarted = false
        var outputFile: File? = null

        try {
            extractor.setDataSource(context, sourceUri, null)

            val originalSize = context.contentResolver
                .openFileDescriptor(sourceUri, "r")?.use { it.statSize } ?: 0L

            // Locate video and audio tracks
            var videoTrackIdx = -1
            var audioTrackIdx = -1
            for (i in 0 until extractor.trackCount) {
                val mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: continue
                if (videoTrackIdx < 0 && mime.startsWith("video/")) videoTrackIdx = i
                if (audioTrackIdx < 0 && mime.startsWith("audio/")) audioTrackIdx = i
            }
            if (videoTrackIdx < 0) return VideoCompressionResult.Error("No video track found in source")

            extractor.selectTrack(videoTrackIdx)
            val srcFmt = extractor.getTrackFormat(videoTrackIdx)
            val srcMime = srcFmt.getString(MediaFormat.KEY_MIME)!!
            val srcW = srcFmt.getInteger(MediaFormat.KEY_WIDTH)
            val srcH = srcFmt.getInteger(MediaFormat.KEY_HEIGHT)
            val rotation = if (srcFmt.containsKey(MediaFormat.KEY_ROTATION))
                srcFmt.getInteger(MediaFormat.KEY_ROTATION) else 0

            // Duration for progress reporting
            val totalUs = if (srcFmt.containsKey(MediaFormat.KEY_DURATION))
                srcFmt.getLong(MediaFormat.KEY_DURATION)
            else MediaMetadataRetriever().use { r ->
                r.setDataSource(context, sourceUri)
                r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong()?.times(1000L) ?: 0L
            }

            // Compute coded output dimensions (must be even; respect rotation swap)
            val (dispW, dispH) = if (rotation == 90 || rotation == 270) srcH to srcW else srcW to srcH
            val scale = minOf(options.maxWidth.toFloat() / dispW, options.maxHeight.toFloat() / dispH)
                .coerceAtMost(1f)
            val outDispW = (dispW * scale).toInt().evenDown()
            val outDispH = (dispH * scale).toInt().evenDown()
            val (outCodedW, outCodedH) =
                if (rotation == 90 || rotation == 270) outDispH to outDispW else outDispW to outDispH

            // Output file
            val dir = File(context.cacheDir, OUTPUT_DIR).also { it.mkdirs() }
            val file = File(dir, "video_${System.currentTimeMillis()}.mp4")
            outputFile = file

            // Build encoder
            val encFmt = MediaFormat.createVideoFormat(OUTPUT_MIME, outCodedW, outCodedH).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, options.videoBitrateBps)
                setInteger(MediaFormat.KEY_FRAME_RATE, options.frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            }
            encoder = MediaCodec.createEncoderByType(OUTPUT_MIME).also { enc ->
                enc.configure(encFmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                encoderSurface = enc.createInputSurface()
                enc.start()
            }

            // Build decoder → output to encoder's surface
            decoder = MediaCodec.createDecoderByType(srcMime).also { dec ->
                dec.configure(srcFmt, encoderSurface, null, 0)
                dec.start()
            }

            // Muxer
            val mx = MediaMuxer(file.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = mx
            if (rotation != 0) mx.setOrientationHint(rotation)

            val audioFmt = if (audioTrackIdx >= 0) extractor.getTrackFormat(audioTrackIdx) else null
            var videoMuxerTrack = -1
            var audioMuxerTrack = -1

            // Pipeline loop
            val bufInfo = MediaCodec.BufferInfo()
            var inputDone = false
            var decoderDone = false
            var encoderDone = false

            while (!encoderDone) {
                if (isCancelled()) return VideoCompressionResult.Cancelled

                // Feed extractor → decoder
                if (!inputDone) {
                    val idx = decoder!!.dequeueInputBuffer(TIMEOUT_US)
                    if (idx >= 0) {
                        val buf = decoder!!.getInputBuffer(idx)!!
                        val n = extractor.readSampleData(buf, 0)
                        if (n < 0) {
                            decoder!!.queueInputBuffer(idx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            val pts = extractor.sampleTime
                            decoder!!.queueInputBuffer(idx, 0, n, pts, 0)
                            if (totalUs > 0) {
                                val pct = ((pts.toFloat() / totalUs) * 90).toInt().coerceIn(0, 90)
                                onProgress?.invoke(pct)
                            }
                            extractor.advance()
                        }
                    }
                }

                // Drain decoder → surface → encoder (implicit via Surface)
                if (!decoderDone) {
                    val dIdx = decoder!!.dequeueOutputBuffer(bufInfo, TIMEOUT_US)
                    if (dIdx >= 0) {
                        val render = bufInfo.size != 0
                        decoder!!.releaseOutputBuffer(dIdx, render) // render=true sends frame to encoder surface
                        if (bufInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            encoder!!.signalEndOfInputStream()
                            decoderDone = true
                        }
                    }
                }

                // Drain encoder → muxer
                val eIdx = encoder!!.dequeueOutputBuffer(bufInfo, TIMEOUT_US)
                when {
                    eIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        videoMuxerTrack = mx.addTrack(encoder!!.outputFormat)
                        if (audioFmt != null) audioMuxerTrack = mx.addTrack(audioFmt)
                        mx.start()
                        muxerStarted = true
                    }
                    eIdx >= 0 -> {
                        val data = encoder!!.getOutputBuffer(eIdx)!!
                        val isConfig = bufInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
                        if (muxerStarted && bufInfo.size > 0 && !isConfig) {
                            mx.writeSampleData(videoMuxerTrack, data, bufInfo)
                        }
                        encoder!!.releaseOutputBuffer(eIdx, false)
                        if (bufInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            encoderDone = true
                        }
                    }
                }
            }

            // Audio passthrough — write all audio samples after video is done
            if (audioTrackIdx >= 0 && muxerStarted) {
                val audioExtractor = MediaExtractor()
                audioExtractor.setDataSource(context, sourceUri, null)
                audioExtractor.selectTrack(audioTrackIdx)

                val audioBuf = ByteBuffer.allocate(256 * 1024)
                val audioBufInfo = MediaCodec.BufferInfo()
                while (!isCancelled()) {
                    val n = audioExtractor.readSampleData(audioBuf, 0)
                    if (n < 0) break
                    audioBufInfo.apply {
                        offset = 0; size = n
                        presentationTimeUs = audioExtractor.sampleTime
                        flags = audioExtractor.sampleFlags
                    }
                    mx.writeSampleData(audioMuxerTrack, audioBuf, audioBufInfo)
                    audioExtractor.advance()
                }
                audioExtractor.release()
            }

            if (isCancelled()) return VideoCompressionResult.Cancelled
            onProgress?.invoke(100)

            return VideoCompressionResult.Success(
                uri = file.toUri(),
                originalSizeBytes = originalSize,
                compressedSizeBytes = file.length()
            )
        } catch (e: Exception) {
            outputFile?.delete()
            throw VideoCompressionException.Unknown(e.message ?: "Compression failed", e)
        } finally {
            runCatching { decoder?.stop(); decoder?.release() }
            runCatching { encoder?.stop(); encoder?.release() }
            runCatching { encoderSurface?.release() }
            runCatching { extractor.release() }
            if (muxerStarted) runCatching { muxer?.stop() }
            runCatching { muxer?.release() }
        }
    }

    private fun Int.evenDown() = if (this % 2 == 0) this else this - 1
}
