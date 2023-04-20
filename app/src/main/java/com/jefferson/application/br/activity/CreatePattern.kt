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
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.jefferson.application.br.App
import com.jefferson.application.br.MaterialLockView
import com.jefferson.application.br.MaterialLockView.OnPatternListener
import com.jefferson.application.br.R
import com.jefferson.application.br.service.AppLockService
import com.jefferson.application.br.util.PasswordManager

class CreatePattern : MyCompatActivity() {
    private lateinit var hintLabel: TextView
    private lateinit var materialLockView: MaterialLockView
    private lateinit var button: Button
    private lateinit var runnable: Runnable
    private lateinit var clearRunnable: Runnable
    private lateinit var clearHandler: Handler
    private lateinit var passwordManager: PasswordManager
    private var password: String? = null
    private var handler: Handler? = null
    private var action: String? = null
    private var defaultText: String? = null
    private var oldPass: String? = null

    private fun sendCommandService(key: String?) {
        val intent = Intent(this, AppLockService::class.java)
        intent.action = App.ACTION_APPLOCK_SERVICE_UPDATE_PASSWORD
        intent.putExtra("key", key)
        startService(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_pattern)
        oldPass = PasswordManager.getPatternCode(this)
        action = intent.action
        defaultText = getString(R.string.desenhe_seu_padrao)
        //init views
        hintLabel = findViewById(R.id.pattern_text)
        button = findViewById(R.id.bt_pattern)
        materialLockView = findViewById(R.id.pattern)
        //init variables
        clearHandler = Handler(Looper.getMainLooper())
        clearRunnable = Runnable { materialLockView.clearPattern() }
        //define values
        hintLabel.text = defaultText
        button.isEnabled = false
        materialLockView.isTactileFeedbackEnabled = false
        materialLockView.setOnPatternListener(object : OnPatternListener() {
            override fun onPatternStart() {
                clearHandler.removeCallbacks(clearRunnable)
                hintLabel.text = getString(R.string.solte_para_terminar)
            }

            override fun onPatternDetected(
                pattern: List<MaterialLockView.Cell>,
                SimplePattern: String
            ) {
                if (SimplePattern.length >= 4) {
                    if (password != null) {
                        if (password == SimplePattern) {
                            button.isEnabled = true
                            materialLockView.isEnabled = false
                            hintLabel.text = getString(R.string.senha_definida_como)
                        } else {
                            hintLabel.text = getString(R.string.tente_de_novo)
                            materialLockView.displayMode = MaterialLockView.DisplayMode.Wrong
                            clearHandler.postDelayed(clearRunnable, 1500)
                        }
                    } else {
                        materialLockView.isEnabled = false
                        password = SimplePattern
                        hintLabel.text = getString(R.string.padrao_salvo)
                        materialLockView.displayMode = MaterialLockView.DisplayMode.Correct
                        handler = Handler(Looper.getMainLooper())
                        runnable = Runnable {
                            materialLockView.isEnabled = true
                            materialLockView.clearPattern()
                            hintLabel.text = getString(R.string.desenhe_novamente)
                        }
                        handler!!.postDelayed(runnable, 1500)
                    }
                } else {
                    materialLockView.displayMode = MaterialLockView.DisplayMode.Wrong
                    hintLabel.text = getString(R.string.connect_mais)
                    clearHandler.postDelayed(clearRunnable, 1500)
                }
                super.onPatternDetected(pattern, SimplePattern)
            }
        })
        button.setOnClickListener {
            password?.let { pass -> PasswordManager.setPatternCode(this, pass) }
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
        }
        addBackPressedCallback()
    }

    private fun addBackPressedCallback() {
        onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (password != null) {
                        hintLabel.text = defaultText
                        password = null
                        button.isEnabled = false
                        materialLockView.clearPattern()
                        handler?.removeCallbacks(runnable)
                        if (!materialLockView.isEnabled) {
                            materialLockView.isEnabled = true
                        }
                    } else {
                        finish()
                    }
                }
            })
    }

    override fun onApplyTheme() {}
    private fun startMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_NO_ANIMATION
        )
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

    companion object {
        const val ENTER_FIST_CREATE = "fist_create"
        const val ENTER_RECREATE = "recreate"
    }
}