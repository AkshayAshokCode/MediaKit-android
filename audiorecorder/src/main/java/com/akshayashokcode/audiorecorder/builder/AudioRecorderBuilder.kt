package com.akshayashokcode.audiorecorder.builder

import android.Manifest
import android.content.Context
import androidx.activity.result.ActivityResultCaller
import com.akshayashokcode.audiorecorder.contract.AudioRecorderContract
import com.akshayashokcode.audiorecorder.model.AudioRecorderException
import com.akshayashokcode.audiorecorder.model.AudioRecorderOptions
import com.akshayashokcode.audiorecorder.model.AudioRecorderResult
import com.akshayashokcode.mediakitcore.launcher.PermissionLauncher

/**
 * Fluent builder for launching an in-app audio recording session.
 *
 * **Lifecycle requirement:** construct this builder in `Activity.onCreate` before
 * `setContent`, so all `registerForActivityResult` calls happen before STARTED.
 */
class AudioRecorderBuilder private constructor(
    private val context: Context,
    caller: ActivityResultCaller
) {
    private var options = AudioRecorderOptions()
    private var onResult: ((AudioRecorderResult) -> Unit)? = null
    private var onError: ((AudioRecorderException) -> Unit)? = null

    private val permissionLauncher = PermissionLauncher(
        caller = caller,
        onResult = { granted ->
            if (granted) recorderLauncher.launch(options)
            else {
                onError?.invoke(AudioRecorderException.PermissionDenied)
                onResult?.invoke(AudioRecorderResult.Error("RECORD_AUDIO permission denied"))
            }
        }
    )

    private val recorderLauncher = caller.registerForActivityResult(AudioRecorderContract()) { result ->
        onResult?.invoke(result)
    }

    companion object {
        fun with(context: Context, caller: ActivityResultCaller): AudioRecorderBuilder =
            AudioRecorderBuilder(context, caller)
    }

    fun options(opts: AudioRecorderOptions): AudioRecorderBuilder = apply { options = opts }

    fun onResult(callback: (AudioRecorderResult) -> Unit): AudioRecorderBuilder = apply {
        onResult = callback
    }

    fun onError(callback: (AudioRecorderException) -> Unit): AudioRecorderBuilder = apply {
        onError = callback
    }

    fun launch() {
        requireNotNull(onResult) { "You must provide a result callback using onResult()" }
        if (PermissionLauncher.isGranted(context, Manifest.permission.RECORD_AUDIO)) {
            recorderLauncher.launch(options)
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }
}
