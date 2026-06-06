package com.akshayashokcode.mediapicker.coordinator

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.mediapicker.launcher.MediaPickerLauncher
import com.akshayashokcode.mediapicker.model.MediaPickerException
import com.akshayashokcode.mediapicker.model.MediaPickerOptions
import com.akshayashokcode.mediapicker.model.MediaPickerResult

internal class MediaPickerCoordinator(
    context: Context,
    caller: ActivityResultCaller,
    private val getOptions: () -> MediaPickerOptions,
    onResult: (MediaPickerResult) -> Unit,
    onError: ((MediaPickerException) -> Unit)? = null
) {
    private val launcher = MediaPickerLauncher(
        context = context,
        caller = caller,
        getOptions = getOptions,
        onResult = onResult,
        onError = onError
    )

    fun launch() = launcher.launch()
}
