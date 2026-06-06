package com.akshayashokcode.audiorecorder.entrypoint

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.audiorecorder.builder.AudioRecorderBuilder
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi

object AudioRecorder {
    @ExperimentalMediaKitApi
    fun with(context: Context, caller: ActivityResultCaller): AudioRecorderBuilder =
        AudioRecorderBuilder.with(context, caller)
}
