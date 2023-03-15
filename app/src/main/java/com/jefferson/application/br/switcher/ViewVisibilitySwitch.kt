package com.jefferson.application.br.switcher

import android.view.View
import android.view.animation.Animation

class ViewVisibilitySwitch(private val target: View) {
    var showAnimation: Animation? = null
    var hideAnimation: Animation? = null

    fun switchVisibility() {
        if (visibility == View.VISIBLE) {
            hide()
        } else {
            show()
        }
    }

    fun changeVisibility(newVisibility: Int) {
        if (target.visibility == newVisibility) {
            return
        }
        val animation = if (newVisibility == View.VISIBLE) showAnimation else hideAnimation
        if (animation != null) {
            target.startAnimation(animation)
        }
        target.visibility = newVisibility
    }

    fun show() {
        changeVisibility(View.VISIBLE)
    }

    fun hide() {
       changeVisibility(View.GONE)
    }

    val view: View
        get() = target

    val visibility: Int
        get() = target.visibility
}