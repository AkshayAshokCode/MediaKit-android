package com.akshayashokcode.imagepicker.builder

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.imagepicker.coordinator.ImagePickerCoordinator
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.model.ImagePickerResult
import com.akshayashokcode.imagepicker.model.MediaSource

/**
 * Fluent builder used to configure and launch the MediaKit image picker flow.
 *
 * Consumers should create instances through [with] to keep the public API
 * consistent and future-proof.
 */
class ImagePickerBuilder private constructor(
    private val context: Context,
    private val caller: ActivityResultCaller
) {
    private var source: MediaSource = MediaSource.Gallery

    // Reserved for future cropper module integration.
    private var crop: Boolean = false

    private var onResult: ((ImagePickerResult) -> Unit)? = null
    private var onError: ((ImagePickerException) -> Unit)? = null

    private val coordinator by lazy {

        ImagePickerCoordinator(
            context = context,
            caller = caller,
            source = source,
            crop = crop,
            onResult = onResult!!,
            onError = onError
        )
    }

    companion object {
        /**
         * Entry point to start building an image picker flow.
         *
         * @param context Application or activity context.
         * @param caller ActivityResultCaller (Activity or Fragment).
         */
        fun with(
            context: Context,
            caller: ActivityResultCaller
        ): ImagePickerBuilder {
            return ImagePickerBuilder(context, caller)
        }
    }

    /**
     * Configure the media source.
     */
    fun source(source: MediaSource): ImagePickerBuilder = apply {
        this.source = source
    }

    /**
     * Enables optional cropping support.
     *
     * Cropper integration is planned for a future MediaKit release.
     */
    fun crop(enable: Boolean): ImagePickerBuilder = apply {
        this.crop = enable
    }

    /**
     * Callback invoked with picker results.
     */
    fun onResult(callback: (ImagePickerResult) -> Unit): ImagePickerBuilder = apply {
        this.onResult = callback
    }

    /**
     * Optional callback invoked for picker failures.
     */
    fun onError(callback: (ImagePickerException) -> Unit): ImagePickerBuilder = apply {
        this.onError = callback
    }

    /**
     * Launches the picker flow with the current configuration.
     */
    fun launch() {
        requireNotNull(onResult) {
            "You must provide a result callback using onResult()"
        }

        coordinator.launch()
    }
}
