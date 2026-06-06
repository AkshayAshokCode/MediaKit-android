package com.akshayashokcode.mediapicker.entrypoint

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.mediapicker.builder.MediaPickerBuilder

object MediaPicker {
    fun with(context: Context, caller: ActivityResultCaller): MediaPickerBuilder =
        MediaPickerBuilder.with(context, caller)
}
