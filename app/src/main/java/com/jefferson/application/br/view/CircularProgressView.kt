/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jefferson.application.br.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.jefferson.application.br.R
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt

class CircularProgressView(context: Context?, val attrs: AttributeSet?) : View(context, attrs) {
    private var progress: Double = 0.0
    private var bounds: RectF? = null
    private var colorAccent: Int? = null
    var max: Long = 0
    private var rect: RectF? = null
    private var progressPaint: Paint? = null
    private var backProgressPaint: Paint? = null
    private var textPaint: Paint? = null
    private var padding = 0
    private var progressWidth = 0f
    private var percentPaint: Paint? = null

    init {
        defineVariables()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(rect!!, 144f, 252f, false, backProgressPaint!!)
        canvas.drawArc(rect!!, 144f, progress.toFloat() * 2.52f, false, progressPaint!!)

        bounds = RectF(rect)
        // measure text width
        val progressText = (progress).roundToInt().toString()
        bounds!!.right = textPaint!!.measureText(progressText, 0, progressText.length)
        // measure text height 
        bounds!!.bottom = textPaint!!.descent() - textPaint!!.ascent()
        bounds!!.left += (rect!!.width() - bounds!!.right - percentPaint!!.measureText("%")) / 2.0f
        bounds!!.top += (rect!!.height() - bounds!!.bottom) / 2.0f
        //draw percent symbol
        canvas.drawText(
            "%",
            bounds!!.left + bounds!!.right,
            bounds!!.top - textPaint!!.ascent(),
            percentPaint!!
        )
        //draw progress text
        canvas.drawText(
            progressText,
            bounds!!.left,
            bounds!!.top - textPaint!!.ascent(),
            textPaint!!
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resizeView(w, h)
    }

    /**
     * Retrieves the color resource from the current theme's attributes.
     *
     * @param res The resource ID of the attribute to retrieve
     * @return The integer value of the color attribute
     */
    private fun getAttrColor(res: Int): Int {
        val typedValue = TypedValue()
        val theme = context?.theme
        theme?.resolveAttribute(res, typedValue, true)
        return typedValue.data
    }

    private fun defineVariables() {
        if (colorAccent == null)
            colorAccent = getAttrColor(R.attr.colorAccent)
        val textColor = getAttrColor(R.attr.commonColorLight)
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        rect = RectF()
        backProgressPaint?.style = Paint.Style.STROKE
        backProgressPaint?.color = colorAccent as Int
        backProgressPaint?.strokeCap = Paint.Cap.ROUND
        backProgressPaint?.alpha = 55
        progressPaint?.color = colorAccent as Int
        progressPaint?.strokeCap = Paint.Cap.ROUND
        progressPaint?.style = Paint.Style.STROKE
        percentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        percentPaint?.color = textColor
        textPaint?.color = colorAccent as Int
    }

    /**
     * Resizes the view based on the given width and height.
     * @param w the new width of the view
     * @param h the new height of the view
     */
    private fun resizeView(w: Int, h: Int) {
        padding = w / 16
        val textSize = w / 4f
        progressWidth = padding * 1.5f
        backProgressPaint?.strokeWidth = progressWidth / 2
        progressPaint?.strokeWidth = progressWidth
        textPaint?.textSize = textSize
        textPaint?.isFakeBoldText = true
        percentPaint?.textSize = w / 8f
        rect!![padding.toFloat(), padding.toFloat(), (w - padding).toFloat()] =
            (h - padding).toFloat()
    }

    /**
     * This method is called to determine the size requirements for this custom view and its children.
     * This method is usually called by the parent view that contains the custom view.
     *
     * @param widthMeasureSpec  a measure specification for the width of this custom view
     * @param heightMeasureSpec a measure specification for the height of this custom view
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Default dimensions for the custom view in case of unspecified constraints
        val displayMetrics = context?.resources?.displayMetrics
        val defaultWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, displayMetrics).toInt()
        val defaultHeight =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180f, displayMetrics).toInt()

        // Get the measure specifications and sizes for the custom view
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // Calculate the desired width based on the measure specifications and sizes
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(defaultWidth, widthSize)
            else -> defaultWidth
        }

        // Calculate the desired height based on the measure specifications and sizes
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(defaultHeight, heightSize)
            else -> defaultHeight
        }

        // Set the measured dimensions for the custom view
        setMeasuredDimension(width, height)
    }

    /**
     * Sets the progress value of the widget.
     *
     * @param progress the new progress value
     */
    fun setProgress(progress: Double) {
        if (max == 0L) {
            this.progress = progress
            return
        }
        // Calculate new progress value as a percentage of the maximum value
        val newProgress = (100.0 / max * progress)
        // If the new progress is different than the current progress, update the value and invalidate the widget
        if (newProgress != this.progress) {
            this.progress = newProgress
            invalidate()
        }
    }

    /**
     * Sets the color of the progress bar.
     *
     * @param color the color to set for the progress bar.
     * This should be a color int value and not a resource id.
     */
    fun setProgressColor(color: Int) {
        progressPaint?.color = color
    }

    /**
     * Sets the background color of the progress bar.
     *
     * @param color the color to set for the progress bar background.
     * This should be a color int value and not a resource id.
     */
    fun setBackProgressColor(color: Int) {
        backProgressPaint?.color = color
    }

    fun getProgress(): Double {
        return progress
    }
}