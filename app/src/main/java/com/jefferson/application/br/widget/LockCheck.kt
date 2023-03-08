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
package com.jefferson.application.br.widget

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatCheckBox
import com.jefferson.application.br.R

class LockCheck(context: Context?, attrs: AttributeSet?) : AppCompatCheckBox(
    context!!, attrs
) {
    override fun setChecked(checked: Boolean) {
        setBackgroundResource(if (checked) R.drawable.ic_lock else R.drawable.ic_lock_open_variant)
        super.setChecked(checked)
    }

    fun animateView() {
        val animation = AnimationUtils.loadAnimation(context, R.anim.checked)
        startAnimation(animation)
    }

    fun setCheckedNoAnim(checked: Boolean) {
        isChecked = checked
    }
}