package com.jefferson.application.br.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class JLoadingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private lateinit var paint: Paint

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        paint = Paint()
        paint.color = Color.WHITE
        paint.textSize = 15f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(Color.TRANSPARENT)
        canvas?.drawText("Loadingg...", 0f, 0f,paint)
    }
}