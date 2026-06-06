package com.akshayashokcode.audiorecorder.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

internal class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val amplitudes = ArrayDeque<Float>()
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val barRect = RectF()

    private val barWidth = context.resources.displayMetrics.density * 3f
    private val barGap = context.resources.displayMetrics.density * 2f
    private val barRadius = barWidth / 2f
    private val minBarHeight = context.resources.displayMetrics.density * 4f

    fun addAmplitude(normalised: Float) {
        amplitudes.addLast(normalised.coerceIn(0f, 1f))
        val maxBars = ((width / (barWidth + barGap)).toInt()).coerceAtLeast(1)
        while (amplitudes.size > maxBars) amplitudes.removeFirst()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (amplitudes.isEmpty()) return

        val cx = width / 2f
        val cy = height / 2f
        val maxBarHalf = cy * 0.85f
        val step = barWidth + barGap

        // Draw bars centred, most recent on the right
        amplitudes.forEachIndexed { i, amp ->
            val barHalf = (minBarHeight / 2f).coerceAtLeast(amp * maxBarHalf)
            val x = cx + (i - amplitudes.size + 1) * step
            barRect.set(x, cy - barHalf, x + barWidth, cy + barHalf)
            canvas.drawRoundRect(barRect, barRadius, barRadius, barPaint)
        }
    }

    fun reset() {
        amplitudes.clear()
        invalidate()
    }
}
