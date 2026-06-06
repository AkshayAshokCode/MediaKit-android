package com.akshayashokcode.audiorecorder.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toUri
import com.akshayashokcode.audiorecorder.activity.RecorderActivity
import com.akshayashokcode.audiorecorder.model.AudioRecorderOptions
import com.akshayashokcode.audiorecorder.model.AudioRecorderResult

internal class AudioRecorderContract :
    ActivityResultContract<AudioRecorderOptions, AudioRecorderResult>() {

    override fun createIntent(context: Context, input: AudioRecorderOptions): Intent =
        Intent(context, RecorderActivity::class.java).apply {
            putExtra(RecorderActivity.EXTRA_OPTIONS, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): AudioRecorderResult {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            val uriStr = intent.getStringExtra(RecorderActivity.EXTRA_RESULT_URI)
                ?: return AudioRecorderResult.Cancelled
            val duration = intent.getLongExtra(RecorderActivity.EXTRA_RESULT_DURATION_MS, 0L)
            return AudioRecorderResult.Success(uriStr.toUri(), duration)
        }
        return AudioRecorderResult.Cancelled
    }
}
