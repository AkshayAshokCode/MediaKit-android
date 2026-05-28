package com.akshayashokcode.sample_app

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.akshayashokcode.imagecropper.MediaKitCropProvider
import com.akshayashokcode.imagepicker.entrypoint.ImagePicker
import com.akshayashokcode.imagepicker.model.ImagePickerResult
import com.akshayashokcode.imagepicker.model.MediaSource
import com.akshayashokcode.sample_app.ui.CropperScreen
import com.akshayashokcode.sample_app.ui.theme.AndroidImageCropperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Built before setContent so registerForActivityResult fires before onStart.
        val pickedBitmap = mutableStateOf<android.graphics.Bitmap?>(null)

        val picker = ImagePicker.with(this, this)
            .source(MediaSource.Gallery)
            .crop(MediaKitCropProvider())
            .onResult { result ->
                when (result) {
                    is ImagePickerResult.Success -> {
                        pickedBitmap.value = contentResolver
                            .openInputStream(result.uri)
                            ?.use { BitmapFactory.decodeStream(it) }
                    }
                    is ImagePickerResult.Error -> {
                        Log.e("MediaKit", "Picker error: ${result.message}")
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
            .onError { error ->
                Log.e("MediaKit", "Picker exception: ${error.message}")
                Toast.makeText(this, error.message ?: "Something went wrong", Toast.LENGTH_SHORT).show()
            }

        enableEdgeToEdge()
        setContent {
            AndroidImageCropperTheme {
                CropperScreen(
                    onPickImage = { picker.launch() },
                    pickedBitmap = pickedBitmap.value
                )
            }
        }
    }
}

