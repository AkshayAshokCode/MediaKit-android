package com.akshayashokcode.mediapicker.builder

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.mediapicker.coordinator.MediaPickerCoordinator
import com.akshayashokcode.mediapicker.model.MediaPickerException
import com.akshayashokcode.mediapicker.model.MediaPickerOptions
import com.akshayashokcode.mediapicker.model.MediaPickerResult
import com.akshayashokcode.mediapicker.model.MediaType

/**
 * Fluent builder for configuring and launching the MediaKit unified media picker.
 *
 * **Lifecycle requirement:** construct this builder (i.e. call [with] and the full
 * fluent chain up to but not including [launch]) in `Activity.onCreate` before
 * `setContent`, so all `registerForActivityResult` calls happen before STARTED.
 *
 * Options (mediaTypes, allowMultiple, restrictions) are read lazily at [launch] time,
 * so they may be mutated between construction and launch.
 */
class MediaPickerBuilder private constructor(
    private val context: Context,
    private val caller: ActivityResultCaller
) {
    private var mediaTypes: List<MediaType> = listOf(MediaType.All)
    private var restrictMimeTypes: List<String> = emptyList()
    private var restrictExtensions: List<String> = emptyList()
    private var allowMultiple: Boolean = false

    private var onResult: ((MediaPickerResult) -> Unit)? = null
    private var onError: ((MediaPickerException) -> Unit)? = null

    private val coordinator = MediaPickerCoordinator(
        context = context,
        caller = caller,
        getOptions = {
            MediaPickerOptions(
                mediaTypes = mediaTypes,
                restrictMimeTypes = restrictMimeTypes,
                restrictExtensions = restrictExtensions,
                allowMultiple = allowMultiple
            )
        },
        onResult = { result -> onResult?.invoke(result) },
        onError = { error -> onError?.invoke(error) }
    )

    companion object {
        fun with(context: Context, caller: ActivityResultCaller): MediaPickerBuilder =
            MediaPickerBuilder(context, caller)
    }

    fun mediaTypes(vararg types: MediaType): MediaPickerBuilder = apply {
        mediaTypes = types.toList()
    }

    fun restrictMimeTypes(vararg mimeTypes: String): MediaPickerBuilder = apply {
        restrictMimeTypes = mimeTypes.toList()
    }

    fun restrictExtensions(vararg extensions: String): MediaPickerBuilder = apply {
        restrictExtensions = extensions.toList()
    }

    fun allowMultiple(allow: Boolean): MediaPickerBuilder = apply {
        allowMultiple = allow
    }

    fun onResult(callback: (MediaPickerResult) -> Unit): MediaPickerBuilder = apply {
        onResult = callback
    }

    fun onError(callback: (MediaPickerException) -> Unit): MediaPickerBuilder = apply {
        onError = callback
    }

    fun launch() {
        requireNotNull(onResult) { "You must provide a result callback using onResult()" }
        coordinator.launch()
    }
}
