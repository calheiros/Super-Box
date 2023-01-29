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
package com.jefferson.application.br.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.EditText
import android.widget.Toast
import com.jefferson.application.br.R

class PinActivity : Activity(), OnLongClickListener {
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pin_activity)
        editText = findViewById(R.id.pinActivityEditText)
        findViewById<View>(R.id.pin_activityBackspaceButton).setOnLongClickListener(this)
    }

    private fun putNumber(num: String?) {
        editText.append(num)
    }

    fun one(v: View?) {
        putNumber("1")
    }

    fun two(v: View?) {
        putNumber("2")
    }

    fun three(v: View?) {
        putNumber("3")
    }

    fun four(v: View?) {
        putNumber("4")
    }

    fun five(v: View?) {
        putNumber("5")
    }

    fun six(v: View?) {
        putNumber("6")
    }

    fun seven(v: View?) {
        putNumber("7")
    }

    fun eight(v: View?) {
        putNumber("8")
    }

    fun nine(v: View?) {
        putNumber("9")
    }

    fun zero(v: View?) {
        putNumber("0")
    }

    override fun onLongClick(vie: View): Boolean {
        editText.setText(String())
        return true
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.length < 4) {
            Toast.makeText(this, "", Toast.LENGTH_LONG).show()
            return false
        }
        if (password.length > 20) {
            Toast.makeText(this, "", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    fun backspace(v: View?) {
        val text = editText.text.toString()
        val size = text.length
        if (size > 0) {
            editText.setText(text.substring(0, size - 1))
            editText.setSelection(size - 1)
        }
    }
}