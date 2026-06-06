package com.akshayashokcode.imagecompressor.entrypoint

import android.content.Context
import com.akshayashokcode.imagecompressor.builder.ImageCompressorBuilder

object ImageCompressor {
    fun with(context: Context): ImageCompressorBuilder =
        ImageCompressorBuilder.with(context)
}
