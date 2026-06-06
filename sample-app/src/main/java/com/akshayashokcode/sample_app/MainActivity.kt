package com.akshayashokcode.sample_app

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.akshayashokcode.audiorecorder.entrypoint.AudioRecorder
import com.akshayashokcode.audiorecorder.model.AudioRecorderOptions
import com.akshayashokcode.audiorecorder.model.AudioRecorderResult
import com.akshayashokcode.imagecropper.CropperOptions
import com.akshayashokcode.imagecropper.MediaKitCropProvider
import com.akshayashokcode.imagepicker.entrypoint.ImagePicker
import com.akshayashokcode.imagepicker.model.ImagePickerResult
import com.akshayashokcode.imagepicker.model.MediaSource
import com.akshayashokcode.mediakitcore.ExperimentalMediaKitApi
import com.akshayashokcode.sample_app.ui.MainScreen
import com.akshayashokcode.sample_app.ui.theme.AndroidImageCropperTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMediaKitApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AudioRecorder and the crop picker require ActivityResultCaller — must be before setContent.
        // Other picker screens use rememberImagePicker / rememberMediaPicker (no onCreate restriction).
        val recordingUri = mutableStateOf<Uri?>(null)
        val recordingDurationMs = mutableStateOf(0L)
        val croppedUri = mutableStateOf<Uri?>(null)

        val audioRecorder = AudioRecorder.with(this, this)
            .options(AudioRecorderOptions(maxDurationSeconds = 120, showWaveform = true))
            .onResult { result ->
                if (result is AudioRecorderResult.Success) {
                    recordingUri.value = result.uri
                    recordingDurationMs.value = result.durationMs
                }
            }
            .onError { error -> Log.e("MediaKit", "Recorder error: ${error.message}") }

        val cropPicker = ImagePicker.with(this, this)
            .source(MediaSource.Gallery)
            .crop(MediaKitCropProvider(CropperOptions(showRotateButtons = true, showFlipButtons = true)))
            .onResult { result ->
                if (result is ImagePickerResult.Success) croppedUri.value = result.uri
            }
            .onError { error -> Log.e("MediaKit", "Crop error: ${error.message}") }

        enableEdgeToEdge()
        setContent {
            AndroidImageCropperTheme {
                MainScreen(
                    onLaunchRecorder = { audioRecorder.launch() },
                    recordingUri = recordingUri.value,
                    recordingDurationMs = recordingDurationMs.value,
                    onLaunchCropPicker = { cropPicker.launch() },
                    croppedUri = croppedUri.value
                )
            }
        }
    }
}
