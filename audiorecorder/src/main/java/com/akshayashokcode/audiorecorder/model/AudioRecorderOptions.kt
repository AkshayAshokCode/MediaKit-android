package com.akshayashokcode.audiorecorder.model

import java.io.Serializable

data class AudioRecorderOptions(
    /** Maximum recording duration in seconds. 0 means no limit. */
    val maxDurationSeconds: Int = 0,
    val format: AudioOutputFormat = AudioOutputFormat.AAC_M4A,
    val showWaveform: Boolean = true
) : Serializable
