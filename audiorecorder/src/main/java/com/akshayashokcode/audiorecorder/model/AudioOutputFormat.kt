package com.akshayashokcode.audiorecorder.model

import java.io.Serializable

enum class AudioOutputFormat : Serializable {
    /** AAC audio in an MPEG-4 container (.m4a). Supported on all API levels. */
    AAC_M4A,
    /** PCM WAV. Requires API 34+; falls back to AAC_M4A on older devices. */
    WAV
}
