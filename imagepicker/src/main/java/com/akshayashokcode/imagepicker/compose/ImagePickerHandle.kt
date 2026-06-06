package com.akshayashokcode.imagepicker.compose

/**
 * Handle returned by [rememberImagePicker]. Call [launch] from any click handler.
 */
class ImagePickerHandle internal constructor(internal val onLaunch: () -> Unit) {
    fun launch() = onLaunch()
}
