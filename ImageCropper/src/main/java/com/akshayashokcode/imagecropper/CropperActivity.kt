package com.akshayashokcode.imagecropper

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import java.io.File
import java.io.FileOutputStream
import androidx.core.net.toUri


class CropperActivity : ComponentActivity() {

    companion object {
        const val EXTRA_INPUT_URI = "extra_input_uri"
        const val EXTRA_OUTPUT_URI = "extra_output_uri"
    }

    private lateinit var cropperView: CropperView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cropper)

        cropperView = findViewById(R.id.cropperView)

        val cropButton = findViewById<Button>(R.id.buttonCrop)

        val uriString = intent.getStringExtra(EXTRA_INPUT_URI)

        if (uriString == null) {
            finish()
            return
        }

        val inputUri = uriString.toUri()

        contentResolver.openInputStream(inputUri)?.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            cropperView.setImageBitmap(bitmap)
        }

        cropButton.setOnClickListener {

            val croppedBitmap = cropperView.getCroppedImage()

            if (croppedBitmap == null) {

                setResult(RESULT_CANCELED)
                finish()
                return@setOnClickListener
            }

            val outputFile = File(
                cacheDir,
                "cropped_${System.currentTimeMillis()}.jpg"
            )

            FileOutputStream(outputFile).use { outputStream ->

                croppedBitmap.compress(
                    android.graphics.Bitmap.CompressFormat.JPEG,
                    100,
                    outputStream
                )
            }

            val outputUri = Uri.fromFile(outputFile)

            setResult(
                RESULT_OK,
                Intent().apply {
                    putExtra(
                        EXTRA_OUTPUT_URI,
                        outputUri.toString()
                    )
                }
            )

            finish()
        }
    }
}
