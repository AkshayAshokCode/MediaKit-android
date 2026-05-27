package com.akshayashokcode.imagepicker.entrypoint

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.imagepicker.builder.ImagePickerBuilder

/**
 * Public entry point for launching MediaKit image picker flows.
 */
object ImagePicker {

    fun with(
        context: Context,
        caller: ActivityResultCaller
    ): ImagePickerBuilder {
        return ImagePickerBuilder.with(context, caller)
    }
}
