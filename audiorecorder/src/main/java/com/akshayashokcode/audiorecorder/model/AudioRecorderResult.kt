package com.akshayashokcode.audiorecorder.model

import android.net.Uri

sealed class AudioRecorderResult {
    data class Success(val uri: Uri, val durationMs: Long) : AudioRecorderResult()
    data object Cancelled : AudioRecorderResult()
    data class Error(val message: String) : AudioRecorderResult()
}
