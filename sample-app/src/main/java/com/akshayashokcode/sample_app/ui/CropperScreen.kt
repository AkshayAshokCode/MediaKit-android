package com.akshayashokcode.sample_app.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.akshayashokcode.imagecropper.CropperView
import com.akshayashokcode.sample_app.R

@Composable
fun CropperScreen() {
    val context = LocalContext.current
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cropperViewState = remember { mutableStateOf<CropperView?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(vertical = 50.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val view = inflater.inflate(R.layout.cropper_view_layout, null)
                val cropperView = view.findViewById<CropperView>(R.id.cropperView)

                cropperViewState.value = cropperView

                // Load sample image into cropper.
                val bitmap = BitmapFactory.decodeResource(
                    ctx.resources,
                    R.drawable.image
                )

                cropperView.setImageBitmap(bitmap)

                view
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Button(
            onClick = {
                croppedBitmap = cropperViewState.value?.getCroppedImage()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Crop Image")
        }

        croppedBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Cropped",
                modifier = Modifier
                    .padding(16.dp)
                    .height(200.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
