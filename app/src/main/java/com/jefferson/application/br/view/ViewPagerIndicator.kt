package com.jefferson.application.br.view

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.ScaleAnimation

class ViewPagerIndicator(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private var mCount = 0
    private var mCurrentPosition = 0
    private var mIndicatorSize = 0
    private var mIndicatorSpacing = 0
    private var mPaint: Paint? = null
    private var mSelectedPaint: Paint? = null
    private var mAnimator: ValueAnimator? = null

    init {
        init()
    }

    private fun init() {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint?.style = Paint.Style.FILL
        mPaint?.color = Color.WHITE
        mSelectedPaint = Paint(mPaint)
        mSelectedPaint?.color = Color.RED
        mIndicatorSize = dpToPx(8f).toInt()
        mIndicatorSpacing = dpToPx(8f).toInt()
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp,
            resources.displayMetrics
        )
    }

    fun setCount(count: Int) {
        mCount = count
        requestLayout()
    }

    fun setCurrentPosition(position: Int) {
        if (position < 0 || position >= mCount) {
            throw IndexOutOfBoundsException()
        }
        if (mAnimator != null) {
            mAnimator!!.cancel()
        }
        val start = mCurrentPosition * (mIndicatorSize + mIndicatorSpacing)
        val end = position * (mIndicatorSize + mIndicatorSpacing)
        mAnimator = ValueAnimator.ofInt(start, end)
        mAnimator?.duration = 200
        mAnimator?.addUpdateListener(AnimatorUpdateListener { valueAnimator ->
            val offset = valueAnimator.animatedValue as Int
            translationX = offset.toFloat()
        })
        mAnimator?.start()
        mCurrentPosition = position
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = mCount * mIndicatorSize + (mCount - 1) * mIndicatorSpacing
        val height = mIndicatorSize
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var cx: Float
        var cy: Float
        for (i in 0 until mCount) {
            cx = (mIndicatorSize + mIndicatorSpacing) * i + mIndicatorSize / 2f
            cy = mIndicatorSize / 2f
            if (i == mCurrentPosition) {
                mSelectedPaint?.let { canvas.drawCircle(cx, cy, mIndicatorSize / 2f, it) }
            } else {
                mPaint?.let { canvas.drawCircle(cx, cy, mIndicatorSize / 2f, it) }
            }
        }
    }
}
