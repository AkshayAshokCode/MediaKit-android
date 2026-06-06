package com.akshayashokcode.audiorecorder.model

import com.akshayashokcode.mediakitcore.exception.MediaKitException

sealed class AudioRecorderException(message: String) : MediaKitException(message) {
    data object PermissionDenied : AudioRecorderException("RECORD_AUDIO permission was denied.")
    data object RecorderFailed : AudioRecorderException("MediaRecorder failed to initialise or start.")
    data object FileCreationFailed : AudioRecorderException("Unable to create output file.")
    class Unknown(message: String, cause: Throwable? = null) : AudioRecorderException(message) {
        init { cause?.let { initCause(it) } }
    }
}
