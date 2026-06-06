package com.akshayashokcode.mediapicker.compose

/** Handle returned by [rememberMediaPicker]. Call [launch] from any click handler. */
class MediaPickerHandle internal constructor(internal val onLaunch: () -> Unit) {
    fun launch() = onLaunch()
}
