package com.akshayashokcode.mediapreviewer.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

internal class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val imgMatrix = Matrix()
    private var scaleFactor = 1f
    private var bmpW = 0f
    private var bmpH = 0f
    private var lastX = 0f
    private var lastY = 0f

    private val scaleDetector = ScaleGestureDetector(context, object :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean {
            val newScale = (scaleFactor * d.scaleFactor).coerceIn(0.5f, 5f)
            val factor = newScale / scaleFactor
            scaleFactor = newScale
            imgMatrix.postScale(factor, factor, d.focusX, d.focusY)
            imageMatrix = imgMatrix
            return true
        }
    })

    init { scaleType = ScaleType.MATRIX }

    fun fitBitmap(bmp: Bitmap) {
        bmpW = bmp.width.toFloat()
        bmpH = bmp.height.toFloat()
        setImageBitmap(bmp)
        post { resetToFit() }
    }

    private fun resetToFit() {
        if (width == 0 || height == 0 || bmpW == 0f) return
        val scale = minOf(width / bmpW, height / bmpH)
        val dx = (width - bmpW * scale) / 2f
        val dy = (height - bmpH * scale) / 2f
        imgMatrix.reset()
        imgMatrix.postScale(scale, scale)
        imgMatrix.postTranslate(dx, dy)
        scaleFactor = scale
        imageMatrix = imgMatrix
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (bmpW > 0) resetToFit()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { lastX = event.x; lastY = event.y }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress) {
                    imgMatrix.postTranslate(event.x - lastX, event.y - lastY)
                    imageMatrix = imgMatrix
                }
                lastX = event.x; lastY = event.y
            }
        }
        return true
    }
}
