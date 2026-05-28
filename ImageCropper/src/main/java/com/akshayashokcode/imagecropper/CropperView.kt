package com.akshayashokcode.imagecropper

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.akshayashokcode.imagecropper.internal.CropOverlayDrawer
import com.akshayashokcode.imagecropper.internal.CropTouchHandler
import com.akshayashokcode.imagecropper.internal.CropperSavedState

class CropperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var bitmap: Bitmap? = null
    private val matrix = Matrix()
    private val cropRect = RectF()
    private val overlayDrawer = CropOverlayDrawer()
    private val touchHandler = CropTouchHandler(cropRect)

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var currentTouch = CropTouchHandler.Area.NONE

    private var currentAspectRatio: AspectRatio = AspectRatio.Free
    private var aspectRatioLocked = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let { canvas.drawBitmap(it, matrix, overlayDrawer.bitmapPaint) }
        overlayDrawer.drawOverlay(canvas, cropRect, width.toFloat(), height.toFloat())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentTouch = touchHandler.detectTouchArea(x, y)
                lastTouchX = x
                lastTouchY = y
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastTouchX
                val dy = y - lastTouchY
                touchHandler.updateCropRect(currentTouch, dx, dy)
                constrainCropRectToImageBounds(currentTouch)
                lastTouchX = x
                lastTouchY = y
                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                currentTouch = CropTouchHandler.Area.NONE
            }
        }
        return true
    }

    fun setImageBitmap(bmp: Bitmap) {
        bitmap = bmp
        matrix.reset()

        if (width == 0 || height == 0) {
            post { setImageBitmap(bmp) }
            return
        }

        val scale = minOf(width.toFloat() / bmp.width, height.toFloat() / bmp.height)
        val dx = (width - bmp.width * scale) / 2
        val dy = (height - bmp.height * scale) / 2

        matrix.postScale(scale, scale)
        matrix.postTranslate(dx, dy)

        val bitmapRect = RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
        matrix.mapRect(bitmapRect)
        cropRect.set(bitmapRect)

        // Re-apply aspect ratio lock after bitmap reset (e.g. after rotate/flip)
        if (aspectRatioLocked && currentAspectRatio !is AspectRatio.Free) {
            applyAspectRatioToCropRect(currentAspectRatio.ratioWidth, currentAspectRatio.ratioHeight)
        }

        invalidate()
    }

    // ── Transforms ───────────────────────────────────────────────────────────

    fun rotate90CW() {
        val bmp = bitmap ?: return
        val m = Matrix().apply { postRotate(90f) }
        setImageBitmap(Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true))
    }

    fun rotate90CCW() {
        val bmp = bitmap ?: return
        val m = Matrix().apply { postRotate(-90f) }
        setImageBitmap(Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true))
    }

    fun flipHorizontal() {
        val bmp = bitmap ?: return
        val m = Matrix().apply { postScale(-1f, 1f, bmp.width / 2f, bmp.height / 2f) }
        setImageBitmap(Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true))
    }

    fun flipVertical() {
        val bmp = bitmap ?: return
        val m = Matrix().apply { postScale(1f, -1f, bmp.width / 2f, bmp.height / 2f) }
        setImageBitmap(Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true))
    }

    // ── Shape & aspect ratio ──────────────────────────────────────────────────

    fun setCropShape(shape: CropShape) {
        overlayDrawer.cropShape = shape
        if (shape is CropShape.Circle) {
            touchHandler.aspectRatioWidth = 1f
            touchHandler.aspectRatioHeight = 1f
            val size = minOf(cropRect.width(), cropRect.height())
            val cx = cropRect.centerX()
            val cy = cropRect.centerY()
            cropRect.set(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
        }
        invalidate()
    }

    fun setAspectRatio(ratio: AspectRatio, locked: Boolean) {
        currentAspectRatio = ratio
        aspectRatioLocked = locked
        when (ratio) {
            is AspectRatio.Free -> {
                touchHandler.aspectRatioWidth = 0f
                touchHandler.aspectRatioHeight = 0f
            }
            else -> {
                touchHandler.aspectRatioWidth = ratio.ratioWidth
                touchHandler.aspectRatioHeight = ratio.ratioHeight
                applyAspectRatioToCropRect(ratio.ratioWidth, ratio.ratioHeight)
                invalidate()
            }
        }
    }

    // ── Output ────────────────────────────────────────────────────────────────

    fun getCroppedImage(): Bitmap? {
        val bmp = bitmap ?: return null
        if (bmp.isRecycled) return null

        val cropWidth = cropRect.width().toInt().coerceAtLeast(1)
        val cropHeight = cropRect.height().toInt().coerceAtLeast(1)

        val output = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        canvas.translate(-cropRect.left, -cropRect.top)
        canvas.drawBitmap(bmp, matrix, overlayDrawer.bitmapPaint)

        if (overlayDrawer.cropShape is CropShape.Circle) {
            return applyCircleMask(output, cropWidth, cropHeight)
        }

        return output
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun applyAspectRatioToCropRect(ratioW: Float, ratioH: Float) {
        val bounds = imageBounds() ?: return

        var newW = minOf(cropRect.width(), bounds.width())
        var newH = newW * ratioH / ratioW

        if (newH > bounds.height()) {
            newH = bounds.height()
            newW = newH * ratioW / ratioH
        }

        // Guard against floating-point precision making min > max when newW ≈ bounds.width()
        val cxMin = minOf(bounds.left + newW / 2, bounds.right - newW / 2)
        val cxMax = maxOf(bounds.left + newW / 2, bounds.right - newW / 2)
        val cyMin = minOf(bounds.top + newH / 2, bounds.bottom - newH / 2)
        val cyMax = maxOf(bounds.top + newH / 2, bounds.bottom - newH / 2)

        val cx = cropRect.centerX().coerceIn(cxMin, cxMax)
        val cy = cropRect.centerY().coerceIn(cyMin, cyMax)

        cropRect.set(cx - newW / 2, cy - newH / 2, cx + newW / 2, cy + newH / 2)
    }

    private fun constrainCropRectToImageBounds(touchArea: CropTouchHandler.Area) {
        val bounds = imageBounds() ?: return

        if (touchArea == CropTouchHandler.Area.MOVE) {
            // Shift the whole rect to stay within bounds without resizing it
            val dx = when {
                cropRect.left < bounds.left -> bounds.left - cropRect.left
                cropRect.right > bounds.right -> bounds.right - cropRect.right
                else -> 0f
            }
            val dy = when {
                cropRect.top < bounds.top -> bounds.top - cropRect.top
                cropRect.bottom > bounds.bottom -> bounds.bottom - cropRect.bottom
                else -> 0f
            }
            cropRect.offset(dx, dy)
        } else {
            // Clamp each edge independently — corner drag can never exceed image bounds
            val minSize = com.akshayashokcode.imagecropper.internal.CropConstants.MIN_CROP_SIZE
            cropRect.left = cropRect.left.coerceIn(bounds.left, cropRect.right - minSize)
            cropRect.top = cropRect.top.coerceIn(bounds.top, cropRect.bottom - minSize)
            cropRect.right = cropRect.right.coerceIn(cropRect.left + minSize, bounds.right)
            cropRect.bottom = cropRect.bottom.coerceIn(cropRect.top + minSize, bounds.bottom)
        }
    }

    private fun imageBounds(): RectF? {
        val bmp = bitmap ?: return null
        return RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat()).apply {
            matrix.mapRect(this)
        }
    }

    private fun applyCircleMask(source: Bitmap, w: Int, h: Int): Bitmap {
        val masked = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(masked)
        val cx = w / 2f
        val cy = h / 2f
        val radius = minOf(w, h) / 2f

        // Draw circular clip region
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawCircle(cx, cy, radius, circlePaint)

        // Draw source bitmap clipped to circle
        val srcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
        canvas.drawBitmap(source, 0f, 0f, srcPaint)
        return masked
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        // Re-fit image whenever the view is resized (e.g. when insets are applied)
        bitmap?.let { setImageBitmap(it) }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // Exclude the entire view from system gesture navigation so crop drags aren't intercepted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            systemGestureExclusionRects = listOf(android.graphics.Rect(0, 0, width, height))
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return CropperSavedState(super.onSaveInstanceState(), cropRect)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is CropperSavedState) {
            super.onRestoreInstanceState(state.superState)
            cropRect.set(state.cropLeft, state.cropTop, state.cropRight, state.cropBottom)
            invalidate()
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
