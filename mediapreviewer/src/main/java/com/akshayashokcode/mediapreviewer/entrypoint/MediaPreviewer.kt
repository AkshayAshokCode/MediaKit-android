package com.akshayashokcode.mediapreviewer.entrypoint

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi
import com.akshayashokcode.mediapreviewer.builder.MediaPreviewerBuilder

object MediaPreviewer {
    @ExperimentalMediaKitApi
    fun with(context: Context, caller: ActivityResultCaller): MediaPreviewerBuilder =
        MediaPreviewerBuilder(context, caller)
}
