package com.jefferson.application.br.triggers

import android.view.View

class SwitchVisibilityTrigger(private val view: View) {
    fun switchVisibility() {
        val visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
        view.visibility = visibility
    }

    val visibility: Int
        get() = view.visibility
}