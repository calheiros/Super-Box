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

import android.content.Intent
import android.os.Bundle
import android.util.ArrayMap
import android.view.View
import android.view.View.OnLongClickListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.jefferson.application.br.R
import com.jefferson.application.br.app.SimpleDialog
import com.jefferson.application.br.util.MathUtils
import com.jefferson.application.br.util.MyPreferences
import com.jefferson.application.br.util.StringUtils
import com.jefferson.application.br.view.CircularProgressView
import java.text.DecimalFormat

class CalculatorActivity : MyCompatActivity(), OnLongClickListener {
    private var createCode = false
    private var code: String? = null
    private var hintTextView: TextView? = null
    private val operatorMap = ArrayMap<Char, Char>()
    private var operations = charArrayOf('+', '×', '÷', '-', ',')

    private lateinit var editText: EditText
    private lateinit var resultButton: Button

    override fun onLongClick(view: View): Boolean {
        val id = view.id
        if (id == R.id.calculator_backspaceButton) {
            editText.text.clear()
        } else {
            enter()
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_calculator)
        createCode = ACTION_CREATE_CODE == intent.action
        editText = findViewById<View>(R.id.calculator_layoutEditText) as EditText
        resultButton = findViewById<View>(R.id.calculator_result) as Button
        resultButton.setOnLongClickListener(this)
        editText.isLongClickable = false
        findViewById<View>(R.id.calculator_backspaceButton).setOnLongClickListener(this)
        createOperatorMap()

        if (createCode) {
            hintTextView = findViewById<View>(R.id.calculator_hintTextView) as TextView
            hintTextView!!.text = "Enter your code and press and hold the = button to confirm it."
            //showTipDialog();
        }
    }

    private fun showHintDialog() {
        val dialog = SimpleDialog(this)
        dialog.setTitle("Hint")
        dialog.setMessage("Enter your code and press and hold the = button to confirm it.")
        dialog.setPositiveButton(getString(android.R.string.ok), null)
        dialog.show()
    }

    private fun createOperatorMap() {
        if (createCode) {
            //TODO
        }
        for (c in operations) {
            var value = '0'
            when (c) {
                '×' -> value = '*'
                '÷' -> value = '/'
                ',' -> value = '.'
            }
            if (value != '0') {
                operatorMap[c] = value
            }
        }
    }

    private fun canAppendDot(text: String, position: Int): Boolean {
        if (text.isEmpty()) return true
        // get start of number
        for (i in position downTo 0) {
            val c = text[i]
            if (c == ',') {
                return false
            }
            if (!Character.isDigit(c)) {
                break
            }
        }

        //get end of number
        for (i in position until text.length) {
            val c = text[i]
            if (c == ',') {
                return false
            }
            if (!Character.isDigit(c)) {
                break
            }
        }
        return true
    }

    override fun onApplyTheme() {
        //do nothing...
    }

    fun onKeyPressed(v: View) {
        var key: String? = null
        when (v.id) {
            R.id.calculator_one -> key = "1"
            R.id.calculator_two -> key = "2"
            R.id.calculator_three -> key = "3"
            R.id.calculator_four -> key = "4"
            R.id.calculator_five -> key = "5"
            R.id.calculator_six -> key = "6"
            R.id.calculator_seven -> key = "7"
            R.id.calculator_eight -> key = "8"
            R.id.calculator_nine -> key = "9"
            R.id.calculator_zero -> key = "0"
            R.id.calculator_dot -> {
                val canAppend = canAppendDot(editText.text.toString(), editText.text.length - 1)
                if (canAppend) {
                    editText.append(",")
                }
                return
            }
            R.id.calculator_plus -> {
                appendOperation("+")
                return
            }
            R.id.calculator_division -> {
                appendOperation("÷")
                return
            }
            R.id.calculator_subtration -> {
                appendOperation("-")
                return
            }
            R.id.calculator_multiplication -> {
                appendOperation("×")
                return
            }
            R.id.calculator_percentage -> {
                appendOperation("^")
                return
            }
            R.id.calculator_open_parenthesis -> key = "("
            R.id.calculator_close_parenthesis -> key = ")"
        }
        editText.append(key)
    }

    fun result(v: View?) {
        if (editText.text.toString().isEmpty()) {
            return
        }
        val expression = StringUtils.replaceEach(
            editText.text.toString(), operatorMap
        )
        var result = ""
        try {
            val format = DecimalFormat("0.#")
            result = format.format(MathUtils.eval(expression))
        } catch (e: RuntimeException) {
            Toast.makeText(this, "Invalid format!", Toast.LENGTH_SHORT).show()
        }
        if (result.isNotEmpty()) {
            editText.setText(result)
        }
    }

    fun backspace(v: View?) {
        val length = editText.text.length
        if (length == 0) {
            return
        }
        editText.text.delete(length - 1, length)
    }

    private fun appendOperation(operation: String) {
        var text = editText.text.toString()
        if (text.isEmpty()) {
            return
        }
        val lastChar = text[text.length - 1]
        for (op in operations) {
            if (lastChar == op) {
                text = text.substring(0, text.length - 1)
                break
            }
        }
        editText.setText(text.plus(operation))
    }

    private fun enter() {
        val input = editText.text.toString()
        if (input.isEmpty()) return
        if (createCode) {
            if (input.length > 50) {
                Toast.makeText(this, "Too big! Maximum 50 characters", Toast.LENGTH_SHORT).show()
                return
            } else if (input.length < 3) {
                showHint("Too short! Minimum 3 characters")
                return
            }
            if (code != null) {
                if (code == input) {
                    MyPreferences.putCalculatorCode(input, this)
                    Toast.makeText(this, "Code confirmed!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showHint("The code does not match!")
                }
            } else {
                code = input
                editText.text.clear()
                showHint("Type your code again to confirm it.")
            }
        } else if (input == MyPreferences.getCalculatorCode(this)) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun showHint(message: String) {
        if (hintTextView!!.visibility != View.VISIBLE) {
            hintTextView!!.visibility = View.VISIBLE
        }
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.duration = 200
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animator: Animation) {}
            override fun onAnimationEnd(aninator: Animation) {
                val fadeIn = AnimationUtils.loadAnimation(this@CalculatorActivity, R.anim.fade_in)
                fadeIn.duration = 200
                hintTextView!!.text = message
                hintTextView!!.startAnimation(fadeIn)
            }

            override fun onAnimationRepeat(animator: Animation) {}
        }
        )
        startHintFadeInAnim(message)
        hintTextView!!.startAnimation(fadeOut)
        //Snackbar.make(editText, message, Snackbar.LENGTH_SHORT).show();
    }

    private fun startHintFadeInAnim(message: String) {
        val fadeIn = AnimationUtils.loadAnimation(this@CalculatorActivity, R.anim.fade_in)
        fadeIn.duration = 200
        hintTextView!!.text = message
        hintTextView!!.startAnimation(fadeIn)
    }

    override fun onBackPressed() {
        if (createCode && code != null) {
            code = null
            showHint("Last code cleared!")
            return
        }
        super.onBackPressed()
    }

    companion object {
        const val ACTION_CREATE_CODE = "create_code_action"
    }
}