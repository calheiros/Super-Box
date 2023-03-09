package com.jefferson.application.br.view

import android.view.View
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.Toast

class ProgressAnimation(context: Context?, attrs: AttributeSet) : View(context, attrs) {
    private var paint: Paint? = null

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.drawColor(Color.GREEN)
        canvas?.drawText("Loading...", 0F, 0F, paint!!)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        paint = Paint()
        paint?.color = Color.BLACK
        paint?.textSize = 16f
        paint?.strokeWidth = 10f
        Toast.makeText(context, "attached to window: $height", Toast.LENGTH_SHORT).show()
    }
}