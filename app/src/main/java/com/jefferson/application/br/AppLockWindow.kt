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
package com.jefferson.application.br

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import com.jefferson.application.br.MaterialLockView.OnPatternListener
import com.jefferson.application.br.database.AppLockDatabase
import com.jefferson.application.br.util.PasswordManager

class AppLockWindow(private val context: Context, private val database: AppLockDatabase) {

    var lockePackageName: String? = null
    var isLocked = false
    var passedApp: String? = ""
    private val windowManager: WindowManager
    private var view: View? = null
    private val params: WindowManager.LayoutParams
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var materialLockView: MaterialLockView? = null
    private var iconImageView: ImageView? = null
    private var password: String? = null
    private var lastView: View? = null


    init {
        @Suppress("DEPRECATION")
        val layoutParamsType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE
        @Suppress("DEPRECATION")
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutParamsType,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            PixelFormat.TRANSLUCENT
        )
        params.windowAnimations = android.R.style.Animation_Dialog
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createView()
    }

    fun setPassword(realPassword: String?) {
        password = realPassword
    }

    private fun createParentView(): View {
        val layout: FrameLayout = object : FrameLayout(context) {
            public override fun onAttachedToWindow() {
                super.onAttachedToWindow()
            }

            override fun dispatchKeyEvent(e: KeyEvent): Boolean {
                if (e.keyCode == KeyEvent.KEYCODE_BACK) {
                    startDefaultLauncher()
                }
                return true
            }
        }

        layout.isFocusable = true
        return LayoutInflater.from(context).inflate(R.layout.activiyt_pattern, layout)
    }

    private fun startDefaultLauncher(): Boolean {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun refreshView() {
        createView()
        if (isLocked) {
            windowManager.removeViewImmediate(lastView)
            windowManager.addView(view, params)
        }
    }

    private fun createView() {
        lastView = view
        view = createParentView()
        iconImageView = view?.findViewById<View>(R.id.icon_super_view) as ImageView
        materialLockView = view?.findViewById<View>(R.id.pattern) as MaterialLockView
        if (lockePackageName != null) iconImageView?.setImageDrawable(
            getIconDrawable(
                lockePackageName!!
            )
        )
        if (materialLockView != null) {
            materialLockView?.isTactileFeedbackEnabled = false
            materialLockView?.setOnPatternListener(
                PatternListener(
                    context
                )
            )
        }
    }

    fun lock(appName: String) {
        isLocked = true
        lockePackageName = appName
        iconImageView?.setImageDrawable(getIconDrawable(appName))
        windowManager.addView(view, params)
    }

    fun unlock() {
        try {
            windowManager.removeView(view)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        isLocked = false
    }

    private fun getIconDrawable(packageName: String): Drawable? {
        try {
            return context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    inner class PatternListener(private val context: Context) : OnPatternListener() {
        private val runnable: Runnable = Runnable { materialLockView!!.clearPattern() }

        override fun onPatternStart() {
            handler.removeCallbacks(runnable)
        }

        override fun onPatternDetected(
            pattern: List<MaterialLockView.Cell>, SimplePattern: String
        ) {
            if (SimplePattern != correctPass()) {
                materialLockView!!.displayMode = MaterialLockView.DisplayMode.Wrong
                handler.postDelayed(runnable, 1000)
            } else {
                unlock()
                materialLockView!!.clearPattern()
                database.addUnlockedApp(lockePackageName)
                passedApp = lockePackageName
                //Toast.makeText(context, "A aplicação continuará desbloqueada até que a tela seja desligada!", Toast.LENGTH_LONG).show();
            }
            super.onPatternDetected(pattern, SimplePattern)
        }

        private fun correctPass(): String? {
            return if (password != null) {
                password
            } else {
                PasswordManager.getPatternCode(context)
            }
        }
    }
}