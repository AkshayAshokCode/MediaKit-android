package com.akshayashokcode.videocompressor.entrypoint

import android.content.Context
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi
import com.akshayashokcode.videocompressor.builder.VideoCompressorBuilder

object VideoCompressor {
    @ExperimentalMediaKitApi
    fun with(context: Context): VideoCompressorBuilder = VideoCompressorBuilder.with(context)
}
