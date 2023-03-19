package com.jefferson.application.br.view

import android.view.View
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.util.AttributeSet

class ProgressAnimation(context: Context?, attrs: AttributeSet) : View(context, attrs) {
    private var textX: Float = 0f
    private var paint: Paint = Paint()
    private var handler: Handler
    private var runnable: Runnable
    private var dots: String =  ""
    var textY = 0F
    init {
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.strokeWidth = 10f
        handler = Handler(context?.mainLooper!!)
        runnable = object : Runnable {
            override fun run() {
                update()
                invalidate()
                postDelayed(this, 1000)
            }
        }
    }

    private fun update() {
        dots = if (dots.length > 3) "" else dots.plus(".")
    }
    fun startAnimation() {
        handler.post(runnable)
    }
    fun stopAnimation() {
        handler.removeCallbacks(runnable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        paint.textSize = height.toFloat() * 0.6F
        textY = paint.textSize
        textX = paint.measureText("Loading....") * 0.1f
    }
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.drawColor(Color.BLACK)

        canvas?.drawText("Loading".plus(dots), textX, textY, paint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}