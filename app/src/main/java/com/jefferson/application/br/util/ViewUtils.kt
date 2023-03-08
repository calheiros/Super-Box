package com.jefferson.application.br.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import kotlin.math.roundToInt

object ViewUtils {
    /* set bottom padding keeping other paddings */
    fun setViewPaddingBottom(view: View?, bottom: Int) {
        if (view == null) return
        val left = view.paddingLeft
        val right = view.paddingRight
        val top = view.paddingTop
        view.setPadding(left, top, right, bottom)
    }

    fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

}
