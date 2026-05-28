package com.akshayashokcode.imagepicker.builder

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.imagepicker.coordinator.ImagePickerCoordinator
import com.akshayashokcode.imagepicker.crop.CropLauncher
import com.akshayashokcode.imagepicker.crop.ImageCropProvider
import com.akshayashokcode.imagepicker.model.ImagePickerException
import com.akshayashokcode.imagepicker.model.ImagePickerResult
import com.akshayashokcode.imagepicker.model.MediaSource

/**
 * Fluent builder used to configure and launch the MediaKit image picker flow.
 *
 * Consumers should create instances through [with] to keep the public API
 * consistent and future-proof.
 *
 * **Lifecycle requirement:** construct the builder (i.e. call [with] and the
 * full fluent chain including [crop]) in `Activity.onCreate` *before*
 * `setContent`, so that all `registerForActivityResult` calls happen before
 * the activity reaches STARTED.
 */
class ImagePickerBuilder private constructor(
    private val context: Context,
    private val caller: ActivityResultCaller
) {
    private var source: MediaSource = MediaSource.Gallery
    private var cropLauncher: CropLauncher? = null

    private var onResult: ((ImagePickerResult) -> Unit)? = null
    private var onError: ((ImagePickerException) -> Unit)? = null

    // Coordinator is created eagerly so gallery/camera registerForActivityResult
    // calls happen before onStart(). Lambda indirection lets source/callbacks
    // remain configurable via the fluent setters after construction.
    // cropLauncher is managed separately — it is registered when crop() is called.
    private val coordinator = ImagePickerCoordinator(
        context = context,
        caller = caller,
        getSource = { source },
        getCropLauncher = { cropLauncher },
        onResult = { result -> onResult?.invoke(result) },
        onError = { error -> onError?.invoke(error) }
    )

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
        ): ImagePickerBuilder = ImagePickerBuilder(context, caller)
    }

    /** Configure the media source. */
    fun source(source: MediaSource): ImagePickerBuilder = apply {
        this.source = source
    }

    /**
     * Enables cropping after image selection.
     *
     * [provider] is responsible for registering the activity result launcher and
     * opening its crop UI. Pass [com.akshayashokcode.imagecropper.MediaKitCropProvider]
     * to use the built-in imagecropper module, or supply your own implementation.
     *
     * This call immediately invokes [ImageCropProvider.createLauncher], which
     * calls [ActivityResultCaller.registerForActivityResult] — so [crop] must
     * be called before the activity reaches STARTED (i.e. before `setContent`).
     */
    fun crop(provider: ImageCropProvider): ImagePickerBuilder = apply {
        cropLauncher = provider.createLauncher(
            context = context,
            caller = caller,
            callback = { result -> onResult?.invoke(result) }
        )
    }

    /** Callback invoked with picker results. */
    fun onResult(callback: (ImagePickerResult) -> Unit): ImagePickerBuilder = apply {
        this.onResult = callback
    }

    /** Optional callback invoked for picker failures. */
    fun onError(callback: (ImagePickerException) -> Unit): ImagePickerBuilder = apply {
        this.onError = callback
    }

    /** Launches the picker flow with the current configuration. */
    fun launch() {
        requireNotNull(onResult) {
            "You must provide a result callback using onResult()"
        }
        coordinator.launch()
    }
}
