package com.akshayashokcode.imagecropper.internal

import androidx.core.graphics.toColorInt

internal object CropConstants {
    const val TOUCH_THRESHOLD = 40f
    const val MIN_CROP_SIZE = 200f
    const val CORNER_STROKE_WIDTH = 10f
    const val CROP_BORDER_WIDTH = 4f
    const val GRID_LINE_WIDTH = 2f
    const val HANDLE_LENGTH = 40f
    val OVERLAY_COLOR = "#B0000000".toColorInt()
}
