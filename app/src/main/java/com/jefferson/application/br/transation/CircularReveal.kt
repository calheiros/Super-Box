package com.jefferson.application.br.transation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.transition.Transition
import android.transition.TransitionValues
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup

class CircularReveal : Transition() {
    private var startX: Float = 0f
    private var startY: Float = 0f

    override fun captureStartValues(transitionValues: TransitionValues) {}

    override fun captureEndValues(transitionValues: TransitionValues) {
        transitionValues.values["radius"] = transitionValues.view.width.coerceAtLeast(transitionValues.view.height) / 2f
        transitionValues.values["centerX"] = (transitionValues.view.left + transitionValues.view.right) / 2f
        transitionValues.values["centerY"] = (transitionValues.view.top + transitionValues.view.bottom) / 2f

        startX = transitionValues.view.x
        startY = transitionValues.view.y
    }

    @SuppressLint("ObjectAnimatorBinding")
    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }

        val startRadius = startValues.values["radius"] as Float? ?: return null
        val endRadius = 0f
        val startX = startValues.values["centerX"] as Float? ?: return null
        val startY = startValues.values["centerY"] as Float? ?: return null

        val animator = ViewAnimationUtils.createCircularReveal(endValues.view, startX.toInt(), startY.toInt(), startRadius, endRadius)
        animator.duration = 1000

        val translationX = PropertyValuesHolder.ofFloat("startX", startX, 0f)
        val translationY = PropertyValuesHolder.ofFloat("startY", startY, 0f)

        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(endValues.view, translationX, translationY)


        val alphaAnimator =  ObjectAnimator.ofFloat(endValues.view, View.ALPHA, 0f, 1f).apply {
            duration = 300
        }

        val set = AnimatorSet()
        set.playTogether(animator, objectAnimator, alphaAnimator)
        set.duration = 1000

        return set
    }
}
