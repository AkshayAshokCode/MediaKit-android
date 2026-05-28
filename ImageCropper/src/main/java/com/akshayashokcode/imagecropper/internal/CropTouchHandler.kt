package com.akshayashokcode.imagecropper.internal

import android.graphics.RectF
import kotlin.math.abs

internal class CropTouchHandler(private val cropRect: RectF) {

    enum class Area { NONE, MOVE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    fun detectTouchArea(x: Float, y: Float): Area = when {
        isNear(x, y, cropRect.left, cropRect.top) -> Area.TOP_LEFT
        isNear(x, y, cropRect.right, cropRect.top) -> Area.TOP_RIGHT
        isNear(x, y, cropRect.left, cropRect.bottom) -> Area.BOTTOM_LEFT
        isNear(x, y, cropRect.right, cropRect.bottom) -> Area.BOTTOM_RIGHT
        cropRect.contains(x, y) -> Area.MOVE
        else -> Area.NONE
    }

    fun updateCropRect(area: Area, dx: Float, dy: Float) {
        when (area) {
            Area.MOVE -> cropRect.offset(dx, dy)

            Area.TOP_LEFT -> {
                cropRect.left = (cropRect.left + dx).coerceAtMost(cropRect.right - CropConstants.MIN_CROP_SIZE)
                cropRect.top = (cropRect.top + dy).coerceAtMost(cropRect.bottom - CropConstants.MIN_CROP_SIZE)
            }

            Area.TOP_RIGHT -> {
                cropRect.right = (cropRect.right + dx).coerceAtLeast(cropRect.left + CropConstants.MIN_CROP_SIZE)
                cropRect.top = (cropRect.top + dy).coerceAtMost(cropRect.bottom - CropConstants.MIN_CROP_SIZE)
            }

            Area.BOTTOM_LEFT -> {
                cropRect.left = (cropRect.left + dx).coerceAtMost(cropRect.right - CropConstants.MIN_CROP_SIZE)
                cropRect.bottom = (cropRect.bottom + dy).coerceAtLeast(cropRect.top + CropConstants.MIN_CROP_SIZE)
            }

            Area.BOTTOM_RIGHT -> {
                cropRect.right = (cropRect.right + dx).coerceAtLeast(cropRect.left + CropConstants.MIN_CROP_SIZE)
                cropRect.bottom = (cropRect.bottom + dy).coerceAtLeast(cropRect.top + CropConstants.MIN_CROP_SIZE)
            }

            else -> Unit
        }
    }

    private fun isNear(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        return abs(x1 - x2) < CropConstants.TOUCH_THRESHOLD &&
                abs(y1 - y2) < CropConstants.TOUCH_THRESHOLD
    }
}
