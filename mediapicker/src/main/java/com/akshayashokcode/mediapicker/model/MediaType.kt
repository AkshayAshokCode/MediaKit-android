package com.akshayashokcode.mediapicker.model

sealed class MediaType {
    data object Image : MediaType()
    data object Video : MediaType()
    data object Audio : MediaType()
    data object Document : MediaType()
    /** Convenience — equivalent to all four types. */
    data object All : MediaType()
}
