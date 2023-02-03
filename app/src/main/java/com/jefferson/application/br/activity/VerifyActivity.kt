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
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import com.jefferson.application.br.MaterialLockView
import com.jefferson.application.br.MaterialLockView.OnPatternListener
import com.jefferson.application.br.R
import com.jefferson.application.br.activity.CreatePattern
import com.jefferson.application.br.util.BlurUtils
import com.jefferson.application.br.util.MyPreferences
import com.jefferson.application.br.util.PasswordManager
import java.io.IOException

class VerifyActivity : MyCompatActivity() {
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private lateinit var materialLockView: MaterialLockView
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        password = PasswordManager(this).internalPassword
        if (password.isEmpty()) {
            startActivity(
                Intent(
                    applicationContext, CreatePattern::class.java
                ).setAction(CreatePattern.ENTER_FIST_CREATE)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            overridePendingTransition(0, 0)
            return
        }
        setNavigationAndStatusBarTransparent()
        setContentView(R.layout.pattern)
        setWallpaper()
        val sharedPrefs = MyPreferences.getSharedPreferences(this)
        if (sharedPrefs.getBoolean(MyPreferences.KEY_FINGERPRINT, false)) openBiometricPrompt()
        materialLockView = findViewById(R.id.pattern)
        materialLockView.isTactileFeedbackEnabled = false
        handler = Handler()
        runnable = Runnable { materialLockView.clearPattern() }
        materialLockView.setOnPatternListener(MyPatternListener())
    }

    private fun setNavigationAndStatusBarTransparent() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun setWallpaper() {
        try {
            val imageView = findViewById<ImageView>(R.id.wallpaper_image_view) ?: return
            val asset = assets
            val rawImage = asset.open("wallpapers/pexels-bruno-thethe.jpg")
            val wallpaper = BitmapFactory.decodeStream(rawImage)
            BlurUtils.blurBitmap(wallpaper, 25f, this)
            imageView.setImageBitmap(wallpaper)
        } catch (err: IOException) {
            Toast.makeText(this, "failed to decode wallpaper", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val promptInfo = PromptInfo.Builder().setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setDescription(getString(R.string.biometric_desc))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .setConfirmationRequired(false).build()
        val biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@VerifyActivity, errString, Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startMainActivity()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@VerifyActivity, "authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        biometricPrompt.authenticate(promptInfo)
    }

    override fun onApplyCustomTheme() {
        setTheme(R.style.LauncherTheme)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_WRITE_READ_PERMISSION_CODE) {
            if (haveWriteReadPermission()) {
                startMainActivity()
            } else {
                materialLockView.clearPattern()
                Toast.makeText(this, "Required permission not allowed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun wrongPasswdAnimation() {
        val shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_anim)
        val view = findViewById<View>(R.id.icon_super_view)
        view?.startAnimation(shakeAnim)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
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

    private fun startMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private inner class MyPatternListener : OnPatternListener() {
        override fun onPatternStart() {
            handler.removeCallbacks(runnable)
        }

        override fun onPatternDetected(
            pattern: List<MaterialLockView.Cell>, SimplePattern: String
        ) {
            if (SimplePattern != password) {
                materialLockView.displayMode = MaterialLockView.DisplayMode.Wrong
                handler.postDelayed(runnable, 2000)
                wrongPasswdAnimation()
            } else {
                materialLockView.displayMode = MaterialLockView.DisplayMode.Correct
                if (haveWriteReadPermission()) {
                    startMainActivity()
                } else {
                    requestWriteReadPermission()
                }
            }
            super.onPatternDetected(pattern, SimplePattern)
        }
    }

    companion object {
        private const val REQUEST_WRITE_READ_PERMISSION_CODE = 13
    }
}