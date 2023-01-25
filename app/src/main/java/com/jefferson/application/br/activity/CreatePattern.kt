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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.jefferson.application.br.App
import com.jefferson.application.br.MaterialLockView
import com.jefferson.application.br.MaterialLockView.OnPatternListener
import com.jefferson.application.br.R
import com.jefferson.application.br.service.AppLockService
import com.jefferson.application.br.util.PasswordManager

class CreatePattern : MyCompatActivity() {
    private var password: String? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var clearRunnable: Runnable? = null
    private var clearHandler: Handler? = null
    private var action: String? = null
    private var materialLockView: MaterialLockView? = null
    private var passwordManager: PasswordManager? = null
    private var defaultText: String? = null
    private var button: Button? = null
    private var text: TextView? = null
    private var oldPass: String? = null
    fun sendCommandService(key: String?) {
        val intent = Intent(this, AppLockService::class.java)
        intent.action = App.ACTION_APPLOCK_SERVICE_UPDATE_PASSWORD
        intent.putExtra("key", key)
        startService(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_pattern)
        // applyParentViewPadding(findViewById(R.id.create_pattern_parent_layout));
        passwordManager = PasswordManager()
        oldPass = passwordManager!!.internalPassword
        action = intent.action
        defaultText = getString(R.string.desenhe_seu_padrao)
        text = findViewById(R.id.pattern_text)
        button = findViewById(R.id.bt_pattern)
        text.setText(defaultText)
        button.setEnabled(false)
        materialLockView = findViewById(R.id.pattern)
        materialLockView.setTactileFeedbackEnabled(false)
        materialLockView.setOnPatternListener(object : OnPatternListener() {
            override fun onPatternStart() {
                if (clearRunnable != null && clearHandler != null) {
                    clearHandler!!.removeCallbacks(clearRunnable!!)
                }
                text.setText(getString(R.string.solte_para_terminar))
            }

            override fun onPatternDetected(
                pattern: List<MaterialLockView.Cell>,
                SimplePattern: String
            ) {
                if (SimplePattern.length >= 4) {
                    if (password != null) {
                        if (password == SimplePattern) {
                            button.setEnabled(true)
                            materialLockView.setEnabled(false)
                            text.setText(getString(R.string.senha_definida_como))
                        } else {
                            text.setText(getString(R.string.tente_de_novo))
                            materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong)
                            clearPattern()
                        }
                    } else {
                        materialLockView.setEnabled(false)
                        password = SimplePattern
                        text.setText(getString(R.string.padrao_salvo))
                        materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Correct)
                        handler = Handler()
                        runnable = Runnable {
                            materialLockView.setEnabled(true)
                            materialLockView.clearPattern()
                            text.setText(getString(R.string.desenhe_novamente))
                        }
                        handler!!.postDelayed(runnable, 1500)
                    }
                } else {
                    materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong)
                    text.setText(getString(R.string.connect_mais))
                    clearPattern()
                }
                super.onPatternDetected(pattern, SimplePattern)
            }
        })
        button.setOnClickListener(View.OnClickListener {
            passwordManager!!.setPassword(password)
            sendCommandService(password)
            when (action) {
                ENTER_FIST_CREATE -> if (haveWriteReadPermission()) {
                    startMainActivity()
                } else {
                    requestWriteReadPermission()
                }
                ENTER_RECREATE -> finish()
                else -> Toast.makeText(this@CreatePattern, "UNKNOWN ACTION!", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onApplyCustomTheme() {}
    private fun startMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in permissions.indices) {
            val permission = permissions[i]
            val grantResult = grantResults[i]
            if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    startMainActivity()
                    break
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (haveWriteReadPermission()) {
            startMainActivity()
        }
    }

    private fun clearPattern() {
        clearHandler = Handler()
        clearRunnable = Runnable { materialLockView!!.clearPattern() }
        clearHandler!!.postDelayed(clearRunnable, 1500)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onBackPressed() {
        if (password != null) {
            text!!.text = defaultText
            password = null
            button!!.isEnabled = false
            materialLockView!!.clearPattern()
            handler!!.removeCallbacks(runnable!!)
            if (!materialLockView!!.isEnabled) {
                materialLockView!!.isEnabled = true
            }
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val ENTER_FIST_CREATE = "fist_create"
        const val ENTER_RECREATE = "recreate"
    }
}