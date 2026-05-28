package com.akshayashokcode.imagecropper.internal

import android.graphics.*
import com.akshayashokcode.imagecropper.CropShape

internal class CropOverlayDrawer {

    var cropShape: CropShape = CropShape.Rectangle

    val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val overlayPaint = Paint().apply {
        color = CropConstants.OVERLAY_COLOR
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = CropConstants.CROP_BORDER_WIDTH
        color = Color.WHITE
    }

    private val gridPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = CropConstants.GRID_LINE_WIDTH
        color = Color.WHITE
        alpha = 120
    }

    private val cornerPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = CropConstants.CORNER_STROKE_WIDTH
        color = Color.WHITE
    }

    fun drawOverlay(canvas: Canvas, rect: RectF, viewWidth: Float, viewHeight: Float) {
        if (cropShape is CropShape.Circle) {
            drawCircleOverlay(canvas, rect, viewWidth, viewHeight)
        } else {
            drawRectOverlay(canvas, rect, viewWidth, viewHeight)
        }
    }

    private fun drawRectOverlay(canvas: Canvas, rect: RectF, viewWidth: Float, viewHeight: Float) {
        canvas.drawRect(0f, 0f, viewWidth, rect.top, overlayPaint)
        canvas.drawRect(0f, rect.bottom, viewWidth, viewHeight, overlayPaint)
        canvas.drawRect(0f, rect.top, rect.left, rect.bottom, overlayPaint)
        canvas.drawRect(rect.right, rect.top, viewWidth, rect.bottom, overlayPaint)

        canvas.drawRect(rect, borderPaint)

        val oneThirdW = rect.width() / 3
        val oneThirdH = rect.height() / 3
        canvas.drawLine(rect.left + oneThirdW, rect.top, rect.left + oneThirdW, rect.bottom, gridPaint)
        canvas.drawLine(rect.left + 2 * oneThirdW, rect.top, rect.left + 2 * oneThirdW, rect.bottom, gridPaint)
        canvas.drawLine(rect.left, rect.top + oneThirdH, rect.right, rect.top + oneThirdH, gridPaint)
        canvas.drawLine(rect.left, rect.top + 2 * oneThirdH, rect.right, rect.top + 2 * oneThirdH, gridPaint)

        drawCorner(canvas, rect.left, rect.top, left = true, top = true)
        drawCorner(canvas, rect.right, rect.top, left = false, top = true)
        drawCorner(canvas, rect.left, rect.bottom, left = true, top = false)
        drawCorner(canvas, rect.right, rect.bottom, left = false, top = false)
    }

    private fun drawCircleOverlay(canvas: Canvas, rect: RectF, viewWidth: Float, viewHeight: Float) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val radius = minOf(rect.width(), rect.height()) / 2f

        // Dim everything outside the circle using even-odd fill
        val path = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            addRect(0f, 0f, viewWidth, viewHeight, Path.Direction.CW)
            addCircle(cx, cy, radius, Path.Direction.CW)
        }
        canvas.drawPath(path, overlayPaint)
        canvas.drawCircle(cx, cy, radius, borderPaint)
    }

    private fun drawCorner(canvas: Canvas, x: Float, y: Float, left: Boolean, top: Boolean) {
        val len = CropConstants.HANDLE_LENGTH
        val signX = if (left) 1 else -1
        val signY = if (top) 1 else -1
        canvas.drawLine(x, y, x + len * signX, y, cornerPaint)
        canvas.drawLine(x, y, x, y + len * signY, cornerPaint)
    }
}
