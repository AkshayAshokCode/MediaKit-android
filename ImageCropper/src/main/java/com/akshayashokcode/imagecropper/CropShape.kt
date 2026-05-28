package com.akshayashokcode.imagecropper

sealed class CropShape {
    object Rectangle : CropShape()
    object Circle : CropShape()
}
