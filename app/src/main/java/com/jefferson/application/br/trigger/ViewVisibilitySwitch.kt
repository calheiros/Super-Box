package com.jefferson.application.br.trigger

import android.view.View
import android.view.animation.Animation

class ViewVisibilitySwitch(private val target: View) {
    var showAnimation: Animation? = null
    var hideAnimation: Animation? = null

    fun switchVisibility() {
        val newVisibility: Int
        var animation: Animation? = null

        if (visibility == View.VISIBLE)  {
            newVisibility = View.GONE
            animation = hideAnimation
        } else {
            newVisibility = View.VISIBLE
            animation = showAnimation
        }
        target.visibility = newVisibility
        if (animation != null)
            target.startAnimation(animation)
    }

    val view: View
        get() = target

    val visibility: Int
        get() = target.visibility
}